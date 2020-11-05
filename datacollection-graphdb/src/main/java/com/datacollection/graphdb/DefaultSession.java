package com.datacollection.graphdb;

import com.datacollection.graphdb.backend.BackendFactory;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.ListenableFuture;
import com.datacollection.common.concurrency.FutureAdapter;
import com.datacollection.common.utils.IterableAdapter;
import com.datacollection.common.utils.Utils;
import com.datacollection.graphdb.backend.EdgeBackend;
import com.datacollection.graphdb.backend.VertexBackend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Cài đặt mặc định của GraphSession sử dụng trong production
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class DefaultSession implements GraphSession {

    private final EdgeBackend edgeBackend;
    private final VertexBackend vertexBackend;

    public DefaultSession(BackendFactory factory) {
        this.edgeBackend = factory.getEdgeBackend();
        this.vertexBackend = factory.getVertexBackend();
    }

    @Override
    public EdgeBackend edgeRepository() {
        return this.edgeBackend;
    }

    @Override
    public VertexBackend vertexRepository() {
        return this.vertexBackend;
    }

    @Override
    public EdgeSet edges(Vertex vertex, Direction direction, String... edgeLabels) {
        Iterable<Edge> results;
        if (direction.equals(Direction.BOTH)) {
            results = Iterables.concat(
                    findEdges(vertex, Direction.OUT, edgeLabels),
                    findEdges(vertex, Direction.IN, edgeLabels));
        } else {
            results = findEdges(vertex, direction, edgeLabels);
        }
        return EdgeSet.convert(results);
    }

    @Override
    public EdgeSet edgesByAdjVertexLabels(Vertex vertex, Direction direction, String... adjVertexLabels) {
        Set<String> lbSets = new HashSet<>(Arrays.asList(adjVertexLabels));
        Direction[] directions = direction.equals(Direction.BOTH)
                ? new Direction[]{Direction.IN, Direction.OUT} : new Direction[]{direction};

        List<Iterable<Edge>> iterableList = new ArrayList<>(2);
        for (Direction d : directions) {
            Iterable<Edge> edges = rx.Observable.from(findEdges(vertex, d))
                    .filter(edge -> {
                        Vertex adj = Direction.OUT.equals(d) ? edge.inVertex() : edge.outVertex();
                        return lbSets.isEmpty() || lbSets.contains(adj.label());
                    })
                    .toBlocking().toIterable();
            iterableList.add(edges);
        }
        return EdgeSet.convert(Iterables.concat(iterableList));
    }

    private Iterable<Edge> findEdges(Vertex vertex, Direction direction, String... edgeLabels) {
        Preconditions.checkArgument(Utils.notEquals(direction, Direction.BOTH));

        if (edgeLabels.length == 0) {
            return edgeBackend.findByVertex(vertex, direction, null);
        } else if (edgeLabels.length == 1) {
            return edgeBackend.findByVertex(vertex, direction, edgeLabels[0]);
        }

        List<Iterable<Edge>> iterableList = new ArrayList<>(edgeLabels.length);
        for (String label : edgeLabels) {
            iterableList.add(edgeBackend.findByVertex(vertex, direction, label));
        }
        return Iterables.concat(iterableList);
    }

    @Override
    public ListenableFuture<EdgeSet> addEdges(long ts, Collection<Edge> edges) {
        return FutureAdapter.from(edgeBackend.saveAll(edges), EdgeSet::convert);
    }

    @Override
    public ListenableFuture<EdgeSet> removeEdge(long ts, String label, Vertex outVertex, Vertex inVertex) {
        Edge edge = Edge.create(label, outVertex, inVertex);
        return FutureAdapter.from(edgeBackend.delete(edge), EdgeSet::convert);
    }

    @Override
    public VertexSet vertices(String... labels) {
        if (labels.length == 0) return VertexSet.convert(vertexBackend.findAll());

        List<Iterable<Vertex>> listVertices = new ArrayList<>(labels.length);
        for (String label : labels) {
            listVertices.add(vertexBackend.findByLabel(label));
        }
        return VertexSet.convert(Iterables.concat(listVertices));
    }

    @Override
    public Optional<Vertex> vertex(String id, String label) {
        Vertex vertex = vertexBackend.findOne(label, id);
        return vertex != null ? Optional.of(vertex) : Optional.empty();
    }

    @Override
    public VertexSet vertices(Vertex vertex, Direction direction, String... edgeLabels) {
        return VertexSet.convert(IterableAdapter.from(
                edges(vertex, direction, edgeLabels),
                edge -> edge.outVertex().equals(vertex) ? edge.inVertex() : edge.outVertex()));
    }

    @Override
    public VertexSet verticesByAdjVertexLabels(Vertex vertex, Direction direction,
                                               String... adjVertexLabels) {
        return VertexSet.convert(IterableAdapter.from(
                edgesByAdjVertexLabels(vertex, direction, adjVertexLabels),
                edge -> edge.outVertex().equals(vertex) ? edge.inVertex() : edge.outVertex()));
    }

    @Override
    public ListenableFuture<VertexSet> addVertices(long ts, Collection<Vertex> vertices) {
        return FutureAdapter.from(vertexBackend.saveAll(vertices), VertexSet::convert);
    }

    @Override
    public ListenableFuture<VertexSet> deleteVertex(long ts, String id, String label) {
        return FutureAdapter.from(vertexBackend.delete(Vertex.create(id, label)), VertexSet::convert);
    }

    @Override
    public void close() {
        edgeBackend.close();
        vertexBackend.close();
    }
}
