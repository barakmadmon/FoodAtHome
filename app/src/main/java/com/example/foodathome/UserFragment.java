package com.example.foodathome;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * A simple menu showing user information
 */
public class UserFragment extends Fragment {
    TextView nameTV;
    Button signoutBtn;
    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameTV = view.findViewById(R.id.nameTV);
        signoutBtn = view.findViewById(R.id.signoutBtn);

        signoutBtn.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).disconnectUser();
            }
        });
    }

    public void updateUser() {
        user = FirebaseDataHandler.getCurrentUser();
        if(user != null) {
            nameTV.setText(user.getName());
        } else {
            nameTV.setText("no user logged in");
        }
    }

}