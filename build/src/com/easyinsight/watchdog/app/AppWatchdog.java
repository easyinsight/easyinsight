package com.easyinsight.watchdog.app;

import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.security.AWSCredentials;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import java.io.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.net.URL;
import java.net.URLConnection;

/**
 * User: James Boe
 * Date: Jan 27, 2009
 * Time: 12:55:27 AM
 */
public class AppWatchdog {

    public static final String[] SHUTDOWN_CMD = {"/opt/tomcat/bin/catalina-easyinsight.sh", "stop"};
    public static final String[] STARTUP_CMD = {"/opt/tomcat/bin/catalina-easyinsight.sh", "start"};


    public void restart() {
        try {
            StreamLogger.logProcess(Runtime.getRuntime().exec(SHUTDOWN_CMD), "shutdown");
            boolean shutdownSuccessful = false;
            int retryCount = 0;
            while (!shutdownSuccessful && retryCount < 10) {
                try {
                    Thread.sleep(5000);
                    URL url = new URL("http://localhost:8080");
                    URLConnection connection = url.openConnection();
                    InputStream content = connection.getInputStream();
                    content.read();
                    System.out.println("successfully opened connection...");
                    retryCount++;
                } catch (Exception e) {
                    shutdownSuccessful = true;
                }
            }
            StreamLogger.logProcess(Runtime.getRuntime().exec(STARTUP_CMD), "startup");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            System.out.println("Running shutdown...");
            StreamLogger.logProcess(Runtime.getRuntime().exec(SHUTDOWN_CMD), "shutdown");
            // need to verify...
            boolean shutdownSuccessful = false;
            int retryCount = 0;
            while (!shutdownSuccessful && retryCount < 10) {
                try {
                    Thread.sleep(5000);
                    URL url = new URL("http://localhost:8080");
                    URLConnection connection = url.openConnection();
                    InputStream content = connection.getInputStream();
                    content.read();
                    System.out.println("successfully opened connection...");
                    retryCount++;
                } catch (Exception e) {
                    shutdownSuccessful = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            System.out.println("Running Startup....");
            StreamLogger.logProcess(Runtime.getRuntime().exec(STARTUP_CMD), "startup");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateFromS3() {
        try {
            AWSCredentials credentials = new AWSCredentials("0AWCBQ78TJR8QCY8ABG2", "bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI");
            RestS3Service s3Service = new RestS3Service(credentials);
            S3Bucket bucket = s3Service.getBucket("eiproduction");
            S3Object object = s3Service.getObject(bucket, "code.zip");
            byte retrieveBuf[];
            retrieveBuf = new byte[1];
            InputStream bfis = object.getDataInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while (bfis.read(retrieveBuf) != -1) {
                baos.write(retrieveBuf);
            }
            byte[] resultBytes = baos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(resultBytes);
            ZipInputStream zin = new ZipInputStream(bais);
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                System.out.println("Unzipping " + ze.getName());
                if (ze.isDirectory()) {
                    File directory = new File("/opt/tomcat/webapps/app/" + ze.getName());
                    if (!directory.exists()) {
                        if (!directory.mkdir()) {
                            throw new RuntimeException("couldn't create directory " + directory.getAbsolutePath());
                        }
                    }
                } else {
                    byte[] buffer = new byte[8192];
                    FileOutputStream fout = new FileOutputStream("/opt/tomcat/webapps/app/" + ze.getName());
                    BufferedOutputStream bufOS = new BufferedOutputStream(fout, 8192);
                    int nBytes;
                    while ((nBytes = zin.read(buffer)) != -1) {
                        bufOS.write(buffer, 0, nBytes);
                    }
                    /*for (int c = zin.read(); c != -1; c = zin.read()) {
                        bufOS.write(c);
                    }*/
                    bufOS.close();
                    fout.close();
                }
                zin.closeEntry();
            }
            zin.close();
            copyFile("eiconfig.properties", "/opt/tomcat/webapps/app/WEB-INF/classes/eiconfig.properties");
            copyFile("cache.ccf", "/opt/tomcat/webapps/app/WEB-INF/classes/cache.ccf");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void copyFile(String fileName, String targetName) throws IOException {
        File file = new File(fileName);
        File target = new File(targetName);
        target.delete();
        FileReader reader = new FileReader(file);
        FileWriter writer = new FileWriter(target);
        int byteSymbol;
        while ((byteSymbol = reader.read()) != -1) {
            writer.write(byteSymbol);
        }
        writer.close();
        reader.close();
    }

    public static void main(String[] args) throws IOException {
            File file = new File("eiconfig.properties");
            File target = new File("/opt/tomcat/webapps/DMS/WEB-INF/classes/eiconfig.properties");
            target.delete();
            FileReader reader = new FileReader(file);
            FileWriter writer = new FileWriter(target);
            int byteSymbol;
            while ((byteSymbol = reader.read()) != -1) {
                writer.write(byteSymbol);
            }
            writer.close();
            reader.close();
    }
}
