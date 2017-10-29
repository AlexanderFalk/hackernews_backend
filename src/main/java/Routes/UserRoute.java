package Routes;

import DataAccess.MongoDB;
import Model.User;
import io.swagger.annotations.Api;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.json.Json;
import javax.json.JsonObject;
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
    public Response getUser(@PathParam("id") String id) {

        //Test data 1
        User user = new User();
        user.setId("foo");
        user.setCreated("");
        user.setDelay("");
        user.setAbout("About foo");
        user.setKarma(125);
        JSONArray submitted = new JSONArray();
        submitted.put(1);
        submitted.put(2);
        user.setSubmitted(submitted);

        //Test data 2
        User secondUser = new User();
        secondUser.setId("bar");
        secondUser.setCreated("");
        secondUser.setDelay("");
        secondUser.setAbout("About bar");
        secondUser.setKarma(225);
        JSONArray secondSubmitted = new JSONArray();
        secondSubmitted.put(3);
        secondSubmitted.put(4);
        secondUser.setSubmitted(secondSubmitted);

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
    public Response postUser(InputStream json) throws IOException, JSONException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        JSONObject jsonObject = null;
        String about = null;
        String created = null;
        String delay = null;
        String id = null;
        int karma = 0;
        String submitted = null;

        try{
            jsonObject = new JSONObject(out.toString());
            about = jsonObject.getString("about");
            created = jsonObject.getString("created");
            delay = jsonObject.getString("delay");
            id = jsonObject.getString("id");
            karma = jsonObject.getInt("karma");
            submitted = jsonObject.getString("submitted");
        }
        catch (JSONException ex){
            ex.printStackTrace();
        }

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
        System.out.println("Inserting user...");

//        User user = new User();
//        user.setAbout(about);
//        user.setCreated(created);
//        user.setDelay(delay);
//        user.setId(id);
//        user.setKarma(karma);
//        user.setSubmitted(Arrays.asList(1, 2, 3, 4));
//
//        userMap.put(user.getId(), user);

        return Response.ok().entity(values.toString()).build();

    }


}
