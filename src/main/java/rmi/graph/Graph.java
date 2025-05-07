package rmi.graph;

import java.util.*;

public class Graph {
    private final Map<Integer, Set<Integer>> adj = new HashMap<>();

    private void addNode(int u) {
        adj.putIfAbsent(u, new HashSet<>());
    }

    public boolean addEdge(int u, int v) {
        addNode(u);
        addNode(v);
        Set<Integer> edges = adj.get(u);
        if (edges.contains(v)) return false;
        edges.add(v);
        return true;
    }

    public boolean deleteEdge(int u, int v) {
        Set<Integer> edges = adj.get(u);
        if (edges == null || !edges.contains(v)) return false;
        edges.remove(v);
        return true;
    }

    public List<Integer> shortestPath(int src, int dst) {
        if (!adj.containsKey(src) || !adj.containsKey(dst)) {
            return Collections.emptyList();
        }
        Queue<Integer> q = new LinkedList<>();
        Map<Integer, Integer> prev = new HashMap<>();
        Set<Integer> visited = new HashSet<>();
        q.add(src);
        visited.add(src);
        while (!q.isEmpty()) {
            int u = q.poll();
            if (u == dst) break;
            for (int v : adj.getOrDefault(u, Collections.emptySet())) {
                if (!visited.contains(v)) {
                    visited.add(v);
                    prev.put(v, u);
                    q.add(v);
                }
            }
        }
        if (!prev.containsKey(dst) && src != dst) return Collections.emptyList();
        List<Integer> path = new ArrayList<>();
        int cur = dst;
        path.add(cur);
        while (cur != src) {
            cur = prev.get(cur);
            path.add(cur);
        }
        Collections.reverse(path);
        return path;
    }
}