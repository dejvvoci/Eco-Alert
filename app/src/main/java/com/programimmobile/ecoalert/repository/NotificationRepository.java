package com.programimmobile.ecoalert.repository;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.programimmobile.ecoalert.model.Notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {

    private static final String COLLECTION = "notifications";

    private static NotificationRepository instance;
    private final FirebaseFirestore db;

    public static NotificationRepository getInstance() {
        if (instance == null) instance = new NotificationRepository();
        return instance;
    }

    private NotificationRepository() {
        db = FirebaseFirestore.getInstance();
    }

    // ─── Dërgo notifikim te user-i ───────────────────────────────────────────

    public void sendNotification(Notification notification,
                                 ReportRepository.ActionCallback callback) {
        db.collection(COLLECTION)
                .add(notification)
                .addOnSuccessListener(ref -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ─── Merr notifikimet e një user-i ───────────────────────────────────────

    public void getUserNotifications(String userId,
                                     MutableLiveData<List<Notification>> liveData) {
        db.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (snapshots != null) {
                        List<Notification> list = new ArrayList<>();
                        list.addAll(snapshots.toObjects(Notification.class));
                        liveData.postValue(list);
                    }
                });
    }

    // ─── Numri i notifikimeve të palexuara ───────────────────────────────────

    public void getUnreadCount(String userId,
                               MutableLiveData<Integer> liveData) {
        db.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .addSnapshotListener((snapshots, error) -> {
                    if (snapshots != null) {
                        liveData.postValue(snapshots.size());
                    }
                });
    }

    // ─── Shëno si të lexuara ─────────────────────────────────────────────────

    public void markAllAsRead(String userId) {
        db.collection(COLLECTION)
                .whereEqualTo("userId", userId)
                .whereEqualTo("isRead", false)
                .get()
                .addOnSuccessListener(snapshots -> {
                    for (var doc : snapshots.getDocuments()) {
                        doc.getReference().update("isRead", true);
                    }
                });
    }
}