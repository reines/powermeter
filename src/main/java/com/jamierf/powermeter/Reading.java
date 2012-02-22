package com.jamierf.powermeter;

import java.util.Date;


public class Reading {

	private final int sensor;
	private final float temperature;
	private final int watts;
	private final Date date;

	public Reading(int sensor, float temperature, int watts) {
		this (sensor, temperature, watts, new Date());
	}

	public Reading(int sensor, float temperature, int watts, Date date) {
		this.sensor = sensor;
		this.temperature = temperature;
		this.watts = watts;
		this.date = date;
	}

	public int getSensor() {
		return sensor;
	}

	public float getTemperature() {
		return temperature;
	}

	public int getWatts() {
		return watts;
	}

	public Date getDate() {
		return date;
	}

	@Override
	public String toString() {
		return String.format("sensor: %d, date: %s, temp: %.1f, watts: %d", sensor, date, temperature, watts);
	}
}
