package rmi.graph;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class VariantGraphServiceImpl extends UnicastRemoteObject implements GraphService {
    private final Graph graph;
    private final ReentrantReadWriteLock rwLock;
    private final PrintWriter log;
    private final ExecutorService executor;

    public VariantGraphServiceImpl(Graph g) throws IOException {
        super();
        this.graph = g;
        this.rwLock = new ReentrantReadWriteLock(true);
        this.log = new PrintWriter("server.log", String.valueOf(StandardCharsets.UTF_8));
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public List<Response> batch(List<Request> requests) throws RemoteException {
        long start = System.currentTimeMillis();
        List<Future<Response>> futures = new ArrayList<>();

        for (Request req : requests) {
            futures.add(executor.submit(() -> processRequest(req)));
        }

        List<Response> responses = new ArrayList<>();
        for (Future<Response> future : futures) {
            try {
                responses.add(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RemoteException("Error processing request", e);
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.printf("%s BATCH COMPLETE %d ms\n", timestamp(), elapsed);
        log.flush();
        return responses;
    }

    private Response processRequest(Request req) {
        switch (req.type) {
            case ADD:
                rwLock.writeLock().lock();
                try {
                    boolean ok = graph.addEdge(req.u, req.v);
                    log.printf("%s ADD %d->%d %s\n", timestamp(), req.u, req.v, ok ? "OK" : "NOOP");
                    return Response.addResp(ok);
                } finally {
                    rwLock.writeLock().unlock();
                }
            case DELETE:
                rwLock.writeLock().lock();
                try {
                    boolean ok = graph.deleteEdge(req.u, req.v);
                    log.printf("%s DELETE %d->%d %s\n", timestamp(), req.u, req.v, ok ? "OK" : "NOOP");
                    return Response.delResp(ok);
                } finally {
                    rwLock.writeLock().unlock();
                }
            case QUERY:
                rwLock.readLock().lock();
                try {
                    List<Integer> path = graph.shortestPath(req.u, req.v);
                    int pathLength = path.size() - 1;
                    log.printf("%s QUERY %d->%d PATH %s\n", timestamp(), req.u, req.v, pathLength);
                    return Response.queryResp(pathLength);
                } finally {
                    rwLock.readLock().unlock();
                }
            default:
                throw new IllegalArgumentException("Unknown request type: " + req.type);
        }
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }

    @Override
    public void finalize() throws Throwable {
        super.finalize();
        executor.shutdown();
    }
}