package rmi.graph;

import java.io.Serializable;

public class Request implements Serializable {
    public enum Type { ADD, DELETE, QUERY }
    public final Type type;
    public final int u;
    public final int v;
    // for QUERY, optional path result filled in Response
    public Request(Type type, int u, int v) {
        this.type = type;
        this.u = u;
        this.v = v;
    }
    public static Request add(int u, int v) { return new Request(Type.ADD, u, v); }
    public static Request del(int u, int v) { return new Request(Type.DELETE, u, v); }
    public static Request query(int u, int v) { return new Request(Type.QUERY, u, v); }
}