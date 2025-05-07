package rmi.graph;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GraphServiceImpl extends UnicastRemoteObject implements GraphService {
    private final Graph graph;
    private final ReentrantReadWriteLock rwLock;
    private final PrintWriter log;

    public GraphServiceImpl(Graph g) throws IOException {
        super();
        this.graph = g;
        this.rwLock = new ReentrantReadWriteLock(true);
        this.log = new PrintWriter("server.log", StandardCharsets.UTF_8);
    }

    @Override
    public List<Response> batch(List<Request> requests) throws RemoteException {
        long start = System.currentTimeMillis();
        List<Response> responses = new ArrayList<>(requests.size());
        for (Request req : requests) {
            switch (req.type) {
                case ADD:
                    rwLock.writeLock().lock();
                    try {
                        boolean ok = graph.addEdge(req.u, req.v);
                        responses.add(Response.addResp(ok));
                        log.printf("%s ADD %d->%d %s\n", timestamp(), req.u, req.v, ok?"OK":"NOOP");
                    } finally { rwLock.writeLock().unlock(); }
                    break;
                case DELETE:
                    rwLock.writeLock().lock();
                    try {
                        boolean ok = graph.deleteEdge(req.u, req.v);
                        responses.add(Response.delResp(ok));
                        log.printf("%s DELETE %d->%d %s\n", timestamp(), req.u, req.v, ok?"OK":"NOOP");
                    } finally { rwLock.writeLock().unlock(); }
                    break;
                case QUERY:
                    rwLock.readLock().lock();
                    try {
                        List<Integer> path = graph.shortestPath(req.u, req.v);
                        int pathLength = path.size() - 1;
                        responses.add(Response.queryResp(pathLength));
                        log.printf("%s QUERY %d->%d PATH %s\n", timestamp(), req.u, req.v, pathLength);
                    } finally { rwLock.readLock().unlock(); }
                    break;
            }
        }
        long elapsed = System.currentTimeMillis() - start;
        log.printf("%s BATCH COMPLETE %d ms\n", timestamp(), elapsed);
        log.flush();
        return responses;
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
    }
}