package j.core.webserver.undertow;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.HttpServletSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.jsp.HackInstanceManager;
import io.undertow.jsp.JspServletBuilder;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.encoding.ContentEncodingRepository;
import io.undertow.server.handlers.encoding.EncodingHandler;
import io.undertow.server.handlers.encoding.GzipEncodingProvider;
import io.undertow.server.handlers.resource.FileResourceManager;
import io.undertow.server.handlers.sse.ServerSentEventConnection;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.*;
import io.undertow.servlet.util.ImmediateInstanceFactory;
import io.undertow.util.Headers;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import j.core.Startup;
import j.core.annotation.description.ClassDescription;
import j.core.common.JProperties;
import j.core.web.mcp.McpRouter;
import j.core.webserver.WebServer;
import j.core.webserver.undertow.servlet.WebContainerInitializer;
import j.log.Logger;
import j.util.JUtilMath;
import j.util.JUtilString;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import lombok.Setter;
import org.apache.jasper.deploy.JspPropertyGroup;
import org.apache.jasper.deploy.TagLibraryInfo;
import org.xnio.Options;
import org.xnio.Sequence;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@ClassDescription(author = "肖炯",
        date = "2021/11/15",
        description = "嵌入式undertow")
@Setter
public class UndertowWebServer implements WebServer {
    private static Logger log=Logger.create(UndertowWebServer.class);//日志输出

    private final Map<String, ServerSentEventConnection> sessions = new ConcurrentHashMap<>();

    private Undertow server;

    private UndertowConf undertowConf=new UndertowConf();

    @Override
    synchronized public void start() throws Exception{
        //未启用
        if(!undertowConf.isEnabled()) return;

        //已启动过
        if(server != null) return;

        log.log("try to start undertow server embedded -> {\"user.dir\":\""+JProperties.getUserDir()+"\"}", -1);

        try {
            //创建实例
            final PathHandler servletPath = new PathHandler();
            final ServletContainer container = ServletContainer.Factory.newInstance();

            DeploymentInfo deploymentInfo = configManager();

            DeploymentManager manager = container.addDeployment(deploymentInfo);
            manager.deploy();

            HttpHandler httpHandler=manager.start();
            servletPath.addPrefixPath(deploymentInfo.getContextPath(), httpHandler);

            Undertow.Builder builder = Undertow.builder();
            addListeners(builder);
            builder.setHandler(configGzip(httpHandler));

            server=builder.build();
        } catch (ServletException e) {
            e.printStackTrace();
        }
        server.start();
        System.out.println("undertow started and listened on port [" + undertowConf.getPort() + "]");
    }

    @Override
    synchronized public void stop() {
        if (server != null) {
            server.stop();
        }
    }

