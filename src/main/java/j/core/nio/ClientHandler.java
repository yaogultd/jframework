package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.core.common.Global;
import j.core.common.JArray;
import j.core.nvwa.Nvwa;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.ConcurrentMap;
import j.util.JUtilMath;
import j.util.JUtilUUID;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@ClassDescription(author = "肖炯",
		date = "2021/08/24",
		description = "NIO Socket客户端")
@Getter
public class ClientHandler extends TimerTask {
	//日志
	private static Logger log=Logger.create(ClientHandler.class);

	protected String uuid=null;//唯一ID
	protected String host;
	protected int port;
	protected SocketChannel socketChannel;
	protected Socket socket;
	protected Map<Integer, Object> socketOptions;
	protected ByteBuffer readBuff;
	protected ByteBuffer writeBuff;
	protected ConcurrentMap<Long, DataPackage> packages =new ConcurrentMap<>();//收到的数据包
	protected long maxIdle;//最大空闲时间，超过自动关闭连接，单位ms
	protected Object[] args;//自定义参数
	protected long createAt;
	protected long lastActive;//最近交互时间，单位ms
	protected boolean ended =false;
	protected boolean connected=false;
	protected long lastHeartbeat=0;

	//可执行的操作，key：read/write，val：true/false
	protected ConcurrentMap<String, Boolean> operations=new ConcurrentMap<>();

	//selector
	protected Selector selectorForReader;

	//定时执行服务
	protected ScheduledExecutorService scheduledExecutorService;

	/**
	 *
	 */
	protected ClientHandler(){

	}

	/**
	 *
	 * @param host
	 * @param port
	 * @param socketOptions
	 * @param args
	 */
	public ClientHandler(String host, int port, Map<Integer, Object> socketOptions, Object[] args) {
		this.host=host;
		this.port=port;
		this.socketOptions=socketOptions==null?SocketOptions.cloneDefaults():socketOptions;
		this.maxIdle=(Long)this.socketOptions.get(SocketOptions.SO_MAX_IDLE);
		this.args=args;
		this.lastActive=SysUtil.getNow();
		this.createAt=SysUtil.getNow();
		this.uuid=JUtilUUID.genUUIDShort();
		this.initBuffer();
		this.setReadable(false);
		this.setWritable(false);

		this.startup();
	}

	/**
	 *
	 */
	private void startup(){
		try{
			//读线程
			this.selectorForReader = Selector.open();

			//定时任务（尝试读）
			this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
			this.scheduledExecutorService.scheduleAtFixedRate(this,100,(Integer)this.getSocketOptions().get(SocketOptions.CLIENT_SELECTOR_EXECUTE_INTERVAL), TimeUnit.MICROSECONDS);

			//连接
			this.connect();
		}catch(Exception e){}
	}

	/**
	 *
	 * @return
	 */
	public boolean isConnected(){
		return this.connected;
	}

	/**
	 *
	 * @param readable
	 */
	public void setReadable(boolean readable){
		operations.put("read", readable);
	}

	/**
	 *
	 * @return
	 */
	public boolean getReadable(){
		return operations.get("read");
	}

	/**
	 *
	 * @param writable
	 */
	public void setWritable(boolean writable){
		operations.put("write", writable);
	}

	/**
	 *
	 * @return
	 */
	public boolean getWritable(){
		return operations.get("write");
	}

	/**
	 *配套的数据片段大小
	 * @return
	 */
	public int getSegmentSize(){
		return (Integer)socketOptions.get(java.net.SocketOptions.SO_SNDBUF);
	}

	/**
	 * 配套的DataSource的block大小（数据片段大小 - 22个字节的数据包描述信息）
	 * @return
	 */
	public int getDataSourceBlockSize(){
		return this.getSegmentSize() - 22;
	}

	/**
	 * 超时时间
	 * @return
	 */
	public int getSoTimeout(){
		return (Integer)socketOptions.get(java.net.SocketOptions.SO_TIMEOUT);
	}

	/**
	 *
	 */
	public void setLastActive(){
		this.lastActive=SysUtil.getNow();
	}


	/**
	 * 初始化读写缓存
	 */
	public void initBuffer(){
		readBuff=ByteBuffer.allocate(this.getSegmentSize());
		writeBuff=ByteBuffer.allocate(this.getSegmentSize());
	}

