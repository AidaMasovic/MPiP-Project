package mk.ukim.finki.mpipproject.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import mk.ukim.finki.mpipproject.R;

public class RegisterFragment extends Fragment {

    private static final String TAG = "RegisterFragment";
    private static final int RC_PICK_IMAGE = 71;
    private static final String DEFAULT_PROFILE_PICTURE_URL =
            "https://firebasestorage.googleapis.com/v0/b/mpip-project.appspot.com/o/img%2Fdefault" +
                    "_profile_picture.png?alt=media&token=700cf70b-2fb9-40b2-8e70-b5c9d751d657";

    private FirebaseAuth mAuth;
    private StorageReference storageReference;

    private Uri imageFilePath;

    private EditText firstNameEt;
    private EditText lastNameEt;
    private EditText emailEt;
    private EditText passwordEt;
    private Button chooseProfilePictureButton;
    private ImageView chosenProfilePictureIv;
    private Button registerButton;
    private ProgressBar loadingPb;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initProperties(view, savedInstanceState);

        initListeners();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == RC_PICK_IMAGE) {
                try {
                    imageFilePath = data.getData();
                    chooseProfilePictureButton.setCompoundDrawablesWithIntrinsicBounds(0,
                            0, R.drawable.ic_checked, 0);
                    Glide.with(this).load(imageFilePath).into(chosenProfilePictureIv);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        if (imageFilePath != null) {
            outState.putString("imageFilePath", imageFilePath.toString());
        }
    }

    private void initProperties(View view, Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        if (savedInstanceState != null) {
            imageFilePath = Uri.parse(savedInstanceState.getString("imageFilePath"));
        } else {
            imageFilePath = Uri.parse(DEFAULT_PROFILE_PICTURE_URL);
        }

        firstNameEt = view.findViewById(R.id.et_first_name);
        lastNameEt = view.findViewById(R.id.et_last_name);
        emailEt = view.findViewById(R.id.et_email_register);
        passwordEt = view.findViewById(R.id.et_password_register);
        chooseProfilePictureButton = view.findViewById(R.id.choose_profile_picture_button);
        chosenProfilePictureIv = view.findViewById(R.id.iv_chosen_profile_picture);
        Glide.with(this).load(imageFilePath).into(chosenProfilePictureIv);
        registerButton = view.findViewById(R.id.register_button);
        loadingPb = view.findViewById(R.id.pb_loading);
    }

    private void initListeners() {
        chooseProfilePictureButton.setOnClickListener(view -> chooseImage());

        registerButton.setOnClickListener(view -> {
            String firstName = firstNameEt.getText().toString();
            String lastName = lastNameEt.getText().toString();
            String email = emailEt.getText().toString();
            String password = passwordEt.getText().toString();

            if (!email.isEmpty() && !password.isEmpty()) {
                loadingPb.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser currentUser = task.getResult().getUser();
                        if (!imageFilePath.toString().equals(DEFAULT_PROFILE_PICTURE_URL)) {
                            setUserProfileInfoWithImage(currentUser, firstName, lastName);
                        } else {
                            setUserProfileInfo(currentUser, firstName, lastName, imageFilePath);
                        }
                    } else {
                        loadingPb.setVisibility(View.GONE);
                        Exception e = task.getException();
                        Log.w(TAG, "register:failure", e);
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(
                                    requireActivity(),
                                    "An account with that email address already exists.",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else if (e instanceof FirebaseAuthWeakPasswordException) {
                            Toast.makeText(
                                    requireActivity(),
                                    "The password is not strong enough.",
                                    Toast.LENGTH_LONG
                            ).show();
                        } else if (e instanceof FirebaseNetworkException) {
                            Toast.makeText(
                                    requireActivity(),
                                    "No internet connection.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
                });
            } else {
                Toast.makeText(
                        requireActivity(),
                        "Please complete all the fields.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, RC_PICK_IMAGE);
    }

    private void setUserProfileInfoWithImage(FirebaseUser currentUser, String firstName,
                                             String lastName) {
        String storageLocation = "img/" + currentUser.getUid();
        StorageReference ref = storageReference.child(storageLocation);
        ref.putFile(imageFilePath).addOnCompleteListener(task1 -> {
            if (task1.isSuccessful()) {
                ref.getDownloadUrl().addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        Uri photoUri = task2.getResult();
                        setUserProfileInfo(currentUser, firstName, lastName, photoUri);
                    } else {
                        loadingPb.setVisibility(View.GONE);
                        Log.w(TAG, "getDownloadUrl:failure", task1.getException());
                    }
                });
            } else {
                loadingPb.setVisibility(View.GONE);
                Toast.makeText(
                        requireActivity(),
                        "Image upload unsuccessful.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void setUserProfileInfo(FirebaseUser currentUser, String firstName, String lastName,
                                    Uri photoUri) {
        UserProfileChangeRequest request =
                new UserProfileChangeRequest.Builder()
                        .setDisplayName(firstName + " " + lastName)
                        .setPhotoUri(photoUri)
                        .build();
        currentUser.updateProfile(request).addOnCompleteListener(task -> {
            loadingPb.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                currentUser.sendEmailVerification();
                showEmailVerificationAlertDialog(currentUser.getEmail());
            } else {
                Log.w(TAG, "register:failure", task.getException());
                Toast.makeText(
                        requireActivity(),
                        "Registration unsuccessful.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private void showEmailVerificationAlertDialog(String email) {
        AlertDialog alertDialog = new AlertDialog.Builder(requireActivity()).create();
        alertDialog.setTitle("VERIFY YOUR EMAIL ADDRESS\n");
        String message = "We have sent an email to " + email + "\n\n" +
                "You need to verify your email to continue.\n" +
                "If you have not received the verification email, please check your " +
                "\"Spam\" or \"Bulk Email\" folder.\n";
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Okay",
                (dialog, which) -> NavHostFragment.findNavController(RegisterFragment.this)
                        .navigate(R.id.action_RegisterFragment_to_LoginFragment));
        alertDialog.show();
    }
}