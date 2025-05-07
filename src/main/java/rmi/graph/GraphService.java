package rmi.graph;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface GraphService extends Remote {
    /**
     * Process a batch of mixed requests: adds, deletes, and queries.
     * Returns a list of Responses in the same order.
     */
    List<Response> batch(List<Request> requests) throws RemoteException;
}
