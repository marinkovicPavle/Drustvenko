package com.marinkovic.drustvenko;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


public class ProfileActivity extends AppCompatActivity {

    //private ImageButton profile_image;
    private EditText firstname_editText;
    private EditText lastname_editText;
    private EditText username_editText;
    private EditText email_editText;
    private EditText password_editText;
    private EditText confirmPassword_editText;
    private EditText oldPassword_editText;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mDatabase;
    private StorageReference mFirebaseStorage;

    private final static int GALLERY_CODE = 1;

    private Uri resultUri = null;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //kod za navigacioni meni
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        MenuItem profile = menu.findItem(R.id.ic_profile);
        profile.setVisible(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ic_home:
                        Intent intent0 = new Intent(ProfileActivity.this, SocialMediaActivity.class);
                        startActivity(intent0);
                        break;

                    case R.id.ic_inbox:
                        Intent intent1 = new Intent(ProfileActivity.this, PostsSentToMeActivity.class);
                        startActivity(intent1);
                        break;

                    case R.id.ic_chaat:
                        Intent intent2 = new Intent(ProfileActivity.this, MainChatActivity.class);
                        startActivity(intent2);
                        break;
                }


                return false;
            }
        });

        firstname_editText = (EditText) findViewById(R.id.register_firstname);
        lastname_editText = (EditText) findViewById(R.id.register_lastname);
        username_editText = (EditText) findViewById(R.id.register_username);
        email_editText = (EditText) findViewById(R.id.register_email);
        password_editText = (EditText) findViewById(R.id.register_password);
        confirmPassword_editText = (EditText) findViewById(R.id.register_confirm_password);
        oldPassword_editText = (EditText) findViewById(R.id.register_old_password);
       /* profile_image = (ImageButton) findViewById(R.id.profile_image);

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_CODE);
            }
        });*/

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mDatabase.getReference().child("MUsers");
        mFirebaseStorage = FirebaseStorage.getInstance().getReference().child("Profile_Pics");


        Log.d("taguj", ""+mAuth.getCurrentUser().getEmail());

        final String userid = mAuth.getCurrentUser().getUid();
        mDatabaseReference.child(userid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fName = ""+dataSnapshot.child("firstname").getValue(String.class);
                firstname_editText.setText(fName);
                String lName = ""+dataSnapshot.child("lastname").getValue(String.class);
                lastname_editText.setText(lName);
                String uName = ""+dataSnapshot.child("username").getValue(String.class);
                username_editText.setText(uName);
                String image_url = ""+dataSnapshot.child("image").getValue(String.class);
                email_editText.setText(mAuth.getCurrentUser().getEmail());
                //Picasso.get().load(image_url).into(profile_image);
                //Glide.with(ProfileActivity.this).load(image_url).diskCacheStrategy(DiskCacheStrategy.ALL).into(profile_image);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void saveOnClick(View view) {
        attemptUpdate();
    }

    private void attemptUpdate() {

        // Reset errors displayed in the form.
        email_editText.setError(null);
        password_editText.setError(null);

        // Store values at the time of the login attempt.
        String email = email_editText.getText().toString();
        String password = password_editText.getText().toString();
        String firstName = firstname_editText.getText().toString();
        String lastName = lastname_editText.getText().toString();
        String username = username_editText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            password_editText.setError(getString(R.string.error_invalid_password));
            focusView = password_editText;
            cancel = true;
        }

        if (TextUtils.isEmpty(firstName)) {
            firstname_editText.setError(getString(R.string.error_field_required));
            focusView = firstname_editText;
            cancel = true;
        }

        if (TextUtils.isEmpty(username)) {
            username_editText.setError(getString(R.string.error_field_required));
            focusView = username_editText;
            cancel = true;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastname_editText.setError(getString(R.string.error_field_required));
            focusView = lastname_editText;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            email_editText.setError(getString(R.string.error_field_required));
            focusView = email_editText;
            cancel = true;
        } else if (!isEmailValid(email)) {
            email_editText.setError(getString(R.string.error_invalid_email));
            focusView = email_editText;
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
        String confirmPassword = confirmPassword_editText.getText().toString();
        if(confirmPassword.equals(password) && password.length() > 5)
            return true;
        else
            return false;
    }


   /* private void checkMailExist(String email) {
        boolean check;
        mAuth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                    @Override
                    public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                        if (task.isSuccessful()) {
                            SignInMethodQueryResult result = task.getResult();
                            List<String> signInMethods = result.getSignInMethods();
                            if (signInMethods.contains(EmailAuthProvider.EMAIL_PASSWORD_SIGN_IN_METHOD)) {
                                Log.e("taguj", "Postoji mail " + task.getException());
                                //Toast.makeText(ProfileActivity.this,"Email alredy exists!", Toast.LENGTH_LONG).show();
                            } else {
                                Log.e("taguj", "Ne postoji mail" + task.getException());
                            }
                        } else {
                            Log.e("taguj", "", task.getException());
                        }
                    }
                });
    }*/

    // TODO: Create a Firebase user
    private void createFirebaseUser() {
        final String email = email_editText.getText().toString();
        final String password = password_editText.getText().toString();
        final String name = firstname_editText.getText().toString().trim();
        final String lname = lastname_editText.getText().toString().trim();
        final String username = username_editText.getText().toString().trim();

        final String userid = mAuth.getCurrentUser().getUid();

        Log.d("taguj", ""+password);

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference userRef = rootRef.child("MUsers");
        Query queries=userRef.orderByChild("username").equalTo(username);
        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Log.d("taguj", "Postoji username");
                    Toast.makeText(ProfileActivity.this, "Username alredy exists!", Toast.LENGTH_LONG).show();
                    username_editText.requestFocus();
                } else {
                    Log.d("taguj", "Ne postoji username");
                    AuthCredential credential = EmailAuthProvider.getCredential(mAuth.getCurrentUser().getEmail(), oldPassword_editText.getText().toString());
                    mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {

                                mAuth.getCurrentUser().updateEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) { //provera novog maila
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(ProfileActivity.this, "Email alredy registered!", Toast.LENGTH_LONG).show();
                                        } else {

                                            mAuth.getCurrentUser().updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) { //menjanje sifre
                                                    if (!task.isSuccessful()) {
                                                        Toast.makeText(ProfileActivity.this, "Something went wrong with password. Please try again later!", Toast.LENGTH_LONG).show();
                                                    } else {
                                                        DatabaseReference currenUserDb = mDatabaseReference.child(userid);
                                                        currenUserDb.child("firstname").setValue(name);
                                                        currenUserDb.child("lastname").setValue(lname);
                                                        currenUserDb.child("username").setValue(username);

                                                        Toast.makeText(ProfileActivity.this,"Profile updated!", Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                        }
                                    }
                                });
                            } else {
                                Toast.makeText(ProfileActivity.this, "Old password wrong!", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        queries.addListenerForSingleValueEvent(eventListener);
    }

   /* @Override
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
                profile_image.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }*/

    public void cancelOnClick(View view) {
        Intent intent = new Intent(ProfileActivity.this, MainChatActivity.class);
        finish();
        startActivity(intent);
    }
}
