package Routes;

import io.swagger.annotations.Api;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Path("/")
@Api(value = "/", description = "This is the default window")
public class App {

    /**
     * @return List of the most popular posts
     */
    @GET
    @Path("/news")
    @Produces(MediaType.APPLICATION_JSON)
    public Response index(InputStream json) {
        return Response.ok().entity("Welcome!").status(200).build();
    }

    @GET
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(InputStream json) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        JSONObject jsonObject = null;
        String id = null;
        String password = null;

        try {
            jsonObject = new JSONObject(out.toString());
            id = jsonObject.getString("id");
            password = jsonObject.getString("password");
        } catch (JSONException ex) {
            ex.printStackTrace();
            return Response.status(400).entity(ex.getMessage()).build();
        }
    }


}
