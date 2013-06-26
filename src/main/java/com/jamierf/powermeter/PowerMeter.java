package com.jamierf.powermeter;

import com.jamierf.powermeter.db.Database;
import com.jamierf.powermeter.web.WebUI;
import com.jamierf.rxtx.RXTXLoader;
import de.uniluebeck.itm.nettyrxtx.RXTXChannelConfig;
import de.uniluebeck.itm.nettyrxtx.RXTXChannelFactory;
import de.uniluebeck.itm.nettyrxtx.RXTXDeviceAddress;
import org.apache.commons.cli.*;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class PowerMeter {

	private static final Logger logger = LoggerFactory.getLogger(PowerMeter.class);

	private static final int MAX_FRAME_SIZE = 8192;

	private static final String DEFAULT_DB_FILE = "readings.db";
	private static final String DEFAULT_COM_PORT = "/dev/ttyUSB0";

	private static final Options options;

	static {
		options = new Options();

		options.addOption("h", false, "Print this help message");
		options.addOption("i", true, "COM port");
		options.addOption("d", true, "Database file");

		try {
			RXTXLoader.load();
		}
		catch (IOException e) {
			if (logger.isErrorEnabled())
				logger.error("Unable to load RXTX natives", e);
		}
	}

	public static void main(String[] args) throws IOException, ParseException {
		final CommandLineParser parser = new GnuParser();
		final CommandLine params = parser.parse(options, args);

		if (params.hasOption("h")) {
			final HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("MediaManager", options);
			return;
		}

		final RXTXDeviceAddress address = new RXTXDeviceAddress(params.getOptionValue("i", DEFAULT_COM_PORT));
		final File databaseFile = new File(params.getOptionValue("d", DEFAULT_DB_FILE));

		final PowerMeter meter = new PowerMeter(address, databaseFile);
		meter.start();
	}

	private final RXTXDeviceAddress address;
	private final ClientBootstrap bootstrap;
	private final Database db;
	private final WebUI web;
	private final AtomicBoolean running;

	public PowerMeter(RXTXDeviceAddress address, File databaseFile) throws IOException {
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

		db = new Database(databaseFile);
		web = new WebUI(8989, db);

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

		db.close();

		bootstrap.getFactory().releaseExternalResources();
	}

	void readingReceived(Reading reading) {
		db.insertReading(reading);
		web.displayReading(reading);
	}
}
