package com.datacollection.graphdb.backend;

import com.datacollection.common.config.Configurable;

public interface BackendFactory extends Configurable {

    EdgeBackend getEdgeBackend();

    VertexBackend getVertexBackend();
}
