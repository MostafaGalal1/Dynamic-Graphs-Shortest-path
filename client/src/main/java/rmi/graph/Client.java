package rmi.graph;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Client {
    public static void main(String[] args) {
        double writeProb = 0.2;
        if (args.length>=1) try { writeProb = Double.parseDouble(args[0]); } catch (Exception ignored) {}
        try (PrintWriter log = new PrintWriter(new FileWriter("client.log", true))) {
            Registry registry = LocateRegistry.getRegistry("172.21.80.1", 1099);
            GraphService stub = (GraphService) registry.lookup("GraphService");
            Random rand = ThreadLocalRandom.current();
            while (true) {
                int batchSize = rand.nextInt(1,6);
                List<Request> batch = new ArrayList<>();
                for (int i=0;i<batchSize;i++) {
                    double r = rand.nextDouble();
                    int u = rand.nextInt(1,11), v = rand.nextInt(1,11);
                    if (r<writeProb/2) batch.add(Request.add(u,v));
                    else if (r<writeProb) batch.add(Request.del(u,v));
                    else batch.add(Request.query(u,v));
                }
                long t0 = System.currentTimeMillis();
                List<Response> res = stub.batch(batch);
                long elapsed = System.currentTimeMillis()-t0;
                log.printf("%s BATCH size=%d elapsed=%dms\n", now(), batchSize, elapsed);
                for (int i=0; i<batch.size(); i++) {
                    Request rq = batch.get(i);
                    Response rp = res.get(i);
                    log.printf("  %s %d->%d -> %s\n", rq.type, rq.u, rq.v
                            , (rp.type==Request.Type.QUERY?rp.pathLength:rp.success));
                }
                log.flush();
                int sleep = rand.nextInt(1,11);
                Thread.sleep(sleep*1000L);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static String now() {
        return new java.text.SimpleDateFormat("HH:mm:ss").format(new Date());
    }
}