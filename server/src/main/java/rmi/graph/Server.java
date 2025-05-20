package rmi.graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Server {
    public static void main(String[] args) {
        String filename = "server/graph.txt";
        Graph g = new Graph();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.equalsIgnoreCase("S")) break;
                String[] parts = line.split("\\s+");
                if (parts.length < 2) continue;
                int u = Integer.parseInt(parts[0]);
                int v = Integer.parseInt(parts[1]);
                g.addEdge(u, v);
            }
        } catch (IOException ioe) {
            System.err.println("Error reading graph file: " + ioe.getMessage());
        }
        try {
            GraphServiceImpl impl = new GraphServiceImpl(g);
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.rebind("GraphService", impl);
            System.out.println("GraphService bound and ready.");
        } catch (Exception e) {
            System.out.println("GraphService exception: " + e.getMessage());
        }
    }
}
