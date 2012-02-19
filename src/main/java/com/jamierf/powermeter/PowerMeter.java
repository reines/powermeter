package com.jamierf.powermeter;

import gnu.io.RXTXLoader;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.uniluebeck.itm.nettyrxtx.RXTXChannelConfig;
import de.uniluebeck.itm.nettyrxtx.RXTXChannelFactory;
import de.uniluebeck.itm.nettyrxtx.RXTXDeviceAddress;

public class PowerMeter {

	private static final Logger logger = LoggerFactory.getLogger(PowerMeter.class);

	private static final int MAX_FRAME_SIZE = 8192;

	static {
		try {
			RXTXLoader.load();
		}
		catch (IOException e) {
			if (logger.isErrorEnabled())
				logger.error("Unable to load RXTX natives", e);
		}
	}

	public static void main(String[] args) {
		final RXTXDeviceAddress address = new RXTXDeviceAddress("/dev/ttyUSB0"); // TODO: CLI parser
		final PowerMeter meter = new PowerMeter(address);
		meter.start();
	}

	private final RXTXDeviceAddress address;
	private final ClientBootstrap bootstrap;
	private final AtomicBoolean running;

	public PowerMeter(RXTXDeviceAddress address) {
		this.address = address;

		bootstrap = new ClientBootstrap(new RXTXChannelFactory(Executors.newCachedThreadPool()));

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			@Override
			public ChannelPipeline getPipeline() throws Exception {
				final ChannelPipeline pipeline = Channels.pipeline();

				pipeline.addLast("framer", new DelimiterBasedFrameDecoder(MAX_FRAME_SIZE, Delimiters.lineDelimiter()));
				pipeline.addLast("decoder", new CC128Decoder());
				pipeline.addLast("handler", new PowerMeterChannelHandler(PowerMeter.this));

				return pipeline;
			}
		});

		bootstrap.setOption("baudrate", 57600);
		bootstrap.setOption("stopbits", RXTXChannelConfig.Stopbits.STOPBITS_1);
		bootstrap.setOption("databits", RXTXChannelConfig.Databits.DATABITS_8);
		bootstrap.setOption("paritybit", RXTXChannelConfig.Paritybit.NONE);

		running = new AtomicBoolean(false);
	}

	public boolean start() {
		if (!running.compareAndSet(false, true))
			throw new RuntimeException("PowerMeter already running");

		return bootstrap.connect(address).awaitUninterruptibly().isSuccess();
	}

	public void stop() {
		if (!running.get())
			throw new RuntimeException("PowerMeter not running");

		bootstrap.getFactory().releaseExternalResources();
	}

	void readingReceived(Reading reading) {
		System.out.println(reading); // TODO: insert into a database, or something cool...?
	}
}
