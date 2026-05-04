package j.core.nio;

import j.core.annotation.description.ClassDescription;
import j.core.annotation.description.MethodDescription;
import j.core.common.Global;
import j.core.nvwa.Nvwa;
import j.log.Logger;
import j.util.ConcurrentList;
import j.util.ConcurrentMap;
import j.util.JUtilBean;
import j.util.JUtilSorter;
import lombok.Getter;

import java.net.*;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ClassDescription(author = "肖炯",
		date = "2021/08/24",
		description = "NIO Socket服务器")
@Getter
public class Server{
	//日志
	private static final Logger log=Logger.create(Server.class);

	//端口-服务器
	private static ConcurrentMap<Integer, Server> servers=new ConcurrentMap<>();

	//是否调试模式
	private boolean debug=false;

	//端口
	private Integer port;

	//与client交互的server端处理类
	private Class serverSideHandlerClass;

	//socket参数设置
	private Map<Integer, Object> socketOptions;

	//与client交互的server端处理对象
	private ConcurrentMap<String, ServerHandler> serverHandlers = new ConcurrentMap<>();

	//最大允许连接的客户端数
	private int maxClients=100;

	//同一客户端IP上最多允许连接的客户端数
	private int maxClientsPerIp=1;

	//最大数据包（字节），默认10M
	private int maxDataPackageSize=1024*1024*10;

	//应用自定义业务参数
	private Object[] args;

	//channel
	private ServerSocketChannel serverSocketChannel;

	//server socket
	private ServerSocket serverSocket;

	//连接接受线程
	private ServerAcceptor acceptor;

	//Selector组
	private List<ServerSelector> selectors=new ArrayList<>();

	//Selector组排序（每次分配最空闲的Selector）
	private ServerSelectorSorter serverSelectorSorter=new ServerSelectorSorter();

	//读任务执行
	private ExecutorService readers;

	//连接监视器
	private ServerMonitor monitor=null;

	//等待连接超时时间（使用java.net.SocketOptions.SO_TIMEOUT选项设置的值）
	private int acceptWaitTimout=0;

	//是否已经结束运行
	private boolean ended=false;

	/**
	 * 处理中的数据包数（客户端可以根据这个来选择最空闲的服务镜像节点，以实现负载均衡）
	 * @return
	 */
	public int getPackages(){
		int packages=0;
		ConcurrentList<ServerHandler> handlers=serverHandlers.listValues();
		for(int i = 0; i< handlers.size(); i++) {
			ServerHandler c=handlers.get(i);
			if(c!=null) packages+=c.getPackages();
		}
		return packages;
	}

	/**
	 *
	 * @param port 端口
	 * @param serverSideHandlerClass 处理客户端交互的类
	 * @param maxClients 最大同时连接客户端数
	 * @param maxClientsPerIp 每个IP最大同时连接客户端数
	 * @param socketOptions socket连接参数
	 * @param args 业务自定义参数
	 * @return
	 * @throws Exception
	 */
	synchronized public static Server start(Integer port,
							   Class serverSideHandlerClass,
							   int maxClients,
							   int maxClientsPerIp,
							   Map<Integer, Object> socketOptions,
							   Object[] args) throws Exception{
		Server instance=servers.get(port);
		if(instance!=null) return instance;

		//纠正参数
		if(maxClients<=0) maxClients=100;
		if(maxClientsPerIp<=0) maxClientsPerIp=1;

		//启动服务端socket
		instance = new Server(port,
				serverSideHandlerClass,
				maxClients,
				maxClientsPerIp,
				socketOptions,
				args);
		instance.startup();
		servers.put(port, instance);
		return instance;
	}

	/**
	 *
	 * @param port 端口
	 * @param serverSideHandlerClass 处理客户端交互的类
	 * @param maxClients 最大同时连接客户端数
	 * @param maxClientsPerIp 每个IP最大同时连接客户端数
	 * @param socketOptions
	 * @param args
	 */
	private Server(Integer port,
				   Class serverSideHandlerClass,
				   int maxClients,
				   int maxClientsPerIp,
				   Map<Integer, Object> socketOptions,
				   Object[] args) {
		this.port=port;
		this.serverSideHandlerClass = serverSideHandlerClass;
		this.maxClients=maxClients;
		this.maxClientsPerIp=maxClientsPerIp;
		this.args=args;
		this.socketOptions=socketOptions==null?SocketOptions.cloneDefaults():socketOptions;
		log.log("new server with options -> "+JUtilBean.map2Json(this.socketOptions), -1);
		this.acceptWaitTimout =(Integer)this.socketOptions.get(java.net.SocketOptions.SO_TIMEOUT);
	}

	/**
	 *
	 * @param maxDataPackageSize
	 */
	public void setMaxDataPackageSize(int maxDataPackageSize){
		if(maxDataPackageSize<=0) maxDataPackageSize=1024*1024*10;
		this.maxDataPackageSize=maxDataPackageSize;
	}

