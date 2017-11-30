package Routes;

import DataAccess.MongoDB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

/**
 * Adapter route for Helge's test data.
 */
@Path("/post")
public class TestRoute {

    private final Logger logger = LogManager.getLogger(TestRoute.class);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response postItem(InputStream json) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        try {
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
            return Response.status(400).entity("Syntax error! Could not read supplied JSON text.").type(MediaType.TEXT_PLAIN).build();
        }

        JSONObject jsonObject = null;
        String postTitle = null;
        String postText = null;
        int hanesstId = 0;
        String postType = null;
        int postParent = 0;
        String username = null;
        String passwordHash = null;
        String postUrl = null;

        try {
            jsonObject = new JSONObject(out.toString());
            postTitle = jsonObject.getString("post_title");
            postText = jsonObject.getString("post_text");
            hanesstId = jsonObject.getInt("hanesst_id");
            postType = jsonObject.getString("post_type");
            postParent = jsonObject.getInt("post_parent");
            username = jsonObject.getString("username");
            passwordHash = jsonObject.getString("pwd_hash");
            postUrl = jsonObject.getString("post_url");
        } catch (JSONException ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
            return Response.status(500).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
        }

        JsonObject values = Json.createObjectBuilder()
                .add("post_title", postTitle)
                .add("post_text", postText)
                .add("hanesst_id", hanesstId)
                .add("post_type", postType)
                .add("post_parent", postParent)
                .add("username", username)
                .add("pwd_hash", passwordHash)
                .add("post_url", postUrl)
                .build();

        Date date = new Date();
        String created = String.valueOf(date.getTime() * 1000); //Gets unix time as string.

        //Creating corresponding Item entity
        Document itemDoc = new Document("id", hanesstId)
                .append("deleted", false)
                .append("type", postType)
                .append("by", username)
                .append("timestamp", created)
                .append("text", postText)
                .append("dead", false)
                .append("parent", postParent)
                .append("poll", new JSONArray())
                .append("kids", new JSONArray())
                .append("url", postUrl)
                .append("score", 0)
                .append("title", postTitle)
                .append("parts", new JSONArray())
                .append("descendants", 0);

        //Creating corresponding User entity
        Document userDoc = new Document("id", username)
                .append("created", created)
                .append("delay", null)
                .append("about", null)
                .append("password", passwordHash)
                .append("karma", 0)
                .append("submitted", new JSONArray().put(hanesstId)); //Updates the Users submitted items here if he doesn't exist.

        //Returns status code 409 CONFLICT if item id already exists in the database.
        if (MongoDB.itemExists(hanesstId)){
            logger.info("Item with already existing hanesst_id was posted. Returned 409.");
            return Response.status(409).entity("CONFLICT! Item with the specified ID already exists.").type(MediaType.TEXT_PLAIN).build();

        }

        MongoDB.insertItem(itemDoc);
        System.out.println("Inserting item...");

        if (!MongoDB.userExists(username)) {
            MongoDB.insertUser(userDoc);
            System.out.println("Inserting user...");
        }
        else{
            //Updates the Users submitted items in the database if he already exists.
            Document userDocToUpdate = MongoDB.getUserDocument(username);
            ArrayList<Integer> submitted = (ArrayList<Integer>) userDocToUpdate.get("submitted");
            submitted.add(hanesstId);
            userDocToUpdate.put("submitted", submitted);
            MongoDB.updateUser(userDocToUpdate);
        }

        return Response.ok().entity(values.toString()).build();


    }

}
