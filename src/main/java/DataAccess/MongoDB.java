package DataAccess;

import Model.Item;
import Model.User;
import com.mongodb.Block;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

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
     *
     * @param userId - The user that you want to search for
     * @return JSON Object of the searched user
     */
    public static String getUser( String userId ) {
        // Creates a new Document to be returned
        Document document = userCollection
                            .find(eq("id", userId))
                            .first();

        return document.toJson();
    }

    /**
     * This method intends to return all users in one request
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


    /*public void insertItem( Item item ) {
        // Initialize new user
        item = new Item();

        Document doc = new Document("id", item.getId())
                .append("deleted", false) // Default value when created
                .append("type", item.getType())
                .append("by", item.getBy())
                .append("timestamp", item.getTimestamp())
                .append("text", item.getText())
                .append("dead", item.isDead())
                .append("parent", item.getParent())
                .append("poll", item.getPoll())
                .append("kids", item.getKids())
                .append("url", item.getUrl())
                .append("score", 0)
                .append("title", item.getTitle())
                .append("parts", item.getParts())
                .append("descendants", item.getDescendants());

        itemCollection.insertOne(doc);
    }*/

    /**
     * This method intends to retrieve a single item from the collection
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
     * This method is used to retrieve all the items in a item collection
     * @return - A document with all items
     */
    // NOT WORKING - STILL TRYING
    public static String getItems() {

        String items = "";
        MongoCursor<Document> cursor = itemCollection.find().iterator();
        try {
            
            while (cursor.hasNext()) {
                items = cursor.next().toJson();

            }

        } finally {
            cursor.close();
        }

        return items;
    }

    /**
     * This method is used to insert item
     * @param document - This parameter is the document pushed from the POST request
     */
    public static void insertItem( Document document ) {
        Document doc = new Document(document);

        itemCollection.insertOne(doc);
    }

    /*public void insertUser( User user ) {

        // Initialize new user
        user = new User();

        Document doc = new Document("id", user.getId())
                    .append("delay", user.getDelay())
                    .append("created", user.getCreated())
                    .append("karma", user.getKarma())
                    .append("about", user.getAbout())
                    .append("submitted", user.getSubmitted());


        userCollection.insertOne(doc);
    }*/

    public static void insertUser( Document document) {
        Document insertDoc = new Document(document);

        userCollection.insertOne(insertDoc);
    }

    public void updateItem( Item item ) {
        itemCollection.updateOne(eq("id", item.getId()),
                new Document("$set", new Document("id", item.getId())
                                                .append("deleted", item.isDeleted()) // Default value when created
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
                                                .append("descendants", item.getDescendants())));
    }

    public void updateUser( User user ) {
        userCollection.updateOne(eq("id", user.getId()),
                new Document("$set", new Document("id", user.getId())
                                                .append("delay", user.getDelay())
                                                .append("created", user.getCreated())
                                                .append("karma", user.getKarma())
                                                .append("about", user.getAbout())
                                                .append("submitted", user.getSubmitted())));
    }

    public void deleteItem( Item item ) {

    }

    public void deleteUser( User user ) {

    }

    public void postItem(Item item, User user) {

    }

    public void addComment(Item item, User user) {

    }
}
