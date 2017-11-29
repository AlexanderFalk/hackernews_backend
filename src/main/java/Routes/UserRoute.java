package Routes;

import DataAccess.MongoDB;
import Model.User;
import io.prometheus.client.Counter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Path("/user")
@Api(value = "/hackernews/user", description = "Here you will find operations about users",
        consumes = "application/json", produces = "application/json")
public class UserRoute {

    private final Logger logger = LogManager.getLogger(UserRoute.class.getName());

    private static final Counter requests = Counter.build()
            .name("user_requests_total").help("Total Requests for user related paths").register();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "GET",
            value = "Finds all users",
            notes = "Can be produced by making a GET request to /hackernews/user",
            response = User.class,
            responseContainer = "String")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, message = "No content/users found"),
                    @ApiResponse(code = 200, message = "All users has been retrieved")
            })
    public Response getUsers(){
        requests.inc();
        try {
            logger.info("All users has been requested and send back in a response");
            return Response.ok().entity(MongoDB.getUsers()).build();
        }catch (ServerErrorException error) {
            logger.error("Could not reply with a response of all users. See trace: ", error);
            return Response.status(204).entity("{Info : No users found}").build();
        }

    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "GET",
            value = "Finds a specific user",
            notes = "Can be produced by making a GET request to /hackernews/user/<ID>",
            response = User.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 204, message = "Specific user not found"),
                    @ApiResponse(code = 200, message = "The user has been retrieved successfully")
            })
    public Response getUser(@PathParam("id") String id) {
        requests.inc();
        try {
            logger.info("Found user with ID: " + id);
            return Response.ok().entity(MongoDB.getUser(id)).build();
        }catch (ServerErrorException error) {
            logger.warn("User with ID: " + id + " not found", error);
            return Response.status(204).entity("{Info: User not found}").build();
        }

    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "POST",
            value = "Creates a new user",
            notes = "Send a POST request to the following url : /hackernews/user/<ID> to create a new user",
            response = User.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "There were an issue translating the HTTP body. Check for errors"),
                    @ApiResponse(code = 200, message = "User has been created successfully")
            })
    public Response postUser(InputStream json) throws IOException, JSONException {
        requests.inc();
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        JSONObject jsonObject;
        String password;
        String about;
        String created;
        String delay;
        String id;
        int karma;
        JSONArray submitted;

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
            logger.error(ex.getMessage());
            return Response.status(400).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
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
        if (MongoDB.userExists(id)){
            logger.info("An User with already existing ID was posted. Returned code 409. ");
            return Response.status(409).entity("CONFLICT! User with the specified ID already exists.").type(MediaType.TEXT_PLAIN).build();

        }

        MongoDB.insertUser(document);
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
    @ApiOperation(
            httpMethod = "PUT",
            value = "Updates a specific user",
            notes = "Can update a user by sending a PUT request to /hackernews/user/<ID> with the respectable HTTP body",
            response = User.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Issue with translating the HTTP body"),
                    @ApiResponse(code = 200, message = "User has been updated successfully")
            })
    public Response updateUser(@PathParam("id") String id, InputStream json) throws IOException {
        requests.inc();
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        JSONObject jsonObject;
        String about;
        String password;
        String created;
        String delay;
        int karma;
        JSONArray submitted;

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
            logger.error("There were an issue formatting to JSON", ex.getMessage());
            return Response.status(400).entity(ex.getMessage()).type(MediaType.TEXT_PLAIN).build();
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
