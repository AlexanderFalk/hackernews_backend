package Routes;

import DataAccess.MongoDB;
import Model.Item;
import Model.User;
import io.prometheus.client.Counter;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Path("/")
@Api(value = "/hackernews/", description = "This is the default window")
public class App {

    private final Logger logger = LogManager.getLogger(App.class.getName());

    private static final Counter requests = Counter.build()
            .name("default_requests_total").help("Total Requests for default paths").register();

    /**
     * The three states the server can be in.
     */
    enum Status {
        Alive,
        Update,
        Dead
    }

    /**
     * @return List of the most popular posts
     */
    @GET
    @Path("/news")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "GET",
            value = "News",
            notes = "Get all newest items",
            response = Item.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 500, message = "Could not return all newest items. Might be server error"),
                    @ApiResponse(code = 200, message = "Successfully retrieved all newest items.")
            })
    public Response index() {
        requests.inc();
        try {
            logger.info("Successfully retrieved all newest items");
            return Response.ok().entity(MongoDB.getAllCategoryItems(Item.PostType.Story)).status(200).build();
        } catch (NullPointerException error) {
            logger.error("There were an issue retrieving all newest items. See trace: ", error);
            return Response.status(500).build();
        }
    }

    /**
     * @return All posts with the "Show" type.
     */
    @GET
    @Path("/show")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "GET",
            value = "Show",
            notes = "Get all items that has type \"SHOW\"",
            response = Item.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 500, message = "Could not return all items with SHOW as type. Might be server error"),
                    @ApiResponse(code = 200, message = "Successfully retrieved all items with SHOW as type.")
            })
    public Response getShowHn() {
        requests.inc();
        try {
            logger.info("Successfully retrieved all items with SHOW as type");
            return Response.ok().entity(MongoDB.getAllCategoryItems(Item.PostType.Show)).build();
        } catch (NullPointerException error) {
            logger.error("There were an issue retrieving all items with SHOW as type. See trace: ", error);
            return Response.status(500).build();
        }
    }

    /**
     * @return All posts with the "Show" type.
     */
    @GET
    @Path("/ask")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "GET",
            value = "Ask",
            notes = "Get all items that has type \"ASK\"",
            response = Item.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 500, message = "Could not return all items with ASK as type. Might be server error"),
                    @ApiResponse(code = 200, message = "Successfully retrieved all items with ASK as type.")
            })
    public Response getAskHn() {
        requests.inc();
        try {
            logger.info("Successfully retrieved all items with ASK as type");
            return Response.ok().entity(MongoDB.getAllCategoryItems(Item.PostType.Ask)).build();
        } catch (NullPointerException error) {
            logger.error("There were an issue retrieving all items with ASK as type. See trace: ", error);
            return Response.status(500).build();
        }

    }


    /**
     * Gets the status of the server.
     *
     * @return "Alive", if up-and-running. "Update" if down for maintenance. "Down" for the system being down.
     */
    @GET
    @Path("/status")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            httpMethod = "GET",
            value = "Status",
            notes = "Server is up and running if a response is returned.",
            response = App.class,
            responseContainer = "String")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 200, message = "Server is up and running"),

            })
    public Response getStatus() {
        requests.inc();
        logger.info("Application is up and running. Woop!");
        return Response.ok().entity(Status.Alive.toString()).build(); //If this statement can be executed - the server is up and running.
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(
            httpMethod = "POST",
            value = "Login",
            notes = "Send a POST request with ID and Password as HTTP Body to login",
            response = User.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 400, message = "Not able to parse the HTTP body. Check format"),
                    @ApiResponse(code = 200, message = "Successfully logged in."),
                    @ApiResponse(code = 401, message = "Wrong username/password. Try again.")
            })
    public Response login(InputStream json) throws IOException {
        requests.inc();
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        JSONObject jsonObject;
        String id;
        String password;

        try {
            jsonObject = new JSONObject(out.toString());
            id = jsonObject.getString("id");
            password = jsonObject.getString("password");
        } catch (JSONException ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
            return Response.status(400).entity(ex.getMessage()).build();
        }

        Document userDoc = MongoDB.getUserDocument(id);
        if (passwordMatches(userDoc, password)) {
            String userJson = MongoDB.getUser(id);
            logger.info("Username: '" + id + "' logged in successfully.");
            return Response.status(200).entity(userJson).build();
        } else{
            logger.info("Username: '" + id + "' - wrong password.");
            return Response.status(401).entity("Wrong username/password combination").build();
        }

    }

    /**
     * Returns the ID of latest item submitted to the system, as wanted by Helge.
     *
     * @return the ID of the latest item as a plain text integer.
     */
    @GET
    @Path("/latest")
    @Produces(MediaType.TEXT_PLAIN)
    @ApiOperation(
            httpMethod = "GET",
            value = "Finds the latest item",
            notes = "Send a request to /hackernews/latest and you will retrieve the latest number",
            response = Item.class,
            responseContainer = "JSON")
    @ApiResponses(
            value = {
                    @ApiResponse(code = 500, message = "Latest item could not be returned. Could be server error"),
                    @ApiResponse(code = 200, message = "Latest item has been returned")
            })
    public Response getLatestDigested() {
        requests.inc();
        int latestDigested = MongoDB.findLatestItem() - 1; //Decrements by 1, as findLatestItem() method increments by 1 at the end.
        try {
            logger.info("Returned latest: " + latestDigested);
            return Response.ok().entity(String.valueOf(latestDigested)).build();
        } catch (ServerErrorException error) {
            logger.error("Could not return the latest item. See trace: " , error);
            return Response.status(500).build();
        }


    }

    /**
     * Checks the input password against the decrypted password in the DB.
     *
     * @param userDoc       User Document from database to check password on.
     * @param inputPassword plain text input password.
     * @return true, if passwords match. False, if they don't.
     */
    private Boolean passwordMatches(Document userDoc, String inputPassword) {
        String hashedPw = userDoc.getString("password");
        return BCrypt.checkpw(inputPassword, hashedPw);
    }


}
