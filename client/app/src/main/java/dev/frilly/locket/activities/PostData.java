package dev.frilly.locket.activities;

public class PostData {
    private String imageUrl;
    private String username;
    private String message;
    private String postTime;

    public PostData(String imageUrl, String username, String message, String postTime) {
        this.imageUrl = imageUrl;
        this.username = username;
        this.message = message;
        this.postTime = postTime;
    }

    public String getImageUrl() {
        return imageUrl;
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

