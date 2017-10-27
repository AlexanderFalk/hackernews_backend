package Routes;

import io.swagger.annotations.Api;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.InputStream;

@Path("/")
@Api(value = "/", description = "This is the default window")
public class App {

    /**
     *
     * @return List of the most popular posts
     */
    @GET
    @Path("/news")
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(InputStream json) {
        return Response.ok().status(200).build();
    }


}
