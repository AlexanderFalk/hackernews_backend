package Model;

import sun.tools.tree.Node;

import java.util.List;

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
    private Node parent;
    private List<Integer> poll;
    private Node kids;
    private String url;
    private int score;
    private String title;
    private List<Integer> parts;
    private int descendants;

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

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public List<Integer> getPoll() {
        return poll;
    }

    public void setPoll(List<Integer> poll) {
        this.poll = poll;
    }

    public Node getKids() {
        return kids;
    }

    public void setKids(Node kids) {
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

    public List<Integer> getParts() {
        return parts;
    }

    public void setParts(List<Integer> parts) {
        this.parts = parts;
    }

    public int getDescendants() {
        return descendants;
    }

    public void setDescendants(int descendants) {
        this.descendants = descendants;
    }
}
