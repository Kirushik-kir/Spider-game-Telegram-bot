package Models;

public class User {
    public long id;
    public String first_name;
    public String last_name;
    public String username;
    public Spider spider;

    public User(long id, String first_name, String last_name, String username, Spider spider) {
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.username = username;
        this.spider = spider;
    }

}