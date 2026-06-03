package com.programimmobile.ecoalert.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserRepository {

    private static final String COLLECTION_ADMINS = "admins";
    private static UserRepository instance;
    private final FirebaseFirestore db;

    public static UserRepository getInstance() {
        if (instance == null) instance = new UserRepository();
        return instance;
    }

    private UserRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public void checkIsAdmin(String userId, MutableLiveData<Boolean> liveData) {
        db.collection(COLLECTION_ADMINS)
                .document(userId)
                .get()
                .addOnSuccessListener(doc ->
                        liveData.postValue(doc.exists()))
                .addOnFailureListener(e ->
                        liveData.postValue(false));
    }
}