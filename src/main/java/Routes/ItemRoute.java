package Routes;

import DataAccess.MongoDB;
import Model.Item;
import io.prometheus.client.Counter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
@Api(value = "/hackernews/item", description = "Here you will find operations about items",
    consumes = "application/json", produces = "application/json")
public class ItemRoute {

    private static final Counter requests = Counter.build()
            .name("item_requests_total").help("Total Requests for item related paths").register();

    private final Logger logger = LogManager.getLogger(ItemRoute.class.getName());

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
        requests.inc();
        if(MongoDB.getAllItems().isEmpty()) {
            logger.error("Returned code 204 - no items were retrieved.");
            return Response.status(204).entity("No Items has been retrieved. Could be a server error").build();
        }
        logger.info("All items were retrieved from host");

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
        requests.inc();
        if(MongoDB.getItem(id) == null) {
            logger.error("Returned code 204 - no item with id " + id + " found.",
                    new NullPointerException("No Item ID found"));
            return Response.status(204).entity("Item not found").build();
        }
        logger.info("Item ID: " + id + " retrieved!");
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
        try {
            requests.inc();
            BufferedReader reader = new BufferedReader(new InputStreamReader(json));
            StringBuilder out = new StringBuilder();
            String line;


            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            reader.close();


            // Generating timestamp
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);

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
            if (MongoDB.itemExists(item.getId())) {
                logger.info("An item with an already existing ID was posted. Returned code 409.");
                return Response.status(409).entity("CONFLICT! Item with the specified ID already exists.").build();

            }

            //Updates the Users submitted items in the database
            Document userDoc = MongoDB.getUserDocument(item.getBy());
            ArrayList<Integer> submitted = (ArrayList<Integer>) userDoc.get("submitted");
            submitted.add(item.getId());
            userDoc.put("submitted", submitted);
            MongoDB.updateUser(userDoc);

            MongoDB.insertItem(itemDocument);
            logger.info("Inserted item with ID: " + item.getId() + " !");
            return Response.status(200).entity(itemDocument.toJson()).build();
        } catch (ServerErrorException see) {
            logger.error("Server error occured for postITEM. See trace", see);
            return Response.status(500).build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "PUT",
            value = "Update an item",
            notes = "You can update an item by sending a PUT request with a item body included. PUT it at " +
                    "/hackernews/item/<itemID>",
            response = Item.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "A new comment has been added to its parent"),
                    @ApiResponse(code = 400, message = "Bad request. Check your payload"),
                    @ApiResponse(code = 409, message = "Indicating that the request could not be proceeded. " +
                            "Maybe a bad request body was received.")
            })
    public Response updateItem(InputStream stream) throws IOException {

        Item item = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder out = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            reader.close();

            JSONObject object = new JSONObject(out.toString());
            item = new Item(
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

            logger.info("Item with id: " + item.getId() + " was updated/put.");
            return Response.ok().entity("{ Update : ok }").build();
        }catch (NullPointerException error) {
            logger.error("Unable to update item with ID: " + item.getId());
            return Response.status(400).entity("{Update : failed}").build();
        }
    }


    @POST
    @Path("{itemId}/comment")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "POST",
            value = "Creates a comment to its parent",
            notes = "You can create a comment by posting HTTP Body as shown in the documentation",
            response = Item.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "A new comment has been added to its parent"),
                    @ApiResponse(code = 400, message = "Bad request. Check your payload"),
                    @ApiResponse(code = 409, message = "Indicating that the request could not be proceeded. " +
                            "Possible an item with same ID")
            })
    public Response comment(InputStream json, @PathParam("itemId") int parentID) throws IOException {
        requests.inc();
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
        int newID = MongoDB.findLatestItem();

        JSONObject object = new JSONObject(out.toString());
        Item item = new Item(
                newID,
                object.getBoolean("deleted"),
                object.getString("type"),
                object.getString("by"),
                timestamp,
                object.getString("text"),
                object.getBoolean("dead"),
                parentID,
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
        if (MongoDB.itemExists(item.getId())){
            logger.info("Comment (item) with id: " + item.getId() + " with an already existing ID was posted. Returned 409");
            return Response.status(409).entity("CONFLICT! Item with the specified ID already exists.").build();

        }

        MongoDB.updateUser(userDoc); //(2 of 2) Updates the Users submitted items in the database
        // Inserts the item into the database
        MongoDB.insertItem(itemDocument);

        // Updates the parent item's attribute: Kids[]
        MongoDB.addComment(parentID, newID);

        return Response.status(200).entity(itemDocument.toJson()).build();
    }


}
