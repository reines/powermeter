package com.jamierf.powermeter.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamierf.powermeter.Reading;

public class Database {

	private static final Logger logger = LoggerFactory.getLogger(Database.class);

	static {
		try {
			Class.forName("org.sqlite.JDBC");
		}
		catch (ClassNotFoundException e) {
			if (logger.isErrorEnabled())
				logger.error("Failed to load SQLite driver", e);
		}
	}

	private static final String TABLE_NAME = "readings";

	private static final String CREATE_DB_SQL = "CREATE TABLE " + TABLE_NAME + " (sensor INTEGER, time BIGINT, temperature REAL, watts INTEGER, PRIMARY KEY (sensor, time))";
	private static final String INSERT_READING_SQL = "INSERT INTO " + TABLE_NAME + " VALUES (?, ?, ?, ?)";
	private static final String FETCH_READINGS_SINCE_SQL = "SELECT * FROM " + TABLE_NAME + " WHERE time > ?";

	private static Connection create(File file) throws DatabaseException {
		try {
			final Connection conn = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());

			// Fetch the meta data, check if the readings table already exists
			final DatabaseMetaData meta = conn.getMetaData();
			final ResultSet result = meta.getTables(null, null, TABLE_NAME, null);
			if (!result.next()) {
				final Statement statement = conn.createStatement();

				statement.execute(CREATE_DB_SQL);

				statement.close();
			}

			return conn;
		}
		catch (SQLException e) {
			if (logger.isWarnEnabled())
				logger.warn("SQL exception", e);

			throw new DatabaseException(e);
		}
	}

	private final Connection conn;

	private PreparedStatement insertReadingStatement;
	private PreparedStatement fetchReadingsStatement;

	public Database(File file) throws DatabaseException {
		conn = Database.create(file);

		try {
			insertReadingStatement = conn.prepareStatement(INSERT_READING_SQL);
			fetchReadingsStatement = conn.prepareStatement(FETCH_READINGS_SINCE_SQL);
		}
		catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public synchronized Collection<Reading> fetchLatestReadings(Date since) {
		try {
			fetchReadingsStatement.setLong(1, since.getTime());

			final ResultSet result = fetchReadingsStatement.executeQuery();
			insertReadingStatement.clearParameters();

			final List<Reading> readings = new ArrayList<Reading>();
			while (result.next()) {
				readings.add(new Reading(result.getInt("sensor"), result.getFloat("temperature"), result.getInt("watts"), new Date(result.getLong("time"))));
			}

			return readings;
		}
		catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public synchronized void insertReading(Reading reading) {
		try {
			insertReadingStatement.setInt(1, reading.getSensor());
			insertReadingStatement.setLong(2, reading.getDate().getTime());
			insertReadingStatement.setFloat(3, reading.getTemperature());
			insertReadingStatement.setInt(4, reading.getWatts());

			final int rows = insertReadingStatement.executeUpdate();
			insertReadingStatement.clearParameters();

			if (logger.isTraceEnabled())
				logger.trace("Inserted {} rows in to database", rows);
		}
		catch (SQLException e) {
			throw new DatabaseException(e);
		}
	}

	public void close() throws DatabaseException {
		try {
			if (conn.isClosed())
				throw new RuntimeException("Database not running");

			conn.close();
		}
		catch (SQLException e) {
			if (logger.isWarnEnabled())
				logger.warn("Unable to close database", e);

			throw new DatabaseException(e);
		}
	}
}
