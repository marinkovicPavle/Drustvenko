package com.marinkovic.drustvenko;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class PostsSentToMeActivity extends AppCompatActivity {

    private ListView postsSentToMeListView;
    private ArrayList<String> users;
    private ArrayAdapter adapter;
    private FirebaseAuth mAuth;
    private ArrayList<DataSnapshot> posts;
    private TextView txtDescription;
    private ImageView sentImageView;

    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts_sent_to_me);

        //kod za navigacioni meni
        bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavView_Bar);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        MenuItem inbox = menu.findItem(R.id.ic_inbox);
        inbox.setVisible(false);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.ic_profile:
                        Intent intent0 = new Intent(PostsSentToMeActivity.this, ProfileActivity.class);
                        startActivity(intent0);
                        break;

                    case R.id.ic_home:
                        Intent intent1 = new Intent(PostsSentToMeActivity.this, SocialMediaActivity.class);
                        startActivity(intent1);
                        break;

                    case R.id.ic_chaat:
                        Intent intent2 = new Intent(PostsSentToMeActivity.this, MainChatActivity.class);
                        startActivity(intent2);
                        break;
                }


                return false;
            }
        });

        mAuth = FirebaseAuth.getInstance();

        sentImageView = (ImageView) findViewById(R.id.sentImageView);
        txtDescription = (TextView) findViewById(R.id.txtDescription);

        txtDescription.setVisibility(View.INVISIBLE);
        sentImageView.setVisibility(View.INVISIBLE);

        postsSentToMeListView = findViewById(R.id.postsSentToMeListView);
        users = new ArrayList<>();
        posts = new ArrayList<>();

        adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, users);


        postsSentToMeListView.setAdapter(adapter);

        FirebaseDatabase.getInstance().getReference().child("MUsers").child(mAuth.getCurrentUser().getUid()).child("received_posts").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                users.add((String) dataSnapshot.child("fromWhom").getValue());
                posts.add(dataSnapshot);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

                int i = 0;

                for (DataSnapshot snapshot : posts) {

                    if (snapshot.getKey().equals(dataSnapshot.getKey())) {

                        posts.remove(i);
                        users.remove(i);

                    }

                    i++;

                }
                adapter.notifyDataSetChanged();


            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        postsSentToMeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                DataSnapshot dataSnapshot = posts.get(i);

                String imageLink = (String) dataSnapshot.child("imageLink").getValue();
                String description = (String) dataSnapshot.child("description").getValue();

                txtDescription.setVisibility(View.VISIBLE);
                sentImageView.setVisibility(View.VISIBLE);

                Glide.with(PostsSentToMeActivity.this).load(imageLink).diskCacheStrategy(DiskCacheStrategy.ALL).into(sentImageView);

                txtDescription.setText("Description: " +description);

            }
        });

        postsSentToMeListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final DataSnapshot dataSnapshot = posts.get(position);
                new AlertDialog.Builder(PostsSentToMeActivity.this)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")

                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                FirebaseDatabase.getInstance().getReference()
                                        .child("MUsers").child(mAuth.getCurrentUser()
                                        .getUid()).child("received_posts")
                                        .child(dataSnapshot.getKey()).removeValue();
                                FirebaseStorage.getInstance().getReference().child("myImages").child((String) dataSnapshot.child("imageIdentifier").getValue()).delete();
                            }
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                return false;
            }
        });

    }


}