	/**
	 *
	 * @return
	 */
	public int getMaxDataPackageSize(){
		return this.maxDataPackageSize;
	}

	/**
	 *
	 * @param debug
	 */
	public void setDebug(boolean debug) {
		this.debug=debug;
	}

	/**
	 *
	 * @return
	 */
	public boolean getDebug() {
		return this.debug;
	}

	/**
	 *
	 * @return
	 */
	public Integer getPort() {
		return this.port;
	}

	/**
	 *
	 * @return
	 */
	public Class getHandlerClass() {
		return this.serverSideHandlerClass;
	}

	/**
	 *
	 * @return
	 */
	public ConcurrentList<ServerHandler> getHandlers() {
		return serverHandlers.listValues();
	}

	/**
	 *
	 * @param uuid
	 * @return
	 */
	public ServerHandler getHandlerOfUuid(String uuid) {
		return serverHandlers.get(uuid);
	}

	/**
	 *
	 * @param serverHandler
	 */
	public void removeHandler(ServerHandler serverHandler) {
		if(Nvwa.isDebug()){
			log.log("server handler "+serverHandler.getUuid()+" removed from the queue!", -1);
		}
		serverHandlers.remove(serverHandler.getUuid());
	}

	/**
	 *
	 * @param ip
	 * @return
	 */
	private int clientsOnIP(String ip) {
		int count=0;
		ConcurrentList<ServerHandler> handlers=getHandlers();
		for(int i = 0; i< handlers.size(); i++) {
			ServerHandler c=handlers.get(i);
			if(c.getAddress().getHostAddress().equals(ip)) count++;
		}
		return count;
	}

	/**
	 *
	 */
	public void shutdown(){
		ended=true;

		try{
			this.acceptor.shutdown();
		}catch(Exception ex){}

		for(int i=0; i<this.selectors.size(); i++){
			try{
				this.selectors.get(i).shutdown();
			}catch(Exception ex){}
		}

		try{
			this.readers.shutdownNow();
		}catch(Exception ex){}

		List<ServerHandler> _servreHandlers=serverHandlers.listValues();
		for (ServerHandler servreHandler : _servreHandlers) {
			try {
				servreHandler.end(true);
			} catch (Exception ignored) {}
		}

		try{
			serverSocketChannel.close();
		}catch(Exception ex){}
	}

	/**
	 *
	 * @return
	 */
	public boolean isEnded(){
		return this.ended;
	}

	/**
	 *
	 * @throws Exception
	 */
	private void startup() throws Exception{
		this.serverSocketChannel = ServerSocketChannel.open();

		this.serverSocketChannel.configureBlocking(false);//nio
		this.serverSocket=serverSocketChannel.socket();

		//设置socket参数
		this.serverSocket.setReuseAddress((Boolean) this.socketOptions.get(java.net.SocketOptions.SO_REUSEADDR));
		this.serverSocket.setOption(StandardSocketOptions.SO_REUSEADDR, (Boolean) this.socketOptions.get(java.net.SocketOptions.SO_REUSEADDR));

		this.serverSocket.setReceiveBufferSize((Integer) this.socketOptions.get(java.net.SocketOptions.SO_RCVBUF));
		this.serverSocket.setOption(StandardSocketOptions.SO_RCVBUF, (Integer) this.socketOptions.get(java.net.SocketOptions.SO_RCVBUF));

		//开始在指定端口上监听
		this.serverSocket.bind(new InetSocketAddress(this.getPort()));

		//开启连接接受线程
		this.acceptor=new ServerAcceptor(this);

		//服务端业务处理（读）线程池
		this.readers = Executors.newFixedThreadPool((Integer)this.socketOptions.get(SocketOptions.SERVER_HANDLE_POOL_SIZE));

		//事件轮询线程组
		int serverSelectors=(Integer)this.socketOptions.get(SocketOptions.SERVER_SELECTORS);
		for(int i=0; i<serverSelectors; i++){
			ServerSelector selector=new ServerSelector(i, this);
			this.selectors.add(selector);
		}

		//启动监控线程
		this.monitor = new ServerMonitor(this);
		(new Thread(this.monitor)).start();
		log.log("listen on port "+this.getPort()+" in nio mode!",-1);
	}

