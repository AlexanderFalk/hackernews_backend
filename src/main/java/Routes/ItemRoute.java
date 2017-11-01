package Routes;

import DataAccess.MongoDB;
import Model.Item;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.bson.Document;
import org.json.JSONObject;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

@Path("/item")
@Api(value = "/item", description = "Here you will find operations about items",
    consumes = "application/json", produces = "application/json")
public class ItemRoute {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "GET",
            value = "Finds all items",
            notes = "Can be produced by making a GET request to /hackernews/item",
            response = Item.class,
            responseContainer = "String")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, message = "No content/items found"),
                    @ApiResponse(code = 200, message = "All items has been retrieved")
            })
    public Response item() {
        if(MongoDB.getAllItems().isEmpty()) {
            return Response.status(204).entity("No Items has been retrieved. Could be a server error").build();
        }
        return Response.status(200).entity(MongoDB.getAllItems()).build();
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "GET",
            value = "Finds a specific item",
            notes = "You can get a specific item by requesting to /hackernews/item/{id}",
            response = Item.class,
            responseContainer = "String")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, message = "(Content) Item ID not found"),
                    @ApiResponse(code = 200, message = "Item ID found successfully")
    })
    public Response item(@PathParam("id") int id) {

        if(MongoDB.getItem(id) == null) {
            return Response.status(204).entity("Item not found").build();
        }
        return Response.status(200).entity(MongoDB.getItem(id)).build();
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "POST",
            value = "Creates an item",
            notes = "You can create an item by posting HTTP Body as shown in the documentation",
            response = Item.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "A new item has been created"),
                    @ApiResponse(code = 400, message = "Bad request. Check your payload"),
                    @ApiResponse(code = 409, message = "Indicating that the request could not be proceeded. " +
                            "Possible an item with same ID")
            })
    public Response postItem(InputStream json) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;


        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        // Generating timestamp
        String timestamp =  String.valueOf(System.currentTimeMillis() / 1000);

        // Get latest ItemID and increment with one
        int id = MongoDB.findLatestItem();

        JSONObject object = new JSONObject(out.toString());
        Item item = new Item(
                id,
                object.getBoolean("deleted"),
                object.getString("type"),
                object.getString("by"),
                timestamp,
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

        //Returns status code 409 conflict if item id already exists in the database.
        if (MongoDB.itemExists(item.getId()))
            return Response.status(409).entity("CONFLICT! Item with the specified ID already exists.").build();

        //Updates the Users submitted items in the database
        Document userDoc = MongoDB.getUserDocument(item.getBy());
        ArrayList<Integer> submitted = (ArrayList<Integer>) userDoc.get("submitted");
        submitted.add(item.getId());
        userDoc.put("submitted", submitted);
        MongoDB.updateUser(userDoc);

        MongoDB.insertItem(itemDocument);

        return Response.status(200).entity(itemDocument.toJson()).build();
    }


    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
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
