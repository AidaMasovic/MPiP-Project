package mk.ukim.finki.mpipproject.activity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import java.util.Objects;

import mk.ukim.finki.mpipproject.R;
import mk.ukim.finki.mpipproject.fragment.ProfileFragment;

public class MainActivity extends AppCompatActivity implements ProfileFragment.UserLogoutListener {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initProperties();

        setUpNavigation();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!isLoggedIn()) {
            navigateToAuthenticationActivity();
        }
    }

    @Override
    public void onUserLoggedOut() {
        logout();
    }

    private void initProperties() {
        mAuth = FirebaseAuth.getInstance();
    }

    private void setUpNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        NavigationUI.setupWithNavController(bottomNavigationView,
                Objects.requireNonNull(navHostFragment).getNavController());
    }

    private boolean isLoggedIn() {
        return this.mAuth.getCurrentUser() != null;
    }

    private void navigateToAuthenticationActivity() {
        Intent intent = new Intent(MainActivity.this, AuthenticationActivity.class);
        startActivity(intent);
        finish();
    }

    public void logout() {
        mAuth.signOut();
        navigateToAuthenticationActivity();
    }
}