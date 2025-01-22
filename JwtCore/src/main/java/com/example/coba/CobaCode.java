package com.example.coba;

/*
IntelliJ IDEA 2024.2.4 (Community Edition)
Build #IC-242.23726.103, built on October 23, 2024
@Author mcputro a.k.a. Mu'ti Cahyono Putro
Created on 22 Jan 2025 11:32
@Last Modified 22 Jan 2025 11:32
Version 1.0
*/

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class CobaCode {
    public void cobian1() {
        Properties properties = new Properties();

        // Memuat file config.properties dari classpath
        try (InputStream input = CobaCode.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return;
            }

            // Memuat data properties dari file
            properties.load(input);

            // Mengakses data dari file properties
            String appName = properties.getProperty("app.name");
            String appVersion = properties.getProperty("app.version");

            String stringDurationInSeconds = properties.getProperty("app.durationInSeconds");
            Integer integerDurationInSeconds = null;
            if (stringDurationInSeconds == null || stringDurationInSeconds.isEmpty() || stringDurationInSeconds.equals("0")) {
                integerDurationInSeconds = 360;
            } else {
                integerDurationInSeconds = Integer.parseInt(stringDurationInSeconds);
            }


            System.out.println("App Name: " + appName);
            System.out.println("App Version: " + appVersion);
            System.out.println("Duration: " + (integerDurationInSeconds + 3));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
