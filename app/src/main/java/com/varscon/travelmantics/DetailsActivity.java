package com.varscon.travelmantics;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DetailsActivity extends AppCompatActivity {
    private String mPostId;
    private TextView mDealPrice, mDealTitle, mDealDescription;
    private ImageView mDisplayImage, imageMore;
    private DatabaseReference mDatabase;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mPostId = getIntent().getExtras().getString("post_id");
        mDealDescription = findViewById(R.id.dealDescription);
        mDealPrice = findViewById(R.id.dealPrice);
        mDealTitle = findViewById(R.id.dealTitle);
        mDisplayImage = findViewById(R.id.dealImage);
        imageMore = findViewById(R.id.options_image);


        mDatabase = FirebaseDatabase.getInstance().getReference("Deals").child(mPostId);

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                TravelModel travelModel = dataSnapshot.getValue(TravelModel.class);
                String dealTitle = travelModel.getDealTitle();
                String dealPrice = travelModel.getDealPrice();
                String dealDescription = travelModel.getDealDescription();
                String imgUrl = travelModel.getImageUrl();

                mDealPrice.setText(dealPrice);
                mDealTitle.setText(dealTitle);
                mDealDescription.setText(dealDescription);

                display(imgUrl);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        imageMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu(v);
            }
        });

    }

    private void display(String imgUrl) {
        Glide.with(DetailsActivity.this)
                .load(imgUrl)
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
                .into(mDisplayImage);
    }

    private void showMenu(View view) {
        PopupMenu popup = new PopupMenu(DetailsActivity.this, view);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.doc_menu, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.option_new:

                        startActivity(new Intent(DetailsActivity.this, PostActivity.class));
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
}
