package dev.frilly.locket.room.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

import dev.frilly.locket.room.entities.UserProfile;

/**
 * Data-access object for {@link dev.frilly.locket.room.entities.UserProfile}.
 */
@Dao
public interface UserProfileDao {

    @Query("select * from UserProfile")
    List<UserProfile> getProfiles();

    @Insert
    void insert(UserProfile... profiles);

    @Delete
    void delete(UserProfile profile);

}
