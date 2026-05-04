package j.core.nio;

import j.core.common.JArray;
import j.core.nvwa.Nvwa;
import j.core.nvwa.NvwaAncestor;
import j.core.sys.SysUtil;
import j.log.Logger;
import j.util.*;
import lombok.Getter;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * 
 * @author 肖炯
 *
 * 2019年3月30日
 *
 * <b>功能描述</b> 当启动一个Server在指定端口监听客户端连接时，必须指定一个类来处理特定业务相关的客户端交互，该类必须是ServerHandler的子类
 */
@Getter
public class ServerHandler extends NvwaAncestor {
	private static Logger log=Logger.create(ServerHandler.class);

	protected String uuid=null;//唯一ID
	protected Map<Integer, Object> socketOptions;
	protected SelectionKey selectKey =null;
	protected Server server=null;
	protected ServerExecutor reader =null;
	protected ServerExecutor writer =null;
	protected SocketChannel socketChannel;
	protected Socket socket;
	protected InetAddress addr;
	protected long maxIdle;//最大空闲时间，超过自动关闭连接，单位ms
	protected long mustSendAfterConnectedWithin;//建立连接后多久内必须发生交互，否则关闭连接，单位ms
	protected Object[] args;//自定义参数
	protected long createAt;
	protected long lastActive;//最近交互时间，单位ms
	protected long interactions=0;//交互次数
	protected long lastValidCommunication=0;//最近有效交互时间
	protected ConcurrentMap<Long, DataPackage> packages =new ConcurrentMap<>();//收到的数据包
	protected ByteBuffer readBuff=null;
	protected ByteBuffer writeBuff=null;
	protected boolean ended=false;//是否已经结束

	//可执行的操作，key：read/write，val：true/false
	protected ConcurrentMap<String, Boolean> operations=new ConcurrentMap<>();

	public ServerHandler(){
		this.uuid=JUtilUUID.genUUID();
	}

	/**
	 *
	 * @param socketChannel
	 * @param socket
	 * @param socketOptions 连接参数
	 * @param args 业务自定义参数
	 */
	public ServerHandler(SocketChannel socketChannel, Socket socket, Map<Integer, Object> socketOptions, Object[] args) {
		this.uuid=JUtilUUID.genUUID();
		this.lastActive=SysUtil.getNow();
		this.createAt=SysUtil.getNow();

		this.socketChannel=socketChannel;
		this.socket=socket;
		this.socketOptions=socketOptions==null?SocketOptions.cloneDefaults():socketOptions;
		this.addr=socket.getInetAddress();
		this.mustSendAfterConnectedWithin=(Long)this.socketOptions.get(SocketOptions.SO_MUST_SEND_AFTER_CONN_WITH_IN);
		this.maxIdle=(Long)this.socketOptions.get(SocketOptions.SO_MAX_IDLE);
		this.args=args;
		this.initBuffer();
		this.setReadable(false);
		this.setWritable(false);
		this.reader = new ServerExecutor(this, "read");
		this.writer = new ServerExecutor(this, "write");
	}

