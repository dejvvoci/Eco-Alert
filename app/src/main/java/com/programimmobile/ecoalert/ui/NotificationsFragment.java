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
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.programimmobile.ecoalert.R;
import com.programimmobile.ecoalert.model.Notification;
import com.programimmobile.ecoalert.repository.NotificationRepository;
import com.programimmobile.ecoalert.viewmodel.AuthViewModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

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
            progressBar.setVisibility(View.VISIBLE);
            loadNotifications(userId);
        }
    }

    private void loadNotifications(String userId) {
        MutableLiveData<List<Notification>> liveData = new MutableLiveData<>();

        NotificationRepository.getInstance()
                .getUserNotifications(userId, liveData);

        liveData.observe(getViewLifecycleOwner(), notifications -> {
            progressBar.setVisibility(View.GONE);

            if (notifications == null || notifications.isEmpty()) {
                rvNotifications.setVisibility(View.GONE);
                layoutEmpty.setVisibility(View.VISIBLE);
                return;
            }

            rvNotifications.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);

            // Shëno si të lexuara
            NotificationRepository.getInstance().markAllAsRead(userId);

            // Adapter inline
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

                            TextView tvTitle   = holder.itemView.findViewById(
                                    R.id.tv_notif_title);
                            TextView tvMessage = holder.itemView.findViewById(
                                    R.id.tv_notif_message);
                            TextView tvDate    = holder.itemView.findViewById(
                                    R.id.tv_notif_date);
                            ImageView ivIcon   = holder.itemView.findViewById(
                                    R.id.iv_notif_icon);
                            View viewUnread    = holder.itemView.findViewById(
                                    R.id.view_unread);

                            // Tipi i njoftimit
                            boolean isApproved = Notification.TYPE_APPROVED
                                    .equals(n.getType());
                            tvTitle.setText(isApproved
                                    ? "✅ Raport i Aprovuar"
                                    : "❌ Raport i Refuzuar");
                            tvTitle.setTextColor(ContextCompat.getColor(
                                    requireContext(),
                                    isApproved ? R.color.category_other
                                            : R.color.category_waste));

                            tvMessage.setText(n.getMessage() != null
                                    ? n.getMessage() : "—");

                            // Data
                            if (n.getTimestamp() != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat(
                                        "dd/MM/yyyy HH:mm", Locale.getDefault());
                                tvDate.setText(sdf.format(n.getTimestamp()));
                            }

                            // Ikona sipas tipit
                            int iconColor = isApproved
                                    ? R.color.category_other
                                    : R.color.category_waste;
                            ivIcon.getBackground().setTint(
                                    ContextCompat.getColor(requireContext(), iconColor));

                            // Tregues palexuar
                            viewUnread.setVisibility(
                                    n.isRead() ? View.INVISIBLE : View.VISIBLE);
                            if (!n.isRead()) {
                                viewUnread.getBackground().setTint(
                                        ContextCompat.getColor(requireContext(),
                                                R.color.green_primary));
                            }
                        }

                        @Override
                        public int getItemCount() { return notifications.size(); }
                    });
        });
    }
}