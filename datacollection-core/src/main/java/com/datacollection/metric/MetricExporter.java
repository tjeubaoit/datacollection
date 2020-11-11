package com.datacollection.metric;

import com.netflix.hystrix.contrib.metrics.eventstream.HystrixMetricsStreamServlet;
import com.soundcloud.prometheus.hystrix.HystrixPrometheusMetricsPublisher;
import com.datacollection.common.config.Properties;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;

/**
 * Export metric to outside world
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class MetricExporter {

    private Server server;
    private final int port;

    public MetricExporter(Properties props) {
        this.port = props.getIntProperty("metric.exporter.server.port", 5000);
    }

    public MetricExporter(int port) {
        this.port = port;
    }

    public void start() {
        if (server != null && server.isRunning()) {
            throw new IllegalStateException("MetricExporter server is running");
        }

        server = new Server(port);

        ServletHandler handler = new ServletHandler();
        handler.addServletWithMapping(HystrixMetricsStreamServlet.class, "/hystrix.stream");
        handler.addServletWithMapping(new ServletHolder(new MetricsServlet()), "/metrics");
        server.setHandler(handler);

        CollectorRegistry registry = CollectorRegistry.defaultRegistry;
        HystrixPrometheusMetricsPublisher.register("DataCollection", registry);

        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        if (server == null || !server.isRunning()) return;
        try {
            server.stop();
            while (!server.isStopped()) Thread.sleep(500);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void join() {
        if (server == null || !server.isRunning()) return;
        try {
            server.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
