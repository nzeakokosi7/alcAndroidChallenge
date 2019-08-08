package com.varscon.travelmantics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class PostActivity extends AppCompatActivity {
    private TextInputEditText mDealTitle, mDealSubtitle, mDealDescription;
    private ImageView mUploadImage, mMoreImage;
    private TextView mSave;
    private Button mImgLoadBtn;
//    private String mDealTitleString, mDealPriceString, mDealDescriptions
    private DatabaseReference mDatabase;
    private Uri mImageUri;
    private static final int GALLERY_REQUEST = 3;
    private StorageReference mStorage;
    private ProgressDialog mProgress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mProgress = new ProgressDialog(this);


        mDealDescription = findViewById(R.id.dealDescriptionEdit);
        mDealSubtitle = findViewById(R.id.dealPriceEdit);
        mDealTitle = findViewById(R.id.dealTitleEdit);
        mUploadImage = findViewById(R.id.dealImage);
        mSave = findViewById(R.id.saveText);
        mMoreImage = findViewById(R.id.options_image);
        mImgLoadBtn = findViewById(R.id.button);

        mImgLoadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getGallery();
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
    }

    private void upload() {
        mProgress.setMessage("Updating ...");
        mProgress.setCancelable(false);

       final String deal_title = mDealTitle.getText().toString().trim();
       final String deal_price = mDealSubtitle.getText().toString().trim();
       final String deal_description = mDealDescription.getText().toString().trim();
       Toast.makeText(PostActivity.this, deal_description + deal_price + deal_title + mImageUri.toString(), Toast.LENGTH_SHORT).show();

        if(!TextUtils.isEmpty(deal_title) && !TextUtils.isEmpty(deal_price) && !TextUtils.isEmpty(deal_description) && mImageUri!=null) {
            final StorageReference filepath = mStorage.child("Deal_Images").child(mImageUri
                    .getLastPathSegment());

            filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                  filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                      @Override
                      public void onSuccess(Uri uri) {


                          DatabaseReference mDB = mDatabase.child("Deals").push();
                          mDB.child("dealTitle").setValue(deal_title);
                          mDB.child("dealPrice").setValue(deal_price);
                          mDB.child("dealDescription").setValue(deal_description);
                          mDB.child("imageUrl").setValue(uri.toString());

                          mProgress.dismiss();
                      }
                  });

                }
            });
        } else {
            Toast.makeText(PostActivity.this, "Incomplete Details", Toast.LENGTH_SHORT).show();
        }
    }

    private void getGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {

            mImageUri = data.getData();


            CropImage.activity(mImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(4, 3)
                    .start(PostActivity.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Toast toast = Toast.makeText(PostActivity.this, "Success" + result.getUri(), Toast.LENGTH_LONG);
                toast.show();

                mImageUri = result.getUri();
                mUploadImage.setImageURI(null);
                mUploadImage.setImageURI(mImageUri);


            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
//                FirebaseCrash.log(String.valueOf(error));


            }

        }
    }
}
