package dev.frilly.locket.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dev.frilly.locket.room.entities.UserProfile;

/**
 * Data-access object for {@link dev.frilly.locket.room.entities.UserProfile}.
 */
@Dao
public interface UserProfileDao {

    @Query("select * from UserProfile u where u.friendState = :state")
    List<UserProfile> getProfiles(UserProfile.FriendState state);

    @Query("select * from UserProfile")
    LiveData<List<UserProfile>> getProfiles();

    @Query("select * from UserProfile u where u.id = :id")
    UserProfile getById(final long id);

    @Query("select * from UserProfile u where lower(u.username) = :username limit 1")
    UserProfile getByUsername(final String username);

    @Query("DELETE FROM UserProfile WHERE friendState = :state")
    void deleteByFriendState(UserProfile.FriendState state);

    @Insert
    void insert(UserProfile... profiles);

    @Update
    void update(UserProfile profile);

    @Delete
    void delete(UserProfile profile);

}
