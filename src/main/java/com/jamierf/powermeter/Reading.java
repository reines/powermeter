package com.jamierf.powermeter;


public class Reading {

	private double temperature;
	private int watts;

	public Reading(double temperature, int watts) {
		this.temperature = temperature;
		this.watts = watts;
	}

	public double getTemperature() {
		return temperature;
	}

	public int getWatts() {
		return watts;
	}

	@Override
	public String toString() {
		return String.format("temp: %.1f, watts: %d", temperature, watts);
	}
}
