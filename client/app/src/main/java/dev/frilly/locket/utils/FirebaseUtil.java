package dev.frilly.locket.utils;

import android.util.Log;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class FirebaseUtil {


    public static void getCurrentUserId(Callback<String> callback) {
        String firebaseUid = FirebaseAuth.getInstance().getUid();
        if (firebaseUid == null) {
            callback.onResult(null);
            return;
        }

        FirebaseFirestore.getInstance().collection("users")
                .whereEqualTo("firebaseUid", firebaseUid)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        String userId = document.getString("userId");
                        callback.onResult(userId);
                    } else {
                        callback.onResult(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error fetching userId", e);
                    callback.onResult(null);
                });
    }

    // Interface callback để xử lý dữ liệu bất đồng bộ
    public interface Callback<T> {
        void onResult(T result);
    }
    public static String currentUserId(){
        return FirebaseAuth.getInstance().getUid();
    }

    public static boolean isLoggedIn(){
        if(currentUserId()!=null){
            return true;
        }
        return false;
    }

    public static DocumentReference currentUserDetails(){
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("users");
    }

    public static DocumentReference getChatroomReference(String chatroomId){
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }

    public static CollectionReference getChatroomMessageReference(String chatroomId){
        return getChatroomReference(chatroomId).collection("chats");
    }

    public static String getChatroomId(String userId1,String userId2){
        if(userId1.hashCode()<userId2.hashCode()){
            return userId1+"_"+userId2;
        }else{
            return userId2+"_"+userId1;
        }
    }

    public static CollectionReference allChatroomCollectionReference(){
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static void getOtherUserFromChatroom(List<String> userIds, Callback<DocumentReference> callback) {
        getCurrentUserId(currentUserId -> {
            if (currentUserId == null) {
                callback.onResult(null);
                return;
            }

            for (String userId : userIds) {
                if (!userId.equals(currentUserId)) {
                    callback.onResult(FirebaseFirestore.getInstance().collection("users").document(userId));
                    return;
                }
            }
            callback.onResult(null);
        });
    }

    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH:MM").format(timestamp.toDate());
    }

    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    public static StorageReference  getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(FirebaseUtil.currentUserId());
    }

    public static StorageReference  getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("profile_pic")
                .child(otherUserId);
    }


}









