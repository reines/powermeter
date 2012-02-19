package com.jamierf.powermeter;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.ConversionException;

class PowerMeterChannelHandler extends SimpleChannelHandler {

	private static final Logger logger = LoggerFactory.getLogger(PowerMeterChannelHandler.class);

	private PowerMeter meter;

	public PowerMeterChannelHandler(PowerMeter meter) {
		this.meter = meter;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (logger.isDebugEnabled())
			logger.debug("Channel connected: {}", e.getChannel());
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (logger.isDebugEnabled())
			logger.debug("Channel disconnected: {}", e.getChannel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		// It was an unsupported message, ignore it
		if (e.getCause() instanceof ConversionException)
			return;

		if (logger.isWarnEnabled())
			logger.warn("Caught exception", e.getCause());
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (logger.isTraceEnabled())
			logger.trace("Message received");

		final Reading reading = (Reading) e.getMessage();
		meter.readingReceived(reading);
	}
}
