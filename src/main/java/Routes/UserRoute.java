package Routes;

import DataAccess.MongoDB;
import Model.User;
import com.sun.org.apache.bcel.internal.util.BCELifier;
import io.swagger.annotations.Api;
import io.swagger.models.auth.In;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import settings.Application;

import javax.json.*;
import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Path("/user")
@Api(value = "/user", description = "")
public class UserRoute {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(){
        return Response.ok().entity(MongoDB.getUsers()).build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") String id) {
        return Response.ok().entity(MongoDB.getUser(id)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postUser(InputStream json) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        JSONObject jsonObject = null;
        String password = null;
        String about = null;
        String created = null;
        String delay = null;
        String id = null;
        int karma = 0;
        JSONArray submitted = null;

        try {
            jsonObject = new JSONObject(out.toString());
            about = jsonObject.getString("about");
            password = jsonObject.getString("password");
            created = jsonObject.getString("created");
            delay = jsonObject.getString("delay");
            id = jsonObject.getString("id");
            karma = jsonObject.getInt("karma");
            submitted = jsonObject.getJSONArray("submitted");
        } catch (JSONException ex) {
            ex.printStackTrace();
            return Response.status(400).entity(ex.getMessage()).build();
        }


        //Correct format JsonArray
        JsonArrayBuilder builder = Json.createArrayBuilder();
        for (Object obj : submitted) {
            builder.add((int) obj);
        }

        JsonArray submittedCorrectFormat = builder.build();

        JsonObject values = Json.createObjectBuilder()
                .add("about", about)
                .add("password", password)
                .add("created", created)
                .add("delay", delay)
                .add("id", id)
                .add("karma", karma)
                .add("submitted", submittedCorrectFormat)
                .build();

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Document document = new Document("id", id)
                .append("created", created)
                .append("delay", delay)
                .append("about", about)
                .append("password", hashedPassword)
                .append("karma", karma)
                .append("submitted", submitted);

        //Returns status code 409 conflict if user id already exists in the database.
        if (MongoDB.userExists(id))
            return Response.status(409).entity("CONFLICT! User with the specified ID already exists.").build();

        MongoDB.insertUser(document);
        System.out.println("Inserting user...");

        return Response.ok().entity(values.toString()).build();

    }

    /**
     * Updates an existing user in the database.
     *
     * @param id of the User to change.
     * @return Appropriate Response code.
     */
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@PathParam("id") String id, InputStream json) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        JSONObject jsonObject = null;
        String about = null;
        String password = null;
        String created = null;
        String delay = null;
        int karma = 0;
        JSONArray submitted = null;

        try {
            jsonObject = new JSONObject(out.toString());
            about = jsonObject.getString("about");
            password = jsonObject.getString("password");
            created = jsonObject.getString("created");
            delay = jsonObject.getString("delay");
            karma = jsonObject.getInt("karma");
            submitted = jsonObject.getJSONArray("submitted");
        } catch (JSONException ex) {
            ex.printStackTrace();
        }

        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        Document userToUpdate = MongoDB.getUserDocument(id);
        userToUpdate.put("about", about);
        userToUpdate.put("password", hashedPassword);
        userToUpdate.put("created", created);
        userToUpdate.put("delay", delay);
        userToUpdate.put("karma", karma);
        userToUpdate.put("submitted", submitted);

        MongoDB.updateUser(userToUpdate);
        System.out.println("Updating user...");

        return Response.ok().entity(userToUpdate.toJson()).build();
    }


}
