package Metrics;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.StreamingOutput;
import java.io.OutputStreamWriter;
import java.io.Writer;

@Path("metrics")
@Produces(MediaType.TEXT_PLAIN)
public class Metrics {

    @GET
    public StreamingOutput metrics() {
        return outputStream -> {
            try(Writer writer = new OutputStreamWriter(outputStream)) {
                TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            }
        };
    }

}
