package Routes;

import DataAccess.MongoDB;
import Model.Item;
import com.sun.org.apache.xalan.internal.xsltc.util.IntegerArray;
import io.swagger.annotations.Api;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.print.attribute.standard.Media;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Path("/item")
@Api(value = "/item", description = "At this route you can " +
        "GET an item, GET multiple items, or POST an item")
public class ItemRoute {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response item() {

        return Response.status(200).entity(MongoDB.getItems()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response item(@PathParam("id") int id) {
        // Test data
        /*Item item = new Item(1, false, "show",
                "foo", "1509214365", "This is an item",
                false, 1, Arrays.asList(0), Arrays.asList(2), "", 22,
                "FIRST ITEM", Arrays.asList(0), 0);*/
        return Response.status(200).entity(MongoDB.getItem(id)).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response postItem(InputStream json) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        System.out.println(out.toString()); //For debugging purposes.
        JSONObject object = new JSONObject(out.toString());
        Item item = new Item(
                object.getInt("id"),
                object.getBoolean("deleted"),
                object.getString("type"),
                object.getString("by"),
                object.getString("timestamp"),
                object.getString("text"),
                object.getBoolean("dead"),
                object.getInt("parent"),
                object.getString("url"),
                object.getInt("score"),
                object.getString("title")
        );

        //(1 of 2) Updates the Users submitted items in the database
        Document userDoc = MongoDB.getUserDocument(item.getBy());
        ArrayList<Integer> submitted = (ArrayList<Integer>) userDoc.get("submitted");
        submitted.add(item.getId());
        userDoc.put("submitted", submitted);

        Document itemDocument = new Document("id", item.getId())
                .append("deleted", item.isDeleted())
                .append("type", item.getType())
                .append("by", item.getBy())
                .append("timestamp", item.getTimestamp())
                .append("text", item.getText())
                .append("dead", item.isDead())
                .append("parent", item.getParent())
                .append("poll", item.getPoll())
                .append("kids", item.getKids())
                .append("url", item.getUrl())
                .append("score", item.getScore())
                .append("title", item.getTitle())
                .append("parts", item.getParts())
                .append("descendants", item.getDescendants());

        //Returns status code 409 conflict if item id already exists in the database.
        if (MongoDB.itemExists(item.getId()))
            return Response.status(409).entity("CONFLICT! Item with the specified ID already exists.").build();

        MongoDB.updateUser(userDoc); //(2 of 2) Updates the Users submitted items in the database
        MongoDB.insertItem(itemDocument);

        return Response.status(200).entity(itemDocument).build();
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateItem(InputStream stream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        JSONObject object = new JSONObject(out.toString());
        Item item = new Item(
                object.getInt("id"),
                object.getBoolean("deleted"),
                object.getString("type"),
                object.getString("by"),
                object.getString("timestamp"),
                object.getString("text"),
                object.getBoolean("dead"),
                object.getInt("parent"),
                object.getJSONArray("poll"),
                object.getJSONArray("kids"),
                object.getString("url"),
                object.getInt("score"),
                object.getString("title"),
                object.getJSONArray("parts"),
                object.getInt("descendants")
        );

        Document itemDocument = new Document("id", item.getId())
                .append("deleted", item.isDeleted())
                .append("type", item.getType())
                .append("by", item.getBy())
                .append("timestamp", item.getTimestamp())
                .append("text", item.getText())
                .append("dead", item.isDead())
                .append("parent", item.getParent())
                .append("poll", item.getPoll())
                .append("kids", item.getKids())
                .append("url", item.getUrl())
                .append("score", item.getScore())
                .append("title", item.getTitle())
                .append("parts", item.getParts())
                .append("descendants", item.getDescendants());


        MongoDB.updateItem(itemDocument);

        return Response.ok().entity("{ Update : ok }").build();
    }


}
