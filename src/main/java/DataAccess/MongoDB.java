package DataAccess;

import Model.Item;
import Model.User;
import com.mongodb.*;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import javax.print.Doc;
import javax.validation.constraints.Null;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.util.JSON.parse;


public class MongoDB {

    // Setup of MONGO DB
    private static MongoClientURI connectionString = new MongoClientURI("mongodb://188.226.184.108:27017"); //TODO outcomment for prod use.
    //private static MongoClientURI connectionString = new MongoClientURI("mongodb://localhost:27017"); //TODO outcomment for local test purposes.

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
     * @return a String which contains all User Documents.
     */
    public static String getUsers() {
        StringBuilder users = new StringBuilder();
        MongoCursor<Document> cursor = userCollection.find().iterator();
        return iterateCollection(users, cursor);
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

        try{
            return document.toJson();
        }
        catch (NullPointerException ex){
            ex.printStackTrace();
        }
        return null;

    }

    /**
     * This method returns the specified Item as a Document.
     *
     * @param itemId Id of item to return as Document.
     * @return Item as Document type.
     */
    public static Document getItemDocument(int itemId) {
        Document document = itemCollection
                .find(eq("id", itemId))
                .first();

        return document;
    }

    /**
     * This method is used to retrieve all the items in the item collection
     *
     * @return - A String which contains all the documents.
     */
    public static String getAllItems() {

        StringBuilder items = new StringBuilder();
        MongoCursor<Document> cursor = itemCollection.find().iterator();
        return iterateCollection(items, cursor);
    }

    public static String getAllCategoryItems(Item.PostType postType) {
        StringBuilder items = new StringBuilder();
        MongoCursor<Document> cursor = itemCollection.find(eq("type", postType.toString())).iterator();
        return iterateCollection(items, cursor);
    }


    /**
     * This method is used to retrieve all the items in the item collection
     * @return - A String which contains all the documents.
     */
    /*public static String getNewestItems() {

        StringBuilder items = new StringBuilder();
        Block<Document> printBlock = new Block<Document>() {
            @Override
            public void apply(final Document document) {
                System.out.println(document.toJson());
                items.append(cursor.next().toJson());
                if (cursor.hasNext()) {
                    items.append(",");
                }
            }
        };
        MongoCursor<Document> cursor = itemCollection.find()
                                        .sort(new BasicDBObject("timestamp", -1)).forEach(printBlock);

        return iterateCollection(items, cursor);
    }
    */

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
                        .append("password", user.getPassword())
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
                        .append("password", userDocument.get("password"))
                        .append("submitted", userDocument.get("submitted"))));
    }

    /**
     * Checks if a User with the specified ID already exists in the database.
     *
     * @param userId User ID to search for.
     * @return "True", if the user already exists. "False", if he doesn't.
     */
    public static boolean userExists(String userId) {
        Document document = userCollection
                .find(eq("id", userId))
                .first();

        return document != null;

    }

    public static boolean itemExists(int itemId) {
        Document document = itemCollection
                .find(eq("id", itemId))
                .first();

        return document != null;
    }

    /**
     * Deletes an Item based on specified Item object.
     *
     * @param item Item to delete.
     * @throws NullPointerException if Item with specified ID does not exist.
     */
    public static void deleteItem(Item item) throws NullPointerException {
        Document itemToDelete = getItemDocument(item.getId());
        if (itemToDelete == null) throw new NullPointerException("Item with specified ID not found.");
        itemCollection.deleteOne(itemToDelete);
    }

    /**
     * Deletes an Item based on specified Item ID integer.
     *
     * @param itemId ID of Item to delete.
     * @throws NullPointerException if Item with specified ID does not exist.
     */
    public static void deleteItem(int itemId) throws NullPointerException {
        Document itemToDelete = getItemDocument(itemId);
        if (itemToDelete == null) throw new NullPointerException("Item with specified ID not found.");
        itemCollection.deleteOne(itemToDelete);
    }

    public void deleteUser(User user) {

    }

    public void postItem(Item item, User user) {

    }

    /**
     * The method pushes a new item into its parent.
     * Each comment is a kid to a post.
     *
     * @param parentID - The parent ID
     * @param kidID    - The comments ID / new item
     */
    public static void addComment(int parentID, int kidID) {
        // Finds parent document to look at attribute 'kids' value
        Document document = itemCollection.find(eq("id", parentID)).first();

        // The queries
        String jsonPush = "{$push:{kids:" + kidID + "}}";
        String jsonSet = "{$set:{kids:[" + kidID + "]}}";

        // parsed


        // First time insert of kid
        if (document.get("kids") == null) {
            DBObject set = (DBObject) parse(jsonSet);
            itemCollection.updateOne(eq("id", parentID),
                    (Bson) set);
        }
        // Continous push to array
        else {
            DBObject push = (DBObject) parse(jsonPush);
            itemCollection.updateOne(eq("id", parentID), (Bson) push);
        }

    }

    /**
     * This method intends to take the latest item number and increment it by one
     *
     * @return The return value is the latest item ID
     */
    public static int findLatestItem() {
        Document document = itemCollection.find().sort(new BasicDBObject("_id", -1)).limit(1).first();

//        Document document = new Document();
//        while(cursor.hasNext()) {
//            document = cursor.next();
//        }
//
//        if(document.isEmpty()) {
//            return 1;
//        }

        int lastID = document.getInteger("id");
        return lastID + 1;
    }

    /**
     * This method is made for the front-end to display the latest thirty stories easily.
     *
     * @return latest thirty stories as JSON
     */
    public static String getLastThirty() {
        StringBuilder items = new StringBuilder();
        MongoCursor<Document> cursor = itemCollection.find(new BasicDBObject("type", "story")).sort(new BasicDBObject("_id", -1)).limit(30).iterator();
        return iterateCollection(items, cursor);

    }

    private static String iterateCollection(StringBuilder items, MongoCursor<Document> cursor) {
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
}
