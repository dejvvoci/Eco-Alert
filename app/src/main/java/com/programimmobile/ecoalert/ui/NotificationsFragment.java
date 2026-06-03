package com.programimmobile.ecoalert.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.model.Notification;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import android.widget.Toast;

public class NotificationsFragment extends Fragment {

    private AuthViewModel authViewModel;
    private RecyclerView rvNotifications;
    private ProgressBar progressBar;
    private LinearLayout layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_notifications, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvNotifications = view.findViewById(R.id.rv_notifications);
        progressBar     = view.findViewById(R.id.progress_bar);
        layoutEmpty     = view.findViewById(R.id.layout_empty);

        authViewModel = new ViewModelProvider(requireActivity())
                .get(AuthViewModel.class);

        rvNotifications.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        String userId = authViewModel.getCurrentUserId();
        if (userId != null) {
            loadNotifications(userId);
        } else {
            progressBar.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    private void loadNotifications(String userId) {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.GONE);

        // Query e thjeshtë pa orderBy — shmang nevojën për index
        FirebaseFirestore.getInstance()
                .collection("notifications")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!isAdded()) return;

                    progressBar.setVisibility(View.GONE);

                    if (snapshots == null || snapshots.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvNotifications.setVisibility(View.GONE);
                        return;
                    }

                    List<Notification> notifications = new ArrayList<>();
                    notifications.addAll(
                            snapshots.toObjects(Notification.class));

                    // Rendo sipas datës në Java — pa nevojë për index
                    notifications.sort((a, b) -> {
                        if (a.getTimestamp() == null) return 1;
                        if (b.getTimestamp() == null) return -1;
                        return b.getTimestamp().compareTo(a.getTimestamp());
                    });

                    if (notifications.isEmpty()) {
                        layoutEmpty.setVisibility(View.VISIBLE);
                        rvNotifications.setVisibility(View.GONE);
                    } else {
                        layoutEmpty.setVisibility(View.GONE);
                        rvNotifications.setVisibility(View.VISIBLE);
                        setupAdapter(notifications);
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    layoutEmpty.setVisibility(View.VISIBLE);
                    rvNotifications.setVisibility(View.GONE);
                    Toast.makeText(requireContext(),
                            "Gabim: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void setupAdapter(List<Notification> notifications) {
        rvNotifications.setAdapter(
                new RecyclerView.Adapter<RecyclerView.ViewHolder>() {

                    @NonNull
                    @Override
                    public RecyclerView.ViewHolder onCreateViewHolder(
                            @NonNull ViewGroup parent, int viewType) {
                        View v = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.item_notification,
                                        parent, false);
                        return new RecyclerView.ViewHolder(v) {};
                    }

                    @Override
                    public void onBindViewHolder(
                            @NonNull RecyclerView.ViewHolder holder, int pos) {
                        Notification n = notifications.get(pos);

                        TextView tvTitle   = holder.itemView
                                .findViewById(R.id.tv_notif_title);
                        TextView tvMessage = holder.itemView
                                .findViewById(R.id.tv_notif_message);
                        TextView tvDate    = holder.itemView
                                .findViewById(R.id.tv_notif_date);
                        ImageView ivIcon   = holder.itemView
                                .findViewById(R.id.iv_notif_icon);
                        View viewUnread    = holder.itemView
                                .findViewById(R.id.view_unread);

                        boolean isApproved = Notification.TYPE_APPROVED
                                .equals(n.getType());

                        // Titulli sipas tipit
                        tvTitle.setText(isApproved
                                ? "✅ Raport i Aprovuar"
                                : "❌ Raport i Refuzuar");
                        tvTitle.setTextColor(ContextCompat.getColor(
                                requireContext(),
                                isApproved
                                        ? R.color.category_other
                                        : R.color.category_waste));

                        // Mesazhi
                        tvMessage.setText(n.getMessage() != null
                                ? n.getMessage() : "—");

                        // Data
                        if (n.getTimestamp() != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat(
                                    "dd/MM/yyyy HH:mm", Locale.getDefault());
                            tvDate.setText(sdf.format(n.getTimestamp()));
                        } else {
                            tvDate.setText("—");
                        }

                        // Ngjyra e ikonës
                        int iconColor = isApproved
                                ? R.color.category_other
                                : R.color.category_waste;
                        ivIcon.getBackground().setTint(
                                ContextCompat.getColor(
                                        requireContext(), iconColor));

                        // Tregues palexuar — vetëm vizual
                        viewUnread.setVisibility(
                                n.isRead() ? View.INVISIBLE : View.VISIBLE);
                        if (!n.isRead()) {
                            viewUnread.getBackground().setTint(
                                    ContextCompat.getColor(requireContext(),
                                            R.color.green_primary));
                        }
                    }

                    @Override
                    public int getItemCount() {
                        return notifications.size();
                    }
                });
    }

    @Override
    public void onPause() {
        super.onPause();
        // markAllAsRead() hequr — shkaktonte zhdukjen e notifikimeve
    }
}