	/**
	 *
	 * @return
	 */
	public String getUuid() {
		return this.uuid;
	}

	/**
	 * 当连接上时
	 * @throws Exception
	 */
	public void onConnect() throws Exception{
		if(this.connected || this.ended) return;

		log.log("["+getUuid()+"] 与服务器连接成功 -> "+this.port, -1);

		this.connected=true;
		this.lastHeartbeat=SysUtil.getNow();
	}

	/**
	 *
	 * @param dataPackage
	 * @param segment
	 * @throws Exception
	 */
	public void onReceive(DataPackage dataPackage, DataSegment segment, boolean isFirstSegment) throws Exception{
		//如果数据已经接收完毕
		if(dataPackage.isCompleted()){
			packages.remove(dataPackage.getId());

			//byte[] all=dataPackage.getData();
			//log.log("get package of "+dataPackage.getId()+" -> "+ new String(all, StandardCharsets.UTF_8), -1);
		}
	}

	/**
	 * 当关闭连接时
	 * @throws Exception
	 */
	public void onClose() throws Exception{
		this.connected=false;
		//if(socket!=null) log.log("["+getUuid()+"] 关闭连接：("+socket.getInetAddress().getHostAddress()+":"+socket.getLocalPort()+","+socket.getPort()+","+getUuid()+")", -1);
		//else log.log("["+getUuid()+"] 关闭连接：("+getUuid()+")", -1);
	}

	/**
	 * 当发生异常时
	 * @throws Exception
	 */
	public void onError() throws Exception{
		if(socket!=null) log.log("["+getUuid()+"] 连接异常：("+socket.getInetAddress().getHostAddress()+":"+socket.getLocalPort()+","+socket.getPort()+","+getUuid()+")", -1);
		else log.log("["+getUuid()+"] 连接异常：("+getUuid()+")", -1);
	}

	/**
	 * 未接收完毕数据片段，key：packageId
	 */
	protected ConcurrentMap<Long, DataSegmentBuffer> dataSegmentBuffers = new ConcurrentMap<>();

	/**
	 * 当前处理的数据包ID
	 */
	protected Long currentPackageId=null;

	/**
	 * 当前收到的，还不能确定所归属数据包的字节
	 */
	protected byte[] pendingBytes=null;

