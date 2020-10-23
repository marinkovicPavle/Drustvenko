package com.marinkovic.drustvenko;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class RegisterActivity extends AppCompatActivity {

    // Constants
    public static final String CHAT_PREFS = "ChatPrefs";
    public static final String DISPLAY_NAME_KEY = "username";

    //private final static int GALLERY_CODE = 1;

    // TODO: Add member variables here:
    // UI references.
    private EditText mFirstName;
    private EditText mLastName;

    private EditText mEmailView;
    private EditText mUsernameView;
    private EditText mPasswordView;
    private EditText mConfirmPasswordView;
    //private ImageButton mProfilePic;


    // Firebase instance variables
    private FirebaseAuth mAuth;
    //private StorageReference mFirebaseStorage;

    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;

    private Uri resultUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirstName = (EditText) findViewById(R.id.register_firstname);
        mLastName = (EditText) findViewById(R.id.register_lastname);
        mEmailView = (EditText) findViewById(R.id.register_email);
        mPasswordView = (EditText) findViewById(R.id.register_password);
        mConfirmPasswordView = (EditText) findViewById(R.id.register_confirm_password);
        mUsernameView = (EditText) findViewById(R.id.register_username);
        /*mProfilePic = (ImageButton) findViewById(R.id.profile_image);

        mProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);

            }
        });*/


        // Keyboard sign in action
        mConfirmPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.integer.register_form_finished || id == EditorInfo.IME_NULL) {
                    attemptRegistration();
                    return true;
                }
                return false;
            }
        });

        // TODO: Get hold of an instance of FirebaseAuth
        mAuth = FirebaseAuth.getInstance();
        //mFirebaseStorage = FirebaseStorage.getInstance().getReference().child("Profile_Pics");
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("MUsers");
    }

    // Executed when Sign Up button is pressed.
    public void signUp(View v) {
        attemptRegistration();
    }

    private void attemptRegistration() {

        // Reset errors displayed in the form.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // TODO: Call create FirebaseUser() here
            createFirebaseUser();
        }
    }

    private boolean isEmailValid(String email) {
        // You can add more checking logic here.
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Add own logic to check for a valid password (minimum 6 characters)
        String confirmPassword = mConfirmPasswordView.getText().toString();
        if(confirmPassword.equals(password) && password.length() > 5)
            return true;
        else
            return false;
    }

    // TODO: Create a Firebase user
    private void createFirebaseUser() {
        final String email = mEmailView.getText().toString();
        final String password = mPasswordView.getText().toString();
        final String name = mFirstName.getText().toString().trim();
        final String lname = mLastName.getText().toString().trim();
        final String username = mUsernameView.getText().toString().trim();
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d("ChatApp", "creteUser onComplete: " + task.isSuccessful());

                if(!task.isSuccessful()){
                    Log.d("ChatApp", "user creation failed");
                    showErrorDialog("Registration attempt failed");
                } else {
                    saveDisplayName();

                    String userid = mAuth.getCurrentUser().getUid();

                    DatabaseReference currenUserDb = mDatabaseReference.child(userid);
                    currenUserDb.child("firstname").setValue(name);
                    currenUserDb.child("lastname").setValue(lname);
                    currenUserDb.child("username").setValue(username);

                    //send users to postList
                    Toast.makeText(RegisterActivity.this,"Registration successful. You will be redirected to login screen." , Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    finish();
                    startActivity(intent);

                   /* final StorageReference imagePath = mFirebaseStorage.child("Profile_Pics")
                            .child(resultUri.getLastPathSegment());

                    imagePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            imagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String userid = mAuth.getCurrentUser().getUid();

                                    DatabaseReference currenUserDb = mDatabaseReference.child(userid);
                                    currenUserDb.child("firstname").setValue(name);
                                    currenUserDb.child("lastname").setValue(lname);
                                    currenUserDb.child("username").setValue(username);
                                    currenUserDb.child("image").setValue(String.valueOf(uri));

                                    //send users to postList
                                    Toast.makeText(RegisterActivity.this,"Registration successful. You will be redirected to login screen." , Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    finish();
                                    startActivity(intent);
                                }
                            });
                        }
                    });*/
                }
            }
        });
    }

    // TODO: Save the display name to Shared Preferences
    private void saveDisplayName() {
        String displayName = mUsernameView.getText().toString();
        SharedPreferences prefs = getSharedPreferences(CHAT_PREFS, 0);
        prefs.edit().putString(DISPLAY_NAME_KEY, displayName).apply();
    }

    // TODO: Create an alert dialog to show in case registration failed
    private void showErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Oops")
                .setMessage(message)

                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Continue with delete operation
                    }
                })


                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

  /*  @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            Uri mImageUri = data.getData();

            CropImage.activity(mImageUri)
                    .setAspectRatio(1,1)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .start(this);



        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                Log.d("Pokazi mi", ""+resultUri);
                mProfilePic.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }*/

}
