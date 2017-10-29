package Routes;

import DataAccess.MongoDB;
import Model.Item;
import io.swagger.annotations.Api;
import org.bson.Document;
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

        while((line = reader.readLine()) != null) {
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
                object.getString("url"),
                object.getInt("score"),
                object.getString("title")
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

        MongoDB.insertItem(itemDocument);

        return Response.status(200).entity(itemDocument).build();
    }



}
