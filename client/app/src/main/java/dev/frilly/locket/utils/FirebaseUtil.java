package dev.frilly.locket.utils;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

public class FirebaseUtil {
    public static String currentUserId() {return FirebaseAuth.getInstance().getUid();}

    public static DocumentReference currentUserDocument() {
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }
}


