package dev.frilly.locket.room.entities;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDate;

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

    public LocalDate birthdate;

}
