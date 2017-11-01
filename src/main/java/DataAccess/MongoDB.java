package DataAccess;

import Model.Item;
import Model.User;
import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Filters.eq;


public class MongoDB {

    // Setup of MONGO DB
    private static MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017");
    private static MongoClient client = new MongoClient(connectionString);

    // Access Database
    private static MongoDatabase database = client.getDatabase("hackernews");
    // Access collection
    private static MongoCollection<Document> itemCollection = database.getCollection("item");
    private static MongoCollection<Document> userCollection = database.getCollection("user");


    /**
     * @param userId - The user that you want to search for
     * @return JSON Object of the searched user
     */
    public static String getUser(String userId) {
        // Creates a new Document to be returned
        Document document = userCollection
                .find(eq("id", userId))
                .first();

        return document.toJson();
    }

    public static Document getUserDocument(String userId) {
        //Creates a new Document to be returned
        Document document = userCollection
                .find(eq("id", userId))
                .first();

        return document;
    }

    /**
     * This method intends to return all users in one request
     *
     * @return
     */
    public static String getUsers() {
        // Creates a new Document to be returned as a JSON
        Document document = null;
        for (Document doc : userCollection.find()) {
            document = doc;
        }

        return document.toJson();
    }

    /**
     * This method intends to retrieve a single item from the collection
     *
     * @param ID - The ID of the item that wants to be retrieved
     * @return The found item in JSON format
     */
    public static String getItem(int ID) {
        // Creates a new Document to be returned
        Document document = itemCollection
                .find(eq("id", ID))
                .first();

        return document.toJson();
    }

    /**
     * This method is used to retrieve all the items in the item collection
     *
     * @return - A String which contains all the documents.
     */
    public static String getItems() {

        StringBuilder items = new StringBuilder();
        MongoCursor<Document> cursor = itemCollection.find().iterator();
        try {

            items.append("[");
            while (cursor.hasNext()) {

                items.append(cursor.next().toJson());
                if (cursor.hasNext()) {
                    items.append(",");
                }
            }
            items.append("]");
        } finally {
            cursor.close();
        }

        return items.toString();
    }

    /**
     * This method is used to insert item
     *
     * @param document - This parameter is the document pushed from the POST request
     */
    public static void insertItem(Document document) {
        Document doc = new Document(document);

        itemCollection.insertOne(doc);
    }

    public static void insertUser(Document document) {
        Document insertDoc = new Document(document);

        userCollection.insertOne(insertDoc);
    }

    public static void updateItem(Document item) {
        itemCollection.updateOne(eq("id", item.getInteger("id")),
                new Document("$set", new Document("id", item.getInteger("id"))
                        .append("deleted", item.getBoolean("deleted")) // Default value when created
                        .append("type", item.getString("type"))
                        .append("by", item.getString("by"))
                        .append("timestamp", item.getString("timestamp"))
                        .append("text", item.getString("text"))
                        .append("dead", item.getBoolean("dead"))
                        .append("parent", item.getInteger("parent"))
                        .append("poll", item.get("poll"))
                        .append("kids", item.get("kids"))
                        .append("url", item.getString("url"))
                        .append("score", item.getInteger("score"))
                        .append("title", item.getString("title"))
                        .append("parts", item.get("parts"))
                        .append("descendants", item.getInteger("descendants"))));
    }

    public static void updateUser(User user) {
        userCollection.updateOne(eq("id", user.getId()),
                new Document("$set", new Document("id", user.getId())
                        .append("delay", user.getDelay())
                        .append("created", user.getCreated())
                        .append("karma", user.getKarma())
                        .append("about", user.getAbout())
                        .append("submitted", user.getSubmitted())));
    }

    /**
     * Updates the stored User with input User Document.
     *
     * @param userDocument that contains data to override stored user with.
     */
    public static void updateUser(Document userDocument) {
        userCollection.updateOne(eq("id", userDocument.get("id")),
                new Document("$set", new Document("id", userDocument.get("id"))
                        .append("delay", userDocument.get("delay"))
                        .append("created", userDocument.get("created"))
                        .append("karma", userDocument.get("karma"))
                        .append("about", userDocument.get("about"))
                        .append("submitted", userDocument.get("submitted"))));
    }

    /**
     * Checks if a User with the specified ID already exists in the database.
     * @param userId User ID to search for.
     * @return "True", if the user already exists. "False", if he doesn't.
     */
    public static boolean userExists(String userId) {
        Document document = userCollection
                .find(eq("id", userId))
                .first();

        return document != null;

    }

    public static boolean itemExists( int itemId ){
        Document document = itemCollection
                .find(eq("id", itemId))
                .first();

        return document != null;
    }

    public void deleteItem(Item item) {

    }

    public void deleteUser(User user) {

    }

    public void postItem(Item item, User user) {

    }

    public void addComment(Item item, User user) {

    }
}