	/**
	 * 解析数据
	 * @param data
	 * @throws Exception
	 */
	private void parseData(byte[] data) throws Exception {
		//无数据
		if (data == null || data.length == 0) return;

		//附加上次待定字节
		if (this.pendingBytes != null && this.pendingBytes.length > 0) {
			data = JArray.append(this.pendingBytes, data);
			this.pendingBytes = null;
		}

		for (int i = 0; i < data.length; i++) {
			//最后一个字节，且等于开始标记的第一个字节，且不属于当前正在处理的数据片段，有可能是数据片段的开始标记被一分为二，故暂存，等下次收到数据再一起处理
			if (i == data.length - 1 && data[i] == Protocol.J_DATA_SEGMENT_START[0] && this.currentPackageId == null) {
				this.pendingBytes = new byte[]{data[i]};
				return;
			}

			//是否为两个字节的开始标记
			boolean isStartTag = data[i] == Protocol.J_DATA_SEGMENT_START[0] && i < data.length - 1 && data[i + 1] == Protocol.J_DATA_SEGMENT_START[1];

			//一个数据片段的开始
			if (isStartTag) {
				if (data.length - i < 22) {//数据片段头信息不完整，下次再处理
					this.currentPackageId = null;
					this.pendingBytes = JArray.sub(data, i, data.length);
					break;
				}

				//数据包ID
				long packageId = JUtilMath.eightBytesToLong(JArray.sub(data, i + 2, i + 10), false);

				//无效数据（数据包ID必须为正数）
				if (packageId <= 0) return;

				//数据包大小
				long total = JUtilMath.eightBytesToLong(JArray.sub(data, i + 10, i + 18), false);

				//当前数据片段大小
				int dataLength = JUtilMath.fourBytesToInt(JArray.sub(data, i + 18, i + 22), false);
				if (dataLength <= 0) return;

				this.currentPackageId = packageId;

				DataSegmentBuffer buffer = this.dataSegmentBuffers.get(packageId);
				if (buffer == null) {
					buffer = new DataSegmentBuffer(packageId, total, dataLength, getReadTimeout());
					this.dataSegmentBuffers.put(packageId, buffer);
				}

				int appendToIndex = Math.min(i + 22 + dataLength, data.length);
				buffer.append(JArray.sub(data, i, appendToIndex));

				if (buffer.isCompleted()) {
					if (Nvwa.isDebug()) {
						//log.log("[client]completed data segment of package => "+packageId + ", dataLength => " +dataLength+" => "+new String(JArray.sub(buffer.getData(), 22, buffer.getData().length), "UTF-8"), -1);
					}
					this.currentPackageId = null;
					this.processSegments();
				} else {
					if (Nvwa.isDebug()) {
						//log.log("[client]get data segment of package => "+packageId + ", all => "+data.length+", appendFromIndex => "+i+", appendToIndex => "+appendToIndex+", dataLength => " +dataLength+", got => "+(appendToIndex - i - 22)+" => "+new String(JArray.sub(data, i+22, appendToIndex), "UTF-8"), -1);
					}
				}

				i = appendToIndex - 1;
				continue;
			}

			//不是片段开始标记，且上一个数据片段缓存尚未接收到完整数据（如接收完毕则currentPackageId为null），故接下来的数据归属于上一个数据片段缓存
			if (this.currentPackageId != null && this.dataSegmentBuffers.containsKey(this.currentPackageId)) {
				DataSegmentBuffer buffer = this.dataSegmentBuffers.get(this.currentPackageId);

				int to = i;
				for (to = i; to < data.length; to++) {
					isStartTag = data[to] == Protocol.J_DATA_SEGMENT_START[0] && to < data.length - 1 && data[to + 1] == Protocol.J_DATA_SEGMENT_START[1];

					//如果to ~ to+1是一个开始标记
					if (isStartTag) break;

					//数据已经达到指定长度
					if (buffer.toCompleted(to - i)) break;
				}

				if (to > i) {
					buffer.append(JArray.sub(data, i, to));
					if (Nvwa.isDebug()) {
						//log.log("[client]追加数据到上一个数据片段，package => "+this.currentPackageId + ", dataLength => " +buffer.getLength()+" => "+new String(JArray.sub(data, i, to), "UTF-8"), -1);
					}
				}

				if (buffer.isCompleted()) {
					if (Nvwa.isDebug()) {
						//log.log("[client]completed(分多次接收) data segment of package => "+this.currentPackageId + ", dataLength => " +buffer.getLength()+" => "+new String(JArray.sub(buffer.getData(), 22, buffer.getData().length), "UTF-8"), -1);
					}
					this.currentPackageId = null;
					this.processSegments();
				}

				i = to - 1;
				continue;
			}

			//不是片段开始标记，也不属于上一个尚未接收完毕的数据片段，这样的数据是未知的，无法处理的数据
			if (Nvwa.isDebug()) {
				int to = i;
				for (to = i; to < data.length; to++) {
					isStartTag = data[to] == Protocol.J_DATA_SEGMENT_START[0] && to < data.length - 1 && data[to + 1] == Protocol.J_DATA_SEGMENT_START[1];

					//如果to ~ to+1是一个开始标记
					if (isStartTag) break;
				}

				//log.log("[client]收到未知数据 => "+this.currentPackageId + ", length => " +data.length+", 范围 => ("+i+","+to+") => "+new String(JArray.sub(data, i, to), "UTF-8"), -1);
				i = to - 1;
			}
		}
	}

