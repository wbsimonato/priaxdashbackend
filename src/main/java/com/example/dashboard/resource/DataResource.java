package com.example.dashboard.resource;

import com.example.dashboard.service.HybridDataService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import java.util.Map;

@Path("/data")
public class DataResource {

    @Inject
    HybridDataService hybridService;

    @POST
    @Path("/hybrid/{datasourceId}")
    @Consumes("application/json")
    @Produces("application/json")
    public Response hybridQuery(
        @PathParam("datasourceId") String datasourceId,
        Map<String, Object> filters
    ) {
        try {
            return Response.ok(
                hybridService.executeHybridQuery(datasourceId, filters)
            ).build();
        } catch (Exception e) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(Map.of("error", e.getMessage()))
                .build();
        }
    }
}