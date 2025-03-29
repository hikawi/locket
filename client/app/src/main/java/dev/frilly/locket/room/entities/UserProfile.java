package dev.frilly.locket.room.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Represents an instance of a user, saved in the local SQL database.
 * <p>
 * WARNING: This type of UserProfile should be used for only FRIENDS, not
 * the user themself.
 */
@Entity
@TypeConverters(UserProfile.FriendStateConvert.class)
public class UserProfile implements Comparable<UserProfile> {

    @PrimaryKey
    public long id;

    @ColumnInfo(index = true)
    public String username;

    @ColumnInfo(index = true)
    public String email;

    public String avatarUrl;

    public long birthdate;

    public FriendState friendState;

    public static UserProfile testUserProfile(final long id, final String username, final String email) {
        final var user = new UserProfile();
        user.id = id;
        user.username = username;
        user.email = email;
        user.avatarUrl = String.format("https://avatars.githubusercontent.com/%s", username);
        user.birthdate = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC);
        user.friendState = FriendState.FRIEND;
        return user;
    }

    @Override
    public int compareTo(UserProfile userProfile) {
        if (friendState != userProfile.friendState)
            return friendState.compareTo(userProfile.friendState);
        return username.compareToIgnoreCase(userProfile.username);
    }

    public enum FriendState {

        FRIEND,
        SENT_REQUEST,
        RECEIVED_REQUEST,

    }

    /**
     * Represents an enumeration type converter for {@link UserProfile}.
     */
    public static class FriendStateConvert {

        @TypeConverter
        public static FriendState fromString(final String value) {
            if (value == null) {
                return null;
            }
            switch (value) {
                case "FRIEND":
                    return FriendState.FRIEND;
                case "SENT_REQUEST":
                    return FriendState.SENT_REQUEST;
                case "RECEIVED_REQUEST":
                    return FriendState.RECEIVED_REQUEST;
                default:
                    throw new IllegalArgumentException("Invalid value: " + value);
            }
        }

        @TypeConverter
        public static String toString(FriendState state) {
            return state != null ? state.name() : null;
        }

    }

}
