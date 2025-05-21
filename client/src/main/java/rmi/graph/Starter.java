package rmi.graph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Starter {
    public static void main(String[] args){
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("system.properties")) {
            props.load(input);
        } catch (IOException ex) {
            System.err.println("error while loading system.properties:" + ex.getMessage());
        }

        // Start server in its own thread
        Thread serverThread = new Thread(() -> {
            try {
                Server.main(new String[]{});
            } catch (Exception e) {
                System.err.println("Server error: " + e.getMessage());
            }
        });
        serverThread.start();

        // Delay to ensure server starts before clients
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

        // Read number of client nodes
        int numberOfNodes = Integer.parseInt(props.getProperty("GSP.numberOfnodes", "0"));
        ExecutorService clientPool = Executors.newFixedThreadPool(numberOfNodes);

        // Schedule shutdown after 1 minute
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            // Gracefully shutdown clients
            clientPool.shutdownNow();
            // Interrupt server thread
            serverThread.interrupt();
            // Exit JVM
            System.exit(0);
        }, 1, TimeUnit.MINUTES);

        // Launch clients
        for(int i = 0; i < numberOfNodes; i++){
            String nodeName = props.getProperty("GSP.node" + i);
            int finalI = i;
            clientPool.submit(() -> {
                try {
                    System.out.println("Starting client " + finalI + " on " + nodeName);
                    Client.main(new String[]{"0.2", String.valueOf(finalI)});
                } catch (Exception e) {
                    System.err.println("Client " + finalI + " error: " + e.getMessage());
                }
            });
        }
    }
}
