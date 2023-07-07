package org.robots.helpers;

import robocode.AdvancedRobot;

import java.io.FileWriter;

public class Logger {
    private FileWriter fileWriter;

    public Logger(String path, String header) {
        try {
            String timesStamp = String.valueOf(System.currentTimeMillis());
            fileWriter = new FileWriter(path + timesStamp + ".csv");
            fileWriter.append(header);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    public void log(Object[] values) {
        try {
            for (Object value : values) {
                fileWriter.append(value.toString());
                fileWriter.append(",");
            }
            fileWriter.append("\n");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void close() {
        try {
            fileWriter.close();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