	/**
	 *
	 * @throws Exception
	 */
	private void processSegments() throws Exception{
		List<DataSegmentBuffer> buffers = this.dataSegmentBuffers.listValues();
		for(DataSegmentBuffer buffer : buffers){
			//未接收完毕
			if(!buffer.isCompleted()) continue;
			this.dataSegmentBuffers.remove(buffer.getPackageId());

			Long packageId = buffer.getPackageId();

			DataSegment segment = null;
			DataPackage dataPackage = this.packages.get(packageId);
			if (dataPackage == null) {//新请求的第一个数据片段
				dataPackage = new DataPackage(packageId, buffer.getTotal(), this.getSegmentSize());
				dataPackage.setFrom(((InetSocketAddress) this.socketChannel.getRemoteAddress()).getHostString());
				this.packages.put(packageId, dataPackage);

				//解析数据片段
				segment = DataSegment.parse(buffer);

				if(Nvwa.isDebug()){
					//log.log("[client]process data segment(the first one) of package => "+packageId + " => "+new String(segment.getData(), "UTF-8"), -1);
				}

				//记录收到数据片段
				dataPackage.onSegment(segment);

				//业务逻辑（不抛出异常）
				try {
					this.onReceive(dataPackage, segment, true);
				} catch (Exception e) {
					log.log(e, Logger.LEVEL_ERROR);
				}
			} else if (!dataPackage.isInterrupted()) {//非第一个片段且未被中断
				//解析数据片段
				segment = DataSegment.parse(buffer);

				if(Nvwa.isDebug()){
					//log.log("[client]process data segment of package => "+packageId + " => "+new String(segment.getData(), "UTF-8"), -1);
				}

				//记录收到数据片段（该默认实现不保存数据）
				dataPackage.onSegment(segment);

				//业务逻辑（不抛出异常）
				try {
					this.onReceive(dataPackage, segment, false);
				} catch (Exception e) {
					log.log(e, Logger.LEVEL_ERROR);
				}
			}
		}
	}

	/**
	 * 接收客户端发送过来的数据
	 * @return
	 */
	public void receive(){
		if(readBuff==null) return;//已经停止
		synchronized(readBuff) {
			try {
				//已经关闭
				if (this.ended || socketChannel == null) return;

				//读取的字节数
				socketChannel.read(readBuff);
				if(readBuff.position()<=0) return;//没读到东西

				setLastActive();

				byte[] data = JArray.sub(readBuff.array(), 0, readBuff.position());
				readBuff.clear();
				if(Nvwa.isDebug()){
					//log.log("nio client received("+this.getSegmentSize()+", "+readBuff.position()+") -> "+new String(data, "UTF-8"), -1);
				}

				//处理数据
				this.parseData(data);

				//处理缓存的数据片段
				this.processSegments();
			} catch (Exception e) {
				if(Nvwa.isDebug()) {
					log.log("客户端接受数据时出现错误（" + e.getMessage() + "）：", Logger.LEVEL_ERROR);
					log.log(e, Logger.LEVEL_ERROR);
				}
				this.end(true);
			}
		}
	}

	/**
	 *
	 * @param dataPackage
	 * @throws Exception
	 */
	public void sendDataPackage(DataPackage dataPackage) throws Exception{
		byte[] tobeSentBytes=dataPackage.send();
		while(tobeSentBytes != null){
			sendBytes(tobeSentBytes);
			tobeSentBytes=dataPackage.send();
		}
	}

	/**
	 *
	 * @param dataSegment
	 * @throws Exception
	 */
	public void sendSegment(DataSegment dataSegment) throws Exception{
		this.sendBytes(dataSegment.assemble());
	}

	/**
	 * 将多个数据片段合并多一起发送，每次发送一个等于缓存区大小的数据段
	 * @param segments
	 * @throws Exception
	 */
	public void sendSegments(List<DataSegment> segments) throws Exception{
		if(segments==null || segments.isEmpty()) return;
		byte[] source = null;
		for(DataSegment s : segments){
			source = JArray.append(source, s.assemble());
		}
		int blocks=(source.length/this.getSegmentSize() + (source.length%this.getSegmentSize()==0?0:1));

		int cursor=0;
		while(cursor < blocks){
			byte[] block=null;
			if(cursor < blocks -1){//不是最后一块
				block = JArray.sub(source, cursor*this.getSegmentSize() , (cursor+1)*this.getSegmentSize());
			}else{
				block = JArray.sub(source, cursor*this.getSegmentSize() , cursor*this.getSegmentSize() +(source.length%this.getSegmentSize()));
			}
			cursor++;
			this.sendBytes(block);
		}
	}

