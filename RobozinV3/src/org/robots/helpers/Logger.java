package org.robots.helpers;

import robocode.AdvancedRobot;
import robocode.RobocodeFileWriter;

import java.io.FileWriter;

public class Logger {
    private RobocodeFileWriter fileWriter;

    public Logger(String path, String header) {
        try {
            fileWriter = new RobocodeFileWriter(path.concat(".data"));
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
