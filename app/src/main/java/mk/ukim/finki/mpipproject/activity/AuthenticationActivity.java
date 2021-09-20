package mk.ukim.finki.mpipproject.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import java.util.List;

import mk.ukim.finki.mpipproject.R;
import mk.ukim.finki.mpipproject.fragment.LoginFragment;

public class AuthenticationActivity extends AppCompatActivity
        implements LoginFragment.UserLoginListener {

    private FirebaseAuth mAuth;

    private GoogleSignInClient mGoogleSignInClient;

    private LoginManager mFacebookLoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);

        initProperties();

        initListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (isLoggedIn()) {
            navigateToMainActivity();
        } else {
            mGoogleSignInClient.signOut();
            mFacebookLoginManager.logOut();
        }
    }

    @Override
    public void onUserLoggedIn() {
        navigateToMainActivity();
    }

    private void initProperties() {
        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mFacebookLoginManager = LoginManager.getInstance();
    }

    private void initListeners() {
        mAuth.addAuthStateListener(firebaseAuth -> {
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            if (currentUser != null && !isEmailVerified(currentUser)) {
                firebaseAuth.signOut();
            }
        });
    }

    private boolean isLoggedIn() {
        return this.mAuth.getCurrentUser() != null;
    }

    private boolean isEmailVerified(FirebaseUser currentUser) {
        return isProviderFacebook(currentUser) || currentUser.isEmailVerified();
    }

    private boolean isProviderFacebook(FirebaseUser currentUser) {
        return currentUser.getProviderData().get(1).getProviderId().equals("facebook.com");
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(AuthenticationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}