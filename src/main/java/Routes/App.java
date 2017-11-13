package Routes;

import DataAccess.MongoDB;
import Model.Item;
import io.swagger.annotations.Api;
import org.bson.Document;
import org.json.JSONArray;
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
@Api(value = "/", description = "This is the default window")
public class App {

    /**
     * The three states the server can be in.
     */
    enum Status{
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
    public Response index(InputStream json) {
        return Response.ok().entity(MongoDB.getAllCategoryItems(Item.PostType.Story)).status(200).build();
    }

    /**
     * @return All posts with the "Show" type.
     */
    @GET
    @Path("/show")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getShowHn(){
        return Response.ok().entity(MongoDB.getAllCategoryItems(Item.PostType.Show)).build();
    }

    /**
     * @return All posts with the "Show" type.
     */
    @GET
    @Path("/ask")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAskHn(){
        return Response.ok().entity(MongoDB.getAllCategoryItems(Item.PostType.Ask)).build();
    }







    /**
     * Gets the status of the server.
     * @return "Alive", if up-and-running. "Update" if down for maintenance. "Down" for the system being down.
     */
    @GET
    @Path("/status")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getStatus(){
        return Response.ok().entity(Status.Alive.toString()).build(); //If this statement can be executed - the server is up and running.
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(InputStream json) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(json));
        StringBuilder out = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            out.append(line);
        }
        reader.close();

        JSONObject jsonObject = null;
        String id = null;
        String password = null;

        try {
            jsonObject = new JSONObject(out.toString());
            id = jsonObject.getString("id");
            password = jsonObject.getString("password");
        } catch (JSONException ex) {
            ex.printStackTrace();
            return Response.status(400).entity(ex.getMessage()).build();
        }

        Document userDoc = MongoDB.getUserDocument(id);
        if(passwordMatches(userDoc, password)) {
            String userJson = MongoDB.getUser(id);
            return Response.status(200).entity(userJson).build();
        }
        else return Response.status(401).entity("Wrong username/password combination").build();

    }

    /**
     * Returns the ID of latest item submitted to the system, as wanted by Helge.
     * @return the ID of the latest item as a plain text integer.
     */
    @GET
    @Path("/latest")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getLatestDigested(){
        int latestDigested = MongoDB.findLatestItem()-1; //Decrements by 1, as findLatestItem() method increments by 1 at the end.
        return Response.ok().entity(latestDigested).build();

    }

    /**
     * Checks the input password against the decrypted password in the DB.
     * @param userDoc User Document from database to check password on.
     * @param inputPassword plain text input password.
     * @return true, if passwords match. False, if they don't.
     */
    private Boolean passwordMatches(Document userDoc, String inputPassword){
        String hashedPw = userDoc.getString("password");
        return BCrypt.checkpw(inputPassword, hashedPw);
    }





}
