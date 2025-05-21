package rmi.graph;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RmiGraphSampler extends AbstractJavaSamplerClient {

    private static final String HOST = "172.21.80.1";
    private static final int PORT = 1099;
    private static final double WRITE_PROB = 0.2;
    private static final int BATCH_SIZE = 5000;

    @Override
    public SampleResult runTest(JavaSamplerContext context) {
        SampleResult result = new SampleResult();
        try {
            Registry registry = LocateRegistry.getRegistry(HOST, PORT);
            GraphService stub = (GraphService) registry.lookup("GraphService");

            Random rand = ThreadLocalRandom.current();
            List<Request> batch = new ArrayList<>();

            for (int i = 0; i < BATCH_SIZE; i++) {
                double r = rand.nextDouble();
                int u = rand.nextInt(1, 11);
                int v = rand.nextInt(1, 11);
                if (r < WRITE_PROB / 2) batch.add(Request.add(u, v));
                else if (r < WRITE_PROB) batch.add(Request.del(u, v));
                else batch.add(Request.query(u, v));
            }

            result.sampleStart();  // Start timer
            List<Response> responses = stub.batch(batch);
            result.sampleEnd();    // Stop timer

            result.setSuccessful(true);
            result.setResponseMessage("Batch processed: " + responses.size() + " responses");
            result.setResponseData("Elapsed time: " + result.getTime() + " ms", "UTF-8");

        } catch (Exception e) {
            result.sampleEnd();
            result.setSuccessful(false);
            result.setResponseMessage("Exception: " + e.getMessage());
        }
        return result;
    }
}
