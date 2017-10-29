package Routes;

import DataAccess.MongoDB;
import Model.User;
import io.swagger.annotations.Api;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.json.JsonObject;

import javax.json.Json;
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

    private HashMap<String, User> userMap = new HashMap<>();

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@PathParam("id") String id){

        //Test data 1
        User user = new User();
        user.setId("foo");
        user.setCreated("");
        user.setDelay("");
        user.setAbout("About foo");
        user.setKarma(125);
        user.setSubmitted(Arrays.asList(1, 2, 3));

        //Test data 2
        User secondUser = new User();
        secondUser.setId("bar");
        secondUser.setCreated("");
        secondUser.setDelay("");
        secondUser.setAbout("About bar");
        secondUser.setKarma(225);
        secondUser.setSubmitted(Arrays.asList(4, 5, 6));

        userMap.put(user.getId(), user);
        userMap.put(secondUser.getId(), secondUser);

        User foundUser = userMap.get(id);

        JsonObject jsonObject = Json.createObjectBuilder()
                .add("about", foundUser.getAbout())
                .add("created", foundUser.getCreated())
                .add("delay", foundUser.getDelay())
                .add("id", foundUser.getId())
                .add("karma", foundUser.getKarma())
                .add("submitted", foundUser.getSubmitted().toString())
                .build();

        return Response.ok().entity(jsonObject.toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postUser(InputStream json) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        while((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        JSONObject jsonObject = new JSONObject(out.toString());
        String about = jsonObject.getString("about");
        String created = jsonObject.getString("created");
        String delay = jsonObject.getString("delay");
        String id = jsonObject.getString("id");
        int karma = jsonObject.getInt("karma");
        String submitted = jsonObject.getString("submitted");

        JsonObject values = Json.createObjectBuilder()
                .add("about", about)
                .add("created", created)
                .add("delay", delay)
                .add("id", id)
                .add("karma", karma)
                .add("submitted", submitted)
                .build();

        Document document = new Document("id", id)
                .append("created", created)
                .append("delay", delay)
                .append("about", about)
                .append("karma", karma)
                .append("submitted", submitted);

        MongoDB.insertUser(document);
        System.out.println("Inserting user");

        User user = new User();
        user.setAbout(about);
        user.setCreated(created);
        user.setDelay(delay);
        user.setId(id);
        user.setKarma(karma);
        user.setSubmitted(Arrays.asList(1, 2, 3, 4));

        userMap.put(user.getId(), user);

        return Response.ok().entity(out.toString()).build();

    }





}
