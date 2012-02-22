package com.jamierf.powermeter.web;

import java.io.IOException;
import java.util.Calendar;

import org.webbitserver.HttpControl;
import org.webbitserver.HttpHandler;
import org.webbitserver.HttpRequest;
import org.webbitserver.HttpResponse;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.handler.EmbeddedResourceHandler;

import com.google.gson.Gson;
import com.jamierf.powermeter.Reading;
import com.jamierf.powermeter.db.Database;

public class WebUI {

	private final Database db;
	private final Gson gson;
	private final WebServer server;
	private final DataWebSocketHandler webSocketHandler;

	public WebUI(int port, Database db) throws IOException {
		this.db = db;

		gson = new Gson();

		server = WebServers.createWebServer(port);

		webSocketHandler = new DataWebSocketHandler();
		server.add("/data", webSocketHandler);

		server.add("/history", new HttpHandler() {
			@Override
			public void handleHttpRequest(HttpRequest req, HttpResponse resp, HttpControl ctrl) throws Exception {
				final Calendar since = Calendar.getInstance();
				since.add(Calendar.DATE, -1);

				resp.content(gson.toJson(WebUI.this.db.fetchLatestReadings(since.getTime()))).end();
			}
		});

		// Static files, in htdocs resource directory
		server.add(new EmbeddedResourceHandler(WebUI.class.getPackage().getName().replaceAll("\\.", "/") + "/htdocs"));

		server.start();
	}

	public void displayReading(Reading reading) {
		webSocketHandler.send(reading);
	}
}