	/**
	 *
	 * @param executor
	 * @param operation
	 */
	public void execute(ExecutorService executor, String operation){
		executor.execute("read".equals(operation) ? this.reader : this.writer);
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
	 *
	 * @return
	 */
	public int getReadTimeout(){
		return (Integer)socketOptions.get(java.net.SocketOptions.SO_TIMEOUT);
	}

	/**
	 * 数据片段（tcp缓冲区大小）
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
	 * 初始化读写缓存
	 */
	public void initBuffer(){
		readBuff=ByteBuffer.allocate(this.getSegmentSize());
		writeBuff=ByteBuffer.allocate(this.getSegmentSize());
	}

	/**
	 *
	 * @param server
	 */
	public void setServer(Server server) {
		this.server=server;
	}

	/**
	 *
	 * @param selectKey
	 */
	public void setSelectKey(SelectionKey selectKey) {
		this.selectKey = selectKey;
	}

	/**
	 *
	 * @return
	 */
	public String getUuid() {
		return this.uuid;
	}

	/**
	 * 获得区分于其它客户端的ID，特定业务中可能需要根据此ID来获得此Client对象，并通过其与客户端进行交互
	 * @return
	 */
	public String getId() {
		return this.addr.getHostAddress();
	}

	/**
	 *
	 * @return
	 */
	public InetAddress getAddress() {
		return this.addr;
	}

	/**
	 *
	 * @return
	 */
	public SocketChannel getSocketChannel() {
		return socketChannel;
	}

	/**
	 *
	 * @return
	 */
	public Socket getSocket() {
		return socket;
	}

	/**
	 * 当连接上时
	 * @throws Exception
	 */
	public void onConnect() throws Exception{
	}

	/**
	 * 当前处理中的请求
	 * @return
	 */
	public int getPackages(){
		return this.packages.size();
	}

	/**
	 *
	 */
	public void setLastValidCommunication() {
		this.lastValidCommunication=SysUtil.getNow();
	}

	/**
	 *
	 * @return
	 */
	public long getLastValidCommunication() {
		return this.lastValidCommunication;
	}

	/**
	 *
	 * @return
	 */
	public long getCreateAt() {
		return createAt;
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

			if(Nvwa.isDebug()){
				byte[] all=dataPackage.getData();
				log.log("get package of "+dataPackage.getId()+" -> "+all.length+" -> "+ new String(all, "UTF-8"), -1);
			}

			//响应
			DataPackage response=new DataPackage(dataPackage.getId(), new DataSourceString(this.getDataSourceBlockSize(), "UTF-8").setSource("You are welcome."), this.getSegmentSize());
			sendDataPackage(response);
		}
	}

	/**
	 * 当关闭连接时
	 * @throws Exception
	 */
	public void onClose() throws Exception{
		if(Nvwa.isDebug()) {
			log.log("服务端关闭连接：("+socket.getInetAddress().getHostAddress()+":"+socket.getLocalPort()+","+socket.getPort()+","+getUuid()+")", -1);
		}
	}