	/**
	 *
	 * @param tobeSentBytes
	 * @throws Exception
	 */
	public void sendBytes(byte[] tobeSentBytes) throws Exception{
		if(writeBuff==null) return;//已经停止
		synchronized(writeBuff) {
			writeBuff.clear();
			writeBuff.put(tobeSentBytes);
			doSend();
		}
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	private boolean doSend() throws Exception{
		//TODO
		while(!this.isConnected()) Global.sleep10Millis();

		writeBuff.flip();
		while(writeBuff.hasRemaining()) socketChannel.write(writeBuff);
		return true;
	}

	/**
	 *
	 */
	synchronized private void connect() throws Exception{
		socketChannel = SocketChannel.open();

		//设置SocketChannel参数
		int soLinger = (Integer) this.socketOptions.get(java.net.SocketOptions.SO_LINGER);
		socketChannel.setOption(StandardSocketOptions.SO_LINGER, soLinger);
		socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, (Boolean) this.socketOptions.get(java.net.SocketOptions.SO_REUSEADDR));
		socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, (Boolean) this.socketOptions.get(java.net.SocketOptions.TCP_NODELAY));
		socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, (Integer) this.socketOptions.get(java.net.SocketOptions.SO_SNDBUF));
		socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, (Integer) this.socketOptions.get(java.net.SocketOptions.SO_RCVBUF));
		socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, (Boolean) this.socketOptions.get(java.net.SocketOptions.SO_KEEPALIVE));
		//设置SocketChannel参数 end

		socketChannel.connect(new InetSocketAddress(this.host, this.port));
		socketChannel.configureBlocking(false);
		socket = socketChannel.socket();

		//设置socket参数
		socket.setTcpNoDelay((Boolean) this.socketOptions.get(java.net.SocketOptions.TCP_NODELAY));
		if (soLinger >= 0) socket.setSoLinger(true, soLinger);
		else socket.setSoLinger(false, 0);

		socket.setSoTimeout((Integer) this.socketOptions.get(java.net.SocketOptions.SO_TIMEOUT));

		if(Nvwa.isDebug()){
			log.log("[client]setSendBufferSize => "+this.socketOptions.get(java.net.SocketOptions.SO_SNDBUF),-1);
		}
		socket.setSendBufferSize((Integer) this.socketOptions.get(java.net.SocketOptions.SO_SNDBUF));

		if(Nvwa.isDebug()){
			log.log("[client]setReceiveBufferSize => "+this.socketOptions.get(java.net.SocketOptions.SO_RCVBUF),-1);
		}
		socket.setReceiveBufferSize((Integer) this.socketOptions.get(java.net.SocketOptions.SO_RCVBUF));

		socket.setKeepAlive((Boolean) this.socketOptions.get(java.net.SocketOptions.SO_KEEPALIVE));
		socket.setOOBInline((Boolean) this.socketOptions.get(java.net.SocketOptions.SO_OOBINLINE));

		if(Nvwa.isDebug()) {
			log.log("[client]connection established on port " + this.getPort() + " in nio mode => (SO_SNDBUF=" + socket.getSendBufferSize() + ", SO_RCVBUF=" + socket.getReceiveBufferSize() + ").", -1);
		}

		//注册Selector事件
		this.socketChannel.register(this.selectorForReader, SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
	}

	/**
	 * 重连
	 * @throws Exception
	 */
	public void reconnect() throws Exception{
		if(!this.connected || this.ended) return;

		log.log("reconnecting.....", -1);
		try{
			this.disconnect();
		}catch (Exception ex){}

		try{
			Thread.sleep(1000);
		}catch(Exception ex){}

		this.connect();
	}

	/**
	 * 心跳
	 * @throws Exception
	 */
	public void heartbeat() throws Exception{
		if(SysUtil.getNow() - this.lastHeartbeat < 5000) return;

		try{
			this.lastHeartbeat=SysUtil.getNow();
			this.sendSegment(new DataSegment(this.getSegmentSize(), Long.MAX_VALUE, 0, Protocol.J_HEARTBEAT));
		}catch(Exception e){
			log.log("客户端发送心跳时出错（"+e.getMessage()+"）：", Logger.LEVEL_ERROR);
			log.log(e, Logger.LEVEL_ERROR);
			this.end(true);
		}
	}

	/**
	 * 是否超过最大空闲时间
	 * @return
	 */
	public boolean isIdle() {
		return this.maxIdle>0 && SysUtil.getNow()-this.lastActive>this.maxIdle;
	}

	/**
	 *
	 * @return
	 */
	public long getReadTimeout(){
		return (Integer)socketOptions.get(java.net.SocketOptions.SO_TIMEOUT);
	}

	/**
	 * 清理无效数据包（已中断的、读取超时的）
	 */
	public void clearInvalidPackages(){
		for(Iterator<Long> it=this.packages.keySet().iterator(); it.hasNext();){
			Long key=it.next();
			DataPackage dataPackage=this.packages.get(key);
			if(dataPackage==null) continue;

			if(dataPackage.isInterrupted()
					|| dataPackage.isTimeout(this.getReadTimeout())){
				//log.log("remove data package "+dataPackage.getId()+","+dataPackage.isInterrupted()+","+dataPackage.isTimeout(this.getReadTimeout()), -1);
				this.removePackage(key);
			}
		}
	}

	/**
	 * 清除过期数据片段
	 */
	public void clearTimeoutDataSegments(){
		//清空超时未完成传输的数据包
		List<DataSegmentBuffer> buffers = this.dataSegmentBuffers.listValues();
		for(DataSegmentBuffer buffer : buffers){
			if(buffer.isTimeout()){
				buffer.destroy();
				this.dataSegmentBuffers.remove(buffer.getPackageId());
				this.removePackage(buffer.getPackageId());
			}
		}
	}

	/**
	 * 移除数据包
	 * @param packageId
	 */
	private void removePackage(Long packageId){
		this.packages.remove(packageId);
		if(this.currentPackageId!=null && this.currentPackageId==packageId) this.currentPackageId=null;
	}

	/**
	 *
	 * @param force 是否强制关闭
	 * @return
	 */
	synchronized public boolean end(boolean force) {
		if(this.ended) return true;

		if(!this.isIdle()
				&&!force) return false;//未满足需关闭连接的条件，且不是强制关闭

		this.ended = true;

		try{
			this.packages.clear();
			this.readBuff.clear();
			this.writeBuff.clear();
		}catch(Exception e) {
			//log.log(e, Logger.LEVEL_ERROR);
		}

		try {
			this.shutdown();
		}catch(Exception e) {
			//log.log(e, Logger.LEVEL_ERROR);
		}

		try {
			this.socketChannel.close();
			//this.socketChannel=null;
		}catch(Exception e) {
			//log.log(e, Logger.LEVEL_ERROR);
		}

		try {
			this.socket.close();
			//this.socket=null;
		}catch(Exception e) {
			//log.log(e, Logger.LEVEL_ERROR);
		}

		try {
			//log.log("["+getUuid()+"] 强制关闭连接：(this.isIdle():"+this.isIdle()+",force:"+force+")", -1);
			this.onClose();
		}catch(Exception ex) {
			//log.log(ex, Logger.LEVEL_ERROR);
		}

		return true;
	}

	/**
	 * 断开当前连接
	 */
	synchronized private void disconnect() {
		this.connected = false;

		try {
			this.socketChannel.close();
			this.socketChannel = null;
		} catch (Exception e) {
			//log.log(e, Logger.LEVEL_ERROR);
		}

		try {
			this.socket.close();
			this.socket = null;
		} catch (Exception e) {
			//log.log(e, Logger.LEVEL_ERROR);
		}

		this.setReadable(false);
		this.setWritable(false);
	}

	/**
	 *
	 * @param selector
	 */
	protected void reading(Selector selector){
		try {
			if (selector == null || this.ended) return;

			int readyNum = selector.selectNow();
			if (readyNum == 0) return;

			Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
			while(keys.hasNext()) {
				SelectionKey key = keys.next();
				keys.remove();
				if(!key.isValid()) continue;//key已经失效

				if(key.isReadable()) this.setReadable(true);

				try{
					if(key.isWritable() || key.isConnectable()) {
						this.setWritable(true);
						this.onConnect();
					}
				}catch(Exception ignored){}

				if(key.isReadable()) this.receive();
			}
		} catch (Exception e) {
			log.log(e, Logger.LEVEL_ERROR);
		}
	}
	/**
	 *
	 */
	protected void shutdown(){
		try{
			this.scheduledExecutorService.shutdownNow();
		}catch(Exception ex){}

		try{
			selectorForReader.close();
		}catch(Exception ex){}
	}

	@Override
	public void run() {
		this.reading(this.selectorForReader);
	}
}