package dev.frilly.locket.model;

public class Post {
    private String fileUrl;
    private String username;
    private String message;
    private String postTime;

    public Post(String fileUrl, String username, String message, String postTime) {
        this.fileUrl = fileUrl;
        this.username = username;
        this.message = message;
        this.postTime = postTime;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getPostTime() {
        return postTime;
    }
}