	/**
	 * 将连接事件注册到指定的Selector
	 * @param selector
	 * @throws Exception
	 */
	protected void setSelectorForConnection(Selector selector) throws Exception{
		this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, "server");
	}

	/**
	 * 建立连接
	 * @throws Exception
	 */
	protected void accept() throws Exception{
		int wait = 0;
		while (this.maxClients > 0 && this.serverHandlers.size() >= this.maxClients) {//超出允许最大连接数
			Global.sleep10Millis();
			wait++;

			//等待超时
			if(wait * 10 >= this.acceptWaitTimout){
				if(Nvwa.isDebug()){
					log.log("超出最大链接数(" + this.maxClients + ")，无法建立连接！", -1);
				}
				return;
			}
		}

		//建立channel
		SocketChannel socketChannel = serverSocketChannel.accept();
		socketChannel.configureBlocking(false);//no blocking
		Socket socket = socketChannel.socket();
		if(Nvwa.isDebug()){
			log.log("[server]options default => (SO_SNDBUF="+socket.getSendBufferSize()+", SO_RCVBUF="+socket.getReceiveBufferSize()+").",-1);
		}

		//设置SocketChannel参数
		int soLinger = (Integer) this.socketOptions.get(java.net.SocketOptions.SO_LINGER);
		socketChannel.setOption(StandardSocketOptions.SO_LINGER, soLinger);
		socketChannel.setOption(StandardSocketOptions.SO_REUSEADDR, (Boolean) this.socketOptions.get(java.net.SocketOptions.SO_REUSEADDR));
		socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, (Boolean) this.socketOptions.get(java.net.SocketOptions.TCP_NODELAY));
		socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, (Integer) this.socketOptions.get(java.net.SocketOptions.SO_SNDBUF));
		socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, (Integer) this.socketOptions.get(java.net.SocketOptions.SO_RCVBUF));
		socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, (Boolean) this.socketOptions.get(java.net.SocketOptions.SO_KEEPALIVE));
		//设置SocketChannel参数 end

		//同一IP上的连接超出最大允许数
		String remoteAddress = socket.getInetAddress().getHostAddress();
		if(clientsOnIP(remoteAddress) >= this.maxClientsPerIp) {
			log.log("同一IP上的连接超出最大允许数（" + this.maxClientsPerIp + "），关闭连接：(" + remoteAddress + ":" + serverSocket.getLocalPort() + ")", -1);
			socketChannel.close();
			return;
		}

		//创建并初始化服务端处理对象
		ServerHandler serverHandler = (ServerHandler)this.getHandlerClass()
				.getConstructor(new Class[]{SocketChannel.class, Socket.class, Map.class, Object[].class})
				.newInstance(new Object[]{socketChannel, socket, this.socketOptions, this.args});
		serverHandler.setServer(this);

		//设置socket参数
		socket.setTcpNoDelay((Boolean) this.socketOptions.get(java.net.SocketOptions.TCP_NODELAY));

		if (soLinger >= 0) socket.setSoLinger(true, soLinger);
		else socket.setSoLinger(false, 0);

		socket.setSoTimeout((Integer) this.socketOptions.get(java.net.SocketOptions.SO_TIMEOUT));

		if(Nvwa.isDebug()){
			log.log("[server]setSendBufferSize => "+this.socketOptions.get(java.net.SocketOptions.SO_SNDBUF),-1);
		}
		socket.setSendBufferSize((Integer) this.socketOptions.get(java.net.SocketOptions.SO_SNDBUF));

		if(Nvwa.isDebug()) {
			log.log("[server]setReceiveBufferSize => " + this.socketOptions.get(java.net.SocketOptions.SO_RCVBUF), -1);
		}
		socket.setReceiveBufferSize((Integer) this.socketOptions.get(java.net.SocketOptions.SO_RCVBUF));
		socket.setKeepAlive((Boolean) this.socketOptions.get(java.net.SocketOptions.SO_KEEPALIVE));
		socket.setOOBInline((Boolean) this.socketOptions.get(java.net.SocketOptions.SO_OOBINLINE));
		//设置socket参数 end

		if(Nvwa.isDebug()) {
			log.log("[server]connection established on port " + this.getPort() + " in nio mode => (SO_SNDBUF=" + socket.getSendBufferSize() + ", SO_RCVBUF=" + socket.getReceiveBufferSize() + ").", -1);
		}

		//分配Selector组
		ServerSelector selector = allotSelector();

		//注册到selector
		serverHandler.setSelectKey(selector.bind(socketChannel, serverHandler.getUuid()));

		//保存到队列
		serverHandlers.put(serverHandler.getUuid(), serverHandler);

		if(Nvwa.isDebug()) {
			log.log("建立连接：(" + this.serverSideHandlerClass + ": " + serverHandler.getUuid() + " -> " + socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort() + "," + socket.getPort() + "," + serverHandler.getUuid() + ",options:" + JUtilBean.map2Json(this.socketOptions) + ")", -1);
		}
	}
	

	@MethodDescription(description = "分配Selector组")
	synchronized private ServerSelector allotSelector(){
		this.selectors = serverSelectorSorter.bubble(this.selectors, JUtilSorter.ASC);
		return this.selectors.get(0);
	}
}