    /**
     *
     * @return
     */
    private DeploymentInfo configManager() {
        String userDir = JProperties.getUserDir();
        Set<Class<?>> handlesTypes = new HashSet<>(2);
        handlesTypes.add(WebContainerInitializer.class);

        File baseDir = new File(JUtilString.appendPath(userDir, undertowConf.getBaseDir()));
        if(!baseDir.exists()) baseDir.mkdirs();
        log.log("undertow server baseDir => "+baseDir.getAbsolutePath(), -1);

        File webapp = new File(JUtilString.appendPath(userDir, undertowConf.getWebApp()));
        if(!webapp.exists()) webapp.mkdirs();
        log.log("undertow server webapp => "+webapp.getAbsolutePath(), -1);

        WebSocketDeploymentInfo webSocketDeploymentInfo = new WebSocketDeploymentInfo().setBuffers(new DefaultByteBufferPool(true, 256));
        String endpointClasses=undertowConf.getEndpointClasses();
        if(!JUtilString.isBlank(endpointClasses)){
            String[] classes = endpointClasses.split(",");
            for(int i=0; i<classes.length; i++){
                try{
                    log.log("add websocket endpoint class => "+classes[i], -1);
                    webSocketDeploymentInfo.addEndpoint(Class.forName(classes[i]));
                }catch (Exception e){
                    log.log(e, Logger.LEVEL_ERROR);
                }
            }
        }

        FilterInfo filterOnlines = new FilterInfo("Onlines", j.core.web.online.Onlines.class);
        filterOnlines.setAsyncSupported(true);

        FilterInfo encodingFilter = new FilterInfo("EncodingFilter", j.core.sys.EncodingFilter.class);
        encodingFilter.setAsyncSupported(true);

        FilterInfo actionRouter = new FilterInfo("ActionRouter", j.core.web.handler.Router.class);
        actionRouter.setAsyncSupported(true);

        FilterInfo i18NFilter = new FilterInfo("I18NFilter", j.I18N.I18NFilter.class);
        i18NFilter.setAsyncSupported(true);

        DeploymentInfo deploymentInfo = Servlets.deployment()
                .setClassLoader(Startup.getDefaultClassLoader())
                .setContextPath("")
                //.setDefaultRequestEncoding("UTF-8")
                //.setDefaultResponseEncoding("UTF-8")
                .setTempDir(baseDir)
                .setDeploymentName(undertowConf.getDeploymentName())
                .setResourceManager(new FileResourceManager(webapp, 0))
                //.setResourceManager(new ClassPathResourceManager(JFramework.class.getClassLoader(), JFramework.class.getPackage()))

                .addFilter(filterOnlines)
                .addFilterUrlMapping("Onlines","*", DispatcherType.REQUEST)
                .addFilterUrlMapping("Onlines","*", DispatcherType.FORWARD)
                .addFilterUrlMapping("Onlines","/", DispatcherType.REQUEST)
                .addFilterUrlMapping("Onlines","/", DispatcherType.FORWARD)

                .addFilter(encodingFilter)
                .addFilterUrlMapping("EncodingFilter","*", DispatcherType.REQUEST)
                .addFilterUrlMapping("EncodingFilter","*", DispatcherType.FORWARD)

                .addFilter(actionRouter)
                .addFilterUrlMapping("ActionRouter","*", DispatcherType.REQUEST)
                .addFilterUrlMapping("ActionRouter","*", DispatcherType.FORWARD)

                .addFilter(i18NFilter)
                .addFilterUrlMapping("I18NFilter","*", DispatcherType.REQUEST)
                .addFilterUrlMapping("I18NFilter","*", DispatcherType.FORWARD)

                .addServlet(JspServletBuilder.createServlet("JspServlet", "*.jsp").setAsyncSupported(true))

                .addServletContainerInitializer(new ServletContainerInitializerInfo(WebContainerInitializer.class, handlesTypes))
                .addServletContextAttribute(WebSocketDeploymentInfo.ATTRIBUTE_NAME,webSocketDeploymentInfo);

        //启用MCP
        if(undertowConf.isMcpEnabled()){
            //构建基于 SSE/HTTP 的传输层提供者 (Servlet)
            log.log("构建基于 SSE/HTTP 的传输层提供者 (Servlet)", -1);
            HttpServletSseServerTransportProvider transport = HttpServletSseServerTransportProvider.builder()
                    .jsonMapper(McpJsonDefaults.getMapper())
                    .baseUrl("/mcp")
                    .sseEndpoint("/sse")
                    .messageEndpoint("/message")
                    .build();

            //配置McpServlet
            log.log("配置McpServlet", -1);
            ServletInfo servletInfo = Servlets.servlet("McpServlet", HttpServletSseServerTransportProvider.class, new ImmediateInstanceFactory<>(transport))
                    .addMapping("/*")
                    .setAsyncSupported(true);

            deploymentInfo.addServlet(servletInfo);

            //初始化 MCP Server，并将 Transport 绑定
            log.log("初始化 MCP Server，并将 Transport 绑定", -1);
            McpSyncServer mcpServer = McpServer.sync(transport)
                    .serverInfo("jframework-mcp-remote-server", "1.0.0")
                    .instructions("Use mcp_gateway tool to access remote business methods.")
                    .toolCall(
                            McpRouter.buildGatewayTool(),
                            (exchange, request) -> {
                                try {
                                    Map<String, Object> argsMap = request.arguments();
                                    String resultJson = McpRouter.dispatch(argsMap, exchange);
                                    // 返回 JSON 格式结果
                                    return new McpSchema.CallToolResult(List.<McpSchema.Content>of(
                                            new McpSchema.TextContent(resultJson)
                                    ), false, null, null);
                                } catch (Exception e) {
                                    return new McpSchema.CallToolResult(List.<McpSchema.Content>of(
                                            new McpSchema.TextContent("{\"error\":\"" + e.getMessage() + "\"}")
                                    ), true, null, null);
                                }
                            }
                    ).build();

            //注册关闭钩子
            log.log("注册关闭钩子", -1);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    log.log("Shutting down MCP Server...", -1);
                    mcpServer.closeGracefully();
                    server.stop();
                } catch (Exception e) {
                    log.log(e, Logger.LEVEL_ERROR);
                }
            }));
        }

        JspServletBuilder.setupDeployment(deploymentInfo,
                new HashMap<String, JspPropertyGroup>(),
                new HashMap<String, TagLibraryInfo>(),
                new HackInstanceManager());

        return deploymentInfo;
    }

    /**
     *
     * @param builder
     */
    private void addListeners(Undertow.Builder builder) throws Exception{
        builder.addHttpListener(undertowConf.getPort(), undertowConf.getHost())
                .setWorkerThreads(undertowConf.getWorkerThreads())
                .setIoThreads(undertowConf.getIoThreads())
                .setServerOption(UndertowOptions.ALLOW_UNESCAPED_CHARACTERS_IN_URL, undertowConf.isAllowUnescapedCharactersInUrl())
                .setServerOption(UndertowOptions.ENABLE_HTTP2, undertowConf.isHttp2enabled());


        if(undertowConf.isSslEnabled()) {
            try {
                KeyStore trustStore = loadKeyStore(undertowConf.getSslKeyStoreType(), undertowConf.getSslKeyStore(), undertowConf.getSslKeyStorePassword());
                SSLContext sslContext = createSSLContext(undertowConf.getSslContextProtocol(), trustStore);

                log.log("set ssl server => " + undertowConf.getSslPort() + " => " + undertowConf.getSslProtocols() + " => " + undertowConf.getSslCiphers() + " => SSL_PROTOCOL = " + undertowConf.getSslProtocol(), -1);
                builder.addHttpsListener(undertowConf.getSslPort(), undertowConf.getHost(), sslContext)
                        .setSocketOption(Options.SSL_ENABLED_PROTOCOLS, Sequence.of(JUtilString.getTokens(undertowConf.getSslProtocols(), " ")))
                        .setSocketOption(Options.SSL_SUPPORTED_PROTOCOLS, Sequence.of(JUtilString.getTokens(undertowConf.getSslProtocols(), " ")))
                        .setSocketOption(Options.SSL_ENABLED_CIPHER_SUITES, Sequence.of(JUtilString.getTokens(undertowConf.getSslCiphers(), ":")))
                        .setSocketOption(Options.SSL_SUPPORTED_CIPHER_SUITES, Sequence.of(JUtilString.getTokens(undertowConf.getSslCiphers(), ":")))
                        .setSocketOption(Options.SSL_PROTOCOL, undertowConf.getSslProtocol());
            }catch (Exception e){
                log.log("start ssl server failed:", -1);
                log.log(e, Logger.LEVEL_ERROR);
            }
        }
    }

    /**
     *
     * @param protocol
     * @param trustStore
     * @return
     * @throws Exception
     */
    private static SSLContext createSSLContext(final String protocol, final KeyStore trustStore) throws Exception {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance(JUtilString.isBlank(protocol)?"TLS":protocol);
        sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
        return sslContext;
    }

    /**
     *
     * @param keyStoreType
     * @param keyStorePath
     * @param keyStorePassword
     * @return
     * @throws Exception
     */
    private static KeyStore loadKeyStore(String keyStoreType, String keyStorePath, String keyStorePassword) throws Exception {
        InputStream stream = new FileInputStream(keyStorePath);
        if(stream == null) {
            throw new IllegalArgumentException("Could not load keystore");
        }
        KeyStore loadedKeystore = KeyStore.getInstance(JUtilString.isBlank(keyStoreType)?"JKS":keyStoreType);
        loadedKeystore.load(stream, keyStorePassword.toCharArray());
        return loadedKeystore;
    }


    /**
     *
     * @param httpHandler
     * @return
     */
    private HttpHandler configGzip(HttpHandler httpHandler) {
        if(!undertowConf.isGzipEnabled()) return httpHandler;

        return new EncodingHandler(httpHandler,
                new ContentEncodingRepository().addEncodingHandler("gzip",
                        new GzipEncodingProvider(undertowConf.getGzipLevel()),
                        undertowConf.getGzipPriority(),
                        this::gzipEnabled));
    }

    /**
     *
     * @param value
     * @return
     */
    private boolean gzipEnabled(final HttpServerExchange value) {
        if (undertowConf.isGzipEnabled()) {
            final String length = value.getResponseHeaders().getFirst(Headers.CONTENT_LENGTH);
            if (!JUtilMath.isLong(length) || Long.parseLong(length) <= 0) return true;

            return Long.parseLong(length) > undertowConf.getGzipMinLength();
        }
        return false;
    }
}