	/**
	 * 当发生异常时
	 * @throws Exception
	 */
	public void onError() throws Exception{
		if(Nvwa.isDebug()) {
			log.log("服务端连接异常：("+addr.getHostAddress()+":"+socket.getLocalPort()+","+socket.getPort()+","+getUuid()+") 是否空闲:"+isIdle()+", 连接后是否超时未收到数据："+notActiveAfterConnectedWithin(), -1);
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
		if(this.ended) return false;

		//TODO
		//while(!this.getWritable()) Global.sleep1Millis();

		writeBuff.flip();
		while(writeBuff.hasRemaining()) socketChannel.write(writeBuff);
		return true;
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
	private void parseData(byte[] data) throws Exception{
		//无数据
		if(data==null || data.length==0) return;

		//附加上次待定字节
		if(this.pendingBytes!=null && this.pendingBytes.length>0){
			data = JArray.append(this.pendingBytes, data);
			this.pendingBytes=null;
		}

		for(int i=0; i<data.length; i++){
			//最后一个字节，且等于开始标记的第一个字节，且不属于当前正在处理的数据片段，有可能是数据片段的开始标记被一分为二，故暂存，等下次收到数据再一起处理
			if(i==data.length-1 && data[i]==Protocol.J_DATA_SEGMENT_START[0] && this.currentPackageId==null){
				this.pendingBytes=new byte[]{data[i]};
				return;
			}

			//是否为两个字节的开始标记
			boolean isStartTag = data[i]==Protocol.J_DATA_SEGMENT_START[0] && i<data.length-1 && data[i+1]==Protocol.J_DATA_SEGMENT_START[1];

			//一个数据片段的开始
			if(isStartTag){
				if(data.length - i < 22){//数据片段头信息不完整，下次再处理
					this.currentPackageId = null;
					this.pendingBytes = JArray.sub(data, i, data.length);
					break;
				}

				//数据包ID
				long packageId = JUtilMath.eightBytesToLong(JArray.sub(data, i+2, i+10), false);

				//无效数据（数据包ID必须为正数）
				if(packageId <= 0) return;

				//数据包大小
				long total = JUtilMath.eightBytesToLong(JArray.sub(data, i+10, i+18), false);

				//当前数据片段大小
				int dataLength = JUtilMath.fourBytesToInt(JArray.sub(data, i+18, i+22), false);
				if(dataLength <= 0) return;

				this.currentPackageId = packageId;

				DataSegmentBuffer buffer = this.dataSegmentBuffers.get(packageId);
				if(buffer==null){
					buffer=new DataSegmentBuffer(packageId, total, dataLength, getReadTimeout());
					this.dataSegmentBuffers.put(packageId, buffer);
				}

				int appendToIndex=Math.min(i+22+dataLength, data.length);
				buffer.append(JArray.sub(data, i, appendToIndex));

				if(buffer.isCompleted()){
					if(Nvwa.isDebug()){
						//log.log("[server]completed data segment of package => "+packageId + ", dataLength => " +dataLength+" => "+new String(JArray.sub(buffer.getData(), 22, buffer.getData().length), "UTF-8"), -1);
					}
					this.currentPackageId=null;
					this.processSegments();
				}else{
					if(Nvwa.isDebug()){
						//log.log("[server]get data segment of package => "+packageId + ", all => "+data.length+", appendFromIndex => "+i+", appendToIndex => "+appendToIndex+", dataLength => " +dataLength+", got => "+(appendToIndex - i - 22)+" => "+new String(JArray.sub(data, i+22, appendToIndex), "UTF-8"), -1);
					}
				}

				i = appendToIndex - 1;
				continue;
			}

			//不是片段开始标记，且上一个数据片段缓存尚未接收到完整数据（如接收完毕则currentPackageId为null），故接下来的数据归属于上一个数据片段缓存
			if(this.currentPackageId!=null && this.dataSegmentBuffers.containsKey(this.currentPackageId)){
				DataSegmentBuffer buffer = this.dataSegmentBuffers.get(this.currentPackageId);

				int to = i;
				for(to=i; to<data.length; to++){
					isStartTag = data[to]==Protocol.J_DATA_SEGMENT_START[0] && to<data.length-1 && data[to+1]==Protocol.J_DATA_SEGMENT_START[1];

					//如果to ~ to+1是一个开始标记
					if(isStartTag) break;

					//数据已经达到指定长度
					if(buffer.toCompleted(to - i)) break;
				}

				if(to>i){
					buffer.append(JArray.sub(data, i, to));
					if(Nvwa.isDebug()){
						//log.log("[server]追加数据到上一个数据片段，package => "+this.currentPackageId + ", dataLength => " +buffer.getLength()+" => "+new String(JArray.sub(data, i, to), "UTF-8"), -1);
					}
				}

				if(buffer.isCompleted()){
					if(Nvwa.isDebug()){
						//log.log("[server]completed(分多次接收) data segment of package => "+this.currentPackageId + ", dataLength => " +buffer.getLength()+" => "+new String(JArray.sub(buffer.getData(), 22, buffer.getData().length), "UTF-8"), -1);
					}
					this.currentPackageId=null;
					this.processSegments();
				}

				i = to - 1;
				continue;
			}

			//不是片段开始标记，也不属于上一个尚未接收完毕的数据片段，这样的数据是未知的，无法处理的数据
			if(Nvwa.isDebug()){
				int to = i;
				for(to=i; to<data.length; to++){
					isStartTag = data[to]==Protocol.J_DATA_SEGMENT_START[0] && to<data.length-1 && data[to+1]==Protocol.J_DATA_SEGMENT_START[1];

					//如果to ~ to+1是一个开始标记
					if(isStartTag) break;
				}

				//log.log("[server]收到未知数据 => "+this.currentPackageId + ", length => " +data.length+", 范围 => ("+i+","+to+") => "+new String(JArray.sub(data, i, to), "UTF-8"), -1);
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

				//是否心跳
				boolean isHeartbeat = JUtilBytes.equals(segment.getData(), Protocol.J_HEARTBEAT);
				if(isHeartbeat){
					buffer.destroy();
					continue;
				}

				if(Nvwa.isDebug()){
					//log.log("[server]process data segment(the first one) of package => "+packageId + " => "+new String(segment.getData(), "UTF-8"), -1);
				}

				//记录收到数据片段
				dataPackage.onSegment(segment);

				//业务逻辑（不抛出异常）
				try {
					this.onReceive(dataPackage, segment, true);
				} catch (Exception e) {
					log.log(e, Logger.LEVEL_ERROR);
				}
			} else if(!dataPackage.isInterrupted()) {//非第一个片段且未被中断
				//解析数据片段
				segment = DataSegment.parse(buffer);

				//是否心跳
				boolean isHeartbeat = JUtilBytes.equals(segment.getData(), Protocol.J_HEARTBEAT);
				if(isHeartbeat){
					buffer.destroy();
					continue;
				}

				if(Nvwa.isDebug()){
					//log.log("[server]process data segment of package => "+packageId + " => "+new String(segment.getData(), "UTF-8"), -1);
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

			buffer.destroy();

			//超出允许的数据包最大字节数
			if (dataPackage.getFinished() > this.server.getMaxDataPackageSize()) {
				throw new Exception("Data package over the max size: " + this.server.getMaxDataPackageSize());
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
				if(this.ended) return;

				//读取的字节数
				socketChannel.read(readBuff);
				if(readBuff.position()<=0) return;//没读到东西

				setLastActive();

				byte[] data = JArray.sub(readBuff.array(), 0, readBuff.position());
				if(Nvwa.isDebug()){
					//log.log("nio server received("+this.getSegmentSize()+", "+readBuff.position()+") -> "+new String(data, "UTF-8"), -1);
				}
				readBuff.clear();

				//处理数据
				this.parseData(data);

				//处理缓存的数据片段
				this.processSegments();
			} catch (Exception e) {
				if(Nvwa.isDebug()) {
					log.log("服务端处理请求时出现错误（" + e.getMessage() + "）：", Logger.LEVEL_ERROR);
					log.log(e, Logger.LEVEL_ERROR);
				}
				this.end(true);
			}
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
	 * 是否建立连接后在规定时间内未发生交互
	 * @return
	 */
	public boolean notActiveAfterConnectedWithin() {
		return interactions==0
				&&mustSendAfterConnectedWithin>0
				&&SysUtil.getNow()-this.lastActive>this.mustSendAfterConnectedWithin;
	}

	/**
	 * 最近活动时间
	 */
	public void setLastActive(){
		this.lastActive=SysUtil.getNow();
		if(this.interactions==Long.MAX_VALUE) this.interactions=1;
		this.interactions++;
	}

	/**
	 * 清理无效数据包
	 */
	public void clearInvalidPackages(){
		ConcurrentList<Long> keys=this.packages.listKeys();
		for(int i=0; i<keys.size(); i++){
			Long key=keys.get(i);
			DataPackage dataPackage=this.packages.get(key);
			if(dataPackage==null) continue;

			if(dataPackage.isInterrupted()
					|| (!dataPackage.isCompleted() && dataPackage.isTimeout(this.getReadTimeout()))){
				this.removePackage(key);
			}
		}
		keys.clear();
		keys=null;
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
				&&!this.notActiveAfterConnectedWithin()
				&&!force) return false;//未满足两个需关闭连接的条件之一，且不是强制关闭

		this.ended =true;

		try{
			this.readBuff.clear();
			this.writeBuff.clear();
			this.readBuff=null;
			this.writeBuff=null;
		}catch(Exception e) {
			//log.log(e, Logger.LEVEL_ERROR);
		}

		if(Nvwa.isDebug()){
			log.log("["+getUuid()+"] 服务端关闭链接（force:"+force+"）", -1);
			if(socket!=null) log.log("["+getUuid()+"] 服务端关闭连接：("+socket.getInetAddress().getHostAddress()+":"+socket.getLocalPort()+","+socket.getPort()+","+getUuid()+")", -1);
			else log.log("["+getUuid()+"] 服务端关闭连接：("+getUuid()+")", -1);
		}

		//停止执行
		reader.end();
		writer.end();

		try {
			this.onClose();
		}catch(Exception ex) {
			log.log(ex, Logger.LEVEL_ERROR);
		}

		if(this.selectKey !=null){
			try{
				this.selectKey.cancel();
				this.selectKey = null;
			}catch(Exception e) {
				//log.log(e, Logger.LEVEL_ERROR);
			}
		}

		try {
			this.socketChannel.close();
			this.socketChannel=null;
		}catch(Exception e) {
			//log.log(e, Logger.LEVEL_ERROR);
		}

		try {
			this.socket.close();
			this.socket=null;
		}catch(Exception e) {
			//log.log(e, Logger.LEVEL_ERROR);
		}

		this.setReadable(false);
		this.setWritable(false);

		//从handler列表中移除
		server.removeHandler(this);

		return true;
	}

	@Override
	public String toString() {
		return this.addr.getHostAddress();
	}
}