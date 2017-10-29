package Model;

import org.json.JSONArray;

/**
 * When you are using the system and you look at: Stories, comments, jobs,
 * Ask HNs and even polls, they are all related to the same data model: Item.
 * They're identified by their ids, which are unique integers,
 * and live under /v0/item/<id>.
 */
public class Item {

    private int id;
    private boolean deleted;
    private String type;
    private String by;
    private String timestamp;
    private String text;
    private boolean dead;
    private int parent;
    private JSONArray poll;
    private JSONArray kids;
    private String url;
    private int score;
    private String title;
    private JSONArray parts;
    private int descendants;

    // First time item creation
    public Item(int id, boolean deleted, String type, String by,
                String timestamp, String text, boolean dead,
                int parent,
                String url, int score, String title) {

        this.id = id;
        this.deleted = deleted;
        this.type = type;
        this.by = by;
        this.timestamp = timestamp;
        this.text = text;
        this.dead = dead;
        this.parent = parent;
        this.url = url;
        this.score = score;
        this.title = title;
    }

    public Item(int id, boolean deleted, String type, String by,
                String timestamp, String text, boolean dead,
                int parent, JSONArray poll, JSONArray kids,
                String url, int score, String title, JSONArray parts,
                int descendants) {

        this.id = id;
        this.deleted = deleted;
        this.type = type;
        this.by = by;
        this.timestamp = timestamp;
        this.text = text;
        this.dead = dead;
        this.parent = parent;
        this.poll = poll;
        this.kids = kids;
        this.url = url;
        this.score = score;
        this.title = title;
        this.parts = parts;
        this.descendants = descendants;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBy() {
        return by;
    }

    public void setBy(String by) {
        this.by = by;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public int getParent() {
        return parent;
    }

    public void setParent(int parent) {
        this.parent = parent;
    }

    public JSONArray getPoll() {
        return poll;
    }

    public void setPoll(JSONArray poll) {
        this.poll = poll;
    }

    public JSONArray getKids() {
        return kids;
    }

    public void setKids(JSONArray kids) {
        this.kids = kids;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public JSONArray getParts() {
        return parts;
    }

    public void setParts(JSONArray parts) {
        this.parts = parts;
    }

    public int getDescendants() {
        return descendants;
    }

    public void setDescendants(int descendants) {
        this.descendants = descendants;
    }
}
