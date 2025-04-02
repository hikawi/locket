package dev.frilly.locket.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.frilly.locket.model.Post;

public class PostCache {
    private static final PostCache instance = new PostCache();
    private final List<Post> allPosts = new ArrayList<>();
    private final Set<String> usernames = new HashSet<>();

    public static PostCache getInstance() {
        return instance;
    }

    public void setPosts(List<Post> posts) {
        allPosts.clear();
        usernames.clear();
        allPosts.addAll(posts);
        for (Post post : posts) {
            usernames.add(post.getUsername());
        }
    }

    public List<Post> getPosts() {
        return allPosts;
    }

    public int getPostCount() {
        return allPosts.size();
    }

    public Set<String> getUsernames() {
        return usernames;
    }
}
