package module_micro_api_server;

import com.github.microprograms.osgi_module_activator.ModuleActivator;

import module_micro_api_server.http_server.HttpServer;

public class Activator extends ModuleActivator {
	private HttpServer httpServer;

	@Override
	protected void onStart() throws Exception {
		httpServer = new HttpServer(context);
		httpServer.start();
	}

	@Override
	protected void onStop() throws Exception {
		httpServer.stop();
	}
}
