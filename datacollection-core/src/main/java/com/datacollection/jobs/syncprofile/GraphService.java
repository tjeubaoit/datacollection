package com.datacollection.jobs.syncprofile;

import com.datacollection.collect.Constants;
import com.datacollection.common.utils.Utils;
import com.datacollection.graphdb.Direction;
import com.datacollection.graphdb.Edge;
import com.datacollection.graphdb.GraphSession;
import com.datacollection.graphdb.Versions;
import com.datacollection.graphdb.Vertex;
import com.datacollection.graphdb.traversal.Step;
import com.datacollection.graphdb.traversal.Traversal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class GraphService implements SyncProfileService {

    private final GraphSession session;
    private final Map<String, Integer> dataVersionMapping = new HashMap<>();

    public GraphService(GraphSession session) {
        this.session = session;
    }

    @Override
    public Map<String, Object> findProfileByUidAsMap(String uid) {
//        return traversal(Vertex.create(uid, Constants.PROFILE));
        return traversalSimple(Vertex.create(uid, Constants.PROFILE));
    }

    @Override
    public void close() {
        session.close();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> traversalSimple(Vertex vProfile) {
        Map<String, Object> map = new HashMap<>();
        for (Edge edge : session.edges(vProfile, Direction.OUT)) {
            // ignore edge out of date
            if (Versions.checkElementOutOfDate(edge, dataVersionMapping)) continue;

            Vertex v = edge.inVertex();
            if (v.label().startsWith(Constants.LOG)) continue;
            if (v.label().startsWith(Constants.POST)) continue;

            Set<Object> values = (Set<Object>) map.computeIfAbsent(edge.label(), k -> new HashSet<>());
            Vertex vFull = Constants.ACCOUNT.equals(edge.label()) || Constants.PHOTO.equals(v.label())
                    ? session.vertex(v.id(), v.label()).orElse(v) : v;

            Map<String, String> p = (Map<String, String>) vFull.properties();
            p.put("id", vFull.id());
            p.put("type", vFull.label());
            edge.properties().forEach((key, value) -> p.put(key, value.toString()));
            values.add(p);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> traversal(Vertex root) {
        Traversal traversal = Traversal.create(session, Traversal.Algorithm.BFS, root,
                Direction.OUT, edge -> {
                    Vertex v = edge.inVertex();
                    return Utils.notEquals(edge.label(), Constants.AVATAR)
                            && Utils.notEquals(v.label(), Constants.PROFILE)
                            && !v.label().startsWith(Constants.POST)
                            && !v.label().startsWith(Constants.LOG);
                });

        Map<String, Object> map = new HashMap<>();
        Step step;
        while ((step = traversal.nextStep()) != null) {
            Vertex v = step.vertex;
            Edge edge = step.edge;
            Set<Object> values = (Set<Object>) map.computeIfAbsent(edge.label(), k -> new HashSet<>());

            // ignore edge out of date
            if (Versions.checkElementOutOfDate(edge, dataVersionMapping)) continue;

            Vertex vFull = session.vertex(v.id(), v.label()).orElse(v);
            Map<String, String> p = (Map<String, String>) vFull.properties();

            p.put("id", vFull.id());
            p.put("type", vFull.label());
            edge.properties().forEach((key, value) -> p.put(key, value.toString()));
            values.add(p);
        }

        return map;
    }
}
