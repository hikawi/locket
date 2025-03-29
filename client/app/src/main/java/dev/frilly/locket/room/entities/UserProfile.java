package dev.frilly.locket.room.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents an instance of a user, saved in the local SQL database.
 * <p>
 * WARNING: This type of UserProfile should be used for only FRIENDS, not
 * the user themself.
 */
@Entity
public class UserProfile {

    @PrimaryKey
    public long id;

    @ColumnInfo(index = true)
    public String username;

    @ColumnInfo(index = true)
    public String email;

    public String avatarUrl;

    public long birthdate;

    public static UserProfile testUserProfile(final long id, final String username, final String email) {
        final var user = new UserProfile();
        user.id = id;
        user.username = username;
        user.email = email;
        user.avatarUrl = String.format("https://avatars.githubusercontent.com/%s", username);
        user.birthdate = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        return user;
    }

}
