package module_micro_api_server.http_server;

import org.eclipse.jetty.server.Server;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer {
	private static final Logger log = LoggerFactory.getLogger(HttpServer.class);
	private static final String property_key_port = "module_micro_api_server.port";

	private BundleContext context;
	private Server server;

	public HttpServer(BundleContext context) {
		this.context = context;
	}

	public synchronized void start() throws Exception {
		String port = context.getProperty(property_key_port);
		log.info("BundleContext Property {} = {}", property_key_port, port);
		server = new Server(Integer.valueOf(port));
		server.setHandler(new HttpRequestHandler(context));
		server.start();
	}

	public synchronized void stop() throws Exception {
		server.stop();
	}
}
