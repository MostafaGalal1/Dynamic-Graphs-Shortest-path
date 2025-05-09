package rmi.graph;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Starter {
    public static void main(String[] args){
        Properties props = new Properties();
        try (InputStream input = new FileInputStream("system.properties")) {
            props.load(input);
        } catch (IOException ex) {
            System.err.println("error while loading system.properties:" + ex.getMessage());
        }
        Thread serverThread = new Thread(() -> {
            try {
                Server.main(new String[]{}); // Run server
            } catch (Exception e) {
                System.err.println("Server error: " + e.getMessage());
            }
        });
        serverThread.start();
        // Delay to ensure server starts before clients
        //Otherwise, error appears on clients
        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}

        // Read number of client nodes
        int numberOfNodes = Integer.parseInt(props.getProperty("GSP.numberOfnodes", "0"));
        ExecutorService clientPool = Executors.newFixedThreadPool(numberOfNodes);

        for(int i=0;i<numberOfNodes;i++){
            String nodeName = props.getProperty("GSP.node" + i);
            int finalI = i;
            clientPool.submit(() -> {
                try {
                    System.out.println("Starting client " + finalI + " on " + nodeName);
                    Client.main(new String[]{"0.2",String.valueOf(finalI)}); // You can pass writeProb here if needed
                } catch (Exception e) {
                    System.err.println("Client " + finalI + " error: " + e.getMessage());
                }
            });

        }



    }
}
