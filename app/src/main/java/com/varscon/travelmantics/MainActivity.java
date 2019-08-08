package com.varscon.travelmantics;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ActionBar toolbar;
    private Handler mHandler;
    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<TravelModel, TravelViewHolder> firebaseRecyclerAdapter;
    private RecyclerView mTravelList;
    private ImageView mOptions;
    private LinearLayoutManager mLayoutmanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);


        mFirebaseAuth = FirebaseAuth.getInstance();
        check_authentication_status();



    }

    private void check_authentication_status() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //check if user is authenticated
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if(user != null) {

                    String email = user.getEmail();
                    Log.d("MainActivity", "onAuthStateChanged: user not null" );
                    //check if email is verified
//                    if (!user.isEmailVerified()) {

                    //set up the view
                    setContentView(R.layout.activity_main);
                    initialize();

//                    //initialize bottom tab
//                    loadFragment(new HomeFragment());
//                    BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
//                    navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

                } else {
                    Log.d("MainActivity", "onAuthStateChanged: user  null" );
                    Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                    intent.setAction(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                            | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
//                    finish();
//
                }
            }
        };
    }

    private void initialize() {
        mLayoutmanager = new LinearLayoutManager(this);
        mTravelList = findViewById(R.id.travel_list);
        mTravelList.setHasFixedSize(true);
        mLayoutmanager.setStackFromEnd(true);
        mLayoutmanager.setReverseLayout(true);
        mTravelList.setLayoutManager(mLayoutmanager);
        mOptions = findViewById(R.id.options_image);
        mDatabase = FirebaseDatabase.getInstance().getReference("Deals");
        showView(mDatabase);
//                    mHandler = new Handler();
        mOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });
    }

    private void showMenu(View view) {
        PopupMenu popup = new PopupMenu(MainActivity.this, view);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.doc_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.option_new:

                        startActivity(new Intent(MainActivity.this, PostActivity.class));
                        return true;
//                        break;
                    case R.id.option_logout:


                        return true;
//                        break;

                }

                return false;
            }
        });

        popup.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mFirebaseAuth.addAuthStateListener(mAuthStateListener);

//        loadFragment(mCurrentFragment);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    private void showView(DatabaseReference PropDB) {


        FirebaseRecyclerOptions<TravelModel> options =
                new FirebaseRecyclerOptions.Builder<TravelModel>()
                        .setQuery(PropDB, TravelModel.class)
                        .build();

        firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<TravelModel, TravelViewHolder>(

                options

        ) {

            @NonNull
            @Override
            public TravelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_layout, parent, false);

                return new TravelViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(final TravelViewHolder viewHolder, final int position, final TravelModel model) {
                final String post_key = getRef(position).getKey();

                viewHolder.mTravelSubtitle.setText(model.getDealPrice());
                viewHolder.mTravelTitle.setText(model.getDealTitle());
                viewHolder.setPropertyImage(MainActivity.this,  model.getImageUrl());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent newIntent = new Intent(MainActivity.this, DetailsActivity.class);
                        newIntent.putExtra("post_id", post_key);
                        startActivity(newIntent);
                    }
                });
            }
        };
        firebaseRecyclerAdapter.startListening();
        mTravelList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class TravelViewHolder extends RecyclerView.ViewHolder {

        View mView;

        TextView mTravelTitle, mTravelSubtitle, mTravelPrice;
        ImageView mTravelDisplay;
        ProgressBar mProgressLoader;


        public TravelViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            mTravelPrice = itemView.findViewById(R.id.travel_price);
            mTravelTitle = itemView.findViewById(R.id.travel_title);
            mTravelSubtitle = itemView.findViewById(R.id.travel_subtitle);
            mTravelDisplay = itemView.findViewById(R.id.travel_thumbnail);
//            mProgressLoader = itemView.findViewById(R.id.progress_loader);

        }

        public void setPropertyImage(final Context ctx, String displayImage) {
            Glide.with(ctx)
                    .load(displayImage)
                    .thumbnail(0.1f)
                    .placeholder(R.color.cardview_dark_background)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
//                            mProgressLoader.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
//                            mProgressLoader.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(mTravelDisplay);
        }
    }
}

