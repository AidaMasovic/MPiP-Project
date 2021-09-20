package mk.ukim.finki.mpipproject.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.Objects;

import mk.ukim.finki.mpipproject.R;

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";
    private static final int RC_GOOGLE_SIGN_IN = 9001;
    private static final int RC_FACEBOOK_LOGIN = 64206;

    private UserLoginListener userLoginListener;

    private FirebaseAuth mAuth;

    private EditText emailEt;
    private EditText passwordEt;
    private Button loginButton;

    private GoogleSignInClient mGoogleSignInClient;
    private Button googleSignInButton;

    private CallbackManager mCallbackManager;
    private LoginManager mFacebookLoginManager;
    private Button facebookLoginButton;

    private Button registerNowButton;

    private ProgressBar loadingPb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initProperties(view);

        initListenersAndCallbacks();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        userLoginListener = (UserLoginListener) context;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_GOOGLE_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    Log.d(TAG, "firebaseAuthWithGoogle:" + Objects.requireNonNull(account).getId());
                    firebaseAuthWithGoogle(account.getIdToken());
                } catch (ApiException e) {
                    Log.w(TAG, "Google sign in failed", e);
                }
            } else if (requestCode == RC_FACEBOOK_LOGIN) {
                Log.d(TAG, String.valueOf(requestCode));
                mCallbackManager.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void initProperties(View view) {
        mAuth = FirebaseAuth.getInstance();

        emailEt = view.findViewById(R.id.et_email_login);
        passwordEt = view.findViewById(R.id.et_password_login);
        loginButton = view.findViewById(R.id.login_button);

        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
        googleSignInButton = view.findViewById(R.id.google_sign_in_button);

        mCallbackManager = CallbackManager.Factory.create();
        mFacebookLoginManager = LoginManager.getInstance();
        facebookLoginButton = view.findViewById(R.id.facebook_login_button);

        registerNowButton = view.findViewById(R.id.register_now_button);

        loadingPb = view.findViewById(R.id.pb_loading);
    }

    private void initListenersAndCallbacks() {
        loginButton.setOnClickListener(v -> {
            String email = emailEt.getText().toString();
            String password = passwordEt.getText().toString();
            if (!email.isEmpty() && !password.isEmpty()) {
                loadingPb.setVisibility(View.VISIBLE);
                firebaseAuthWithEmailAndPassword(email, password);
            } else {
                Toast.makeText(
                        requireActivity(),
                        "Please enter an email and a password.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });

        googleSignInButton.setOnClickListener(v -> googleSignIn());

        facebookLoginButton.setOnClickListener(v -> facebookLogin());
        mFacebookLoginManager.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

        registerNowButton.setOnClickListener(
                v -> NavHostFragment.findNavController(LoginFragment.this)
                        .navigate(R.id.action_LoginFragment_to_RegisterFragment)
        );
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuthWithCredential(credential);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        firebaseAuthWithCredential(credential);
    }

    private void firebaseAuthWithEmailAndPassword(String email, String password) {
        Task<AuthResult> task = mAuth.signInWithEmailAndPassword(email, password);
        firebaseAuthOnComplete(task);
    }

    private void firebaseAuthWithCredential(AuthCredential credential) {
        Task<AuthResult> task = mAuth.signInWithCredential(credential);
        firebaseAuthOnComplete(task);
    }

    private void firebaseAuthOnComplete(Task<AuthResult> task) {
        task.addOnCompleteListener(requireActivity(), t -> {
            loadingPb.setVisibility(View.GONE);
            if (t.isSuccessful()) {
                Log.d(TAG, "signIn:success");
                try {
                    FirebaseUser currentUser = t.getResult().getUser();
                    if (isEmailVerified(currentUser)) {
                        userLoginListener.onUserLoggedIn();
                    } else {
                        Toast.makeText(
                                requireActivity(),
                                "Email address not verified.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            } else {
                Exception e = t.getException();
                Log.w(TAG, "signIn:failure", e);
                if (e instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(
                            requireActivity(),
                            "An account already exists with the same email address but " +
                                    "different sign-in credentials.",
                            Toast.LENGTH_LONG
                    ).show();
                } else if (e instanceof FirebaseAuthInvalidUserException) {
                    Toast.makeText(
                            requireActivity(),
                            "Invalid email address.",
                            Toast.LENGTH_LONG
                    ).show();
                } else if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    Toast.makeText(
                            requireActivity(),
                            "Incorrect password.",
                            Toast.LENGTH_LONG
                    ).show();
                } else if (e instanceof FirebaseNetworkException){
                    Toast.makeText(
                            requireActivity(),
                            "No internet connection.",
                            Toast.LENGTH_LONG
                    ).show();
                }
                mGoogleSignInClient.signOut();
                mFacebookLoginManager.logOut();
            }
        });
    }

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    public void facebookLogin() {
        mFacebookLoginManager.logIn(this, Arrays.asList("email", "public_profile"));
    }

    private boolean isEmailVerified(FirebaseUser currentUser) {
        return isProviderFacebook(currentUser) || currentUser.isEmailVerified();
    }

    private boolean isProviderFacebook(FirebaseUser currentUser) {
        return currentUser.getProviderData().get(1).getProviderId().equals("facebook.com");
    }

    public interface UserLoginListener {
        void onUserLoggedIn();
    }
}