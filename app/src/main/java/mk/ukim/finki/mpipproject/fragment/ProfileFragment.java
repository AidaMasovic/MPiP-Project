package mk.ukim.finki.mpipproject.fragment;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.AccessToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

import mk.ukim.finki.mpipproject.R;

public class ProfileFragment extends Fragment {

    private FirebaseUser currentUser;

    private UserLogoutListener userLogoutListener;

    private ImageView profilePictureIv;
    private TextView fullNameTv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initProperties(view);

        bindData();

        initListeners(view);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        userLogoutListener = (UserLogoutListener) context;
    }

    private void initProperties(View view) {
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        profilePictureIv = view.findViewById(R.id.iv_profile_picture);
        fullNameTv = view.findViewById(R.id.tv_full_name);
    }

    private void bindData() {
        String photoUrl = "";
        try {
            photoUrl = currentUser.getPhotoUrl().toString();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            String queryParams = "?access_token=" + accessToken.getToken() + "&height=500";
            photoUrl += queryParams;
        }
        Glide.with(this).load(photoUrl).into(profilePictureIv);
        fullNameTv.setText(currentUser.getDisplayName());
    }

    private void initListeners(View view) {
        view.findViewById(R.id.btn_logout)
                .setOnClickListener(v -> userLogoutListener.onUserLoggedOut());
    }

    public interface UserLogoutListener {
        void onUserLoggedOut();
    }
}