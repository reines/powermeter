package com.jamierf.powermeter.web;

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

import com.google.gson.Gson;

public class DataWebSocketHandler implements WebSocketHandler {

	private static final Logger logger = LoggerFactory.getLogger(DataWebSocketHandler.class);

	private final Collection<WebSocketConnection> conns;
	private final Gson gson;

	public DataWebSocketHandler() {
		conns = new LinkedList<WebSocketConnection>();
		gson = new Gson();
	}

	protected void send(Object o) {
		synchronized (conns) {
			for (WebSocketConnection conn : conns) {
				conn.send(gson.toJson(o));
			}
		}
	}

	@Override
	public void onClose(WebSocketConnection conn) throws Exception {
		if (logger.isTraceEnabled())
			logger.trace("Connection closed");

		synchronized (conns) {
			conns.remove(conn);
		}
	}

	@Override
	public void onMessage(WebSocketConnection conn, String msg) throws Throwable {
		if (logger.isTraceEnabled())
			logger.trace("String received");

		// TODO Auto-generated method stub
	}

	@Override
	public void onMessage(WebSocketConnection conn, byte[] msg) throws Throwable {
		if (logger.isTraceEnabled())
			logger.trace("Bytes received");

		// TODO Auto-generated method stub
	}

	@Override
	public void onOpen(WebSocketConnection conn) throws Exception {
		if (logger.isTraceEnabled())
			logger.trace("Connection opened");

		synchronized (conns) {
			conns.add(conn);
		}
	}

	@Override
	public void onPong(WebSocketConnection conn, String msg) throws Throwable {
		// TODO Auto-generated method stub
	}
}
