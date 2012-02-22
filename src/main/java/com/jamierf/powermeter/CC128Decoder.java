package com.jamierf.powermeter;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;

class CC128Decoder extends StringDecoder {

	private static final Logger logger = LoggerFactory.getLogger(CC128Decoder.class);

	@SuppressWarnings("unused")
	private static class Message {
		// We use strings here for all otherwise 090 will be parsed as octal
		// rather than decimal, and fail

		private static class Channel {
			private String watts;
		}

		private String src;
		private String dsb;
		private String time;
		private String tmpr;
		private String sensor;
		private String id;
		private String type;
		private Channel ch1;
	}

	private final XStream xstream;

	public CC128Decoder() {
		xstream = new XStream();

		xstream.alias("msg", Message.class);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		final String xml = (String) super.decode(ctx, channel, msg);
		if (xml == null)
			return null;

		if (logger.isTraceEnabled())
			logger.trace(xml);

		final Message reading = (Message) xstream.fromXML(xml);
		return new Reading(Integer.parseInt(reading.sensor), Float.parseFloat(reading.tmpr), Integer.parseInt(reading.ch1.watts));
	}
}
