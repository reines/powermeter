package com.jamierf.powermeter;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.string.StringDecoder;

import com.thoughtworks.xstream.XStream;

class CC128Decoder extends StringDecoder {

	@SuppressWarnings("unused")
	private static class Message {
		private static class Channel {
			private int watts;
		}

		private String src;
		private int dsb;
		private String time;
		private double tmpr;
		private int sensor;
		private int id;
		private int type;
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

		final Message reading = (Message) xstream.fromXML(xml);
		return new Reading(reading.tmpr, reading.ch1.watts);
	}
}
