package Model;

import java.util.List;

public class User {

    private String id;
    private String delay;
    private String created;
    private int karma;
    private String about;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDelay() {
        return delay;
    }

    public void setDelay(String delay) {
        this.delay = delay;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getKarma() {
        return karma;
    }

    public void setKarma(int karma) {
        this.karma = karma;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public List<Integer> getSubmitted() {
        return submitted;
    }

    public void setSubmitted(List<Integer> submitted) {
        this.submitted = submitted;
    }

    private List<Integer> submitted;

}
