package rmi.graph;

import java.io.Serializable;
import java.util.List;

public class Response implements Serializable {
    public final Request.Type type;
    public final boolean success;        // for ADD and DELETE
    public final Integer pathLength;    // for QUERY

    private Response(Request.Type type, boolean success, Integer pathLength) {
        this.type = type;
        this.success = success;
        this.pathLength = pathLength;
    }
    public static Response addResp(boolean ok) { return new Response(Request.Type.ADD, ok, null); }
    public static Response delResp(boolean ok) { return new Response(Request.Type.DELETE, ok, null); }
    public static Response queryResp(Integer p) { return new Response(Request.Type.QUERY, true, p); }
}