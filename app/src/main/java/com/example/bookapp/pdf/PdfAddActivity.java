package com.example.bookapp.pdf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityPdfAddBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfAddActivity extends AppCompatActivity {

    private ActivityPdfAddBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static String dbUrl = "https://book-app-efa2e-default-rtdb.firebaseio.com/";
    private static String storageUrl = "book-app-efa2e.appspot.com";
    private static final String TAG = "ADD_PDF_TAG";
    private static final int PDF_PICK_CODE = 1000;
    private Uri pdfUri = null;
    private ArrayList<String> categoryTitleArrayList, categoryIdArraylist;
    private String title;
    private String description;
    private String selectedCategoryId, selectedCategoryTitle;
    private String namePdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfAddBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        loadPdfCategories();

        binding.attachBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pdfPickIntent();
            }
        });

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryPickDialog();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate();
            }
        });
    }

    private void validate() {
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();
        selectedCategoryTitle = binding.categoryTv.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "title is null", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(this, "description is null", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(selectedCategoryTitle)) {
            Toast.makeText(this, "Title is null", Toast.LENGTH_LONG).show();
        } else if (pdfUri==null) {
            Toast.makeText(this, "File is null", Toast.LENGTH_LONG).show();
        }else {
            uploadPdfStorage();
        }
    }

    private void uploadPdfStorage() {
        progressDialog.setMessage("Uploading....");
        progressDialog.show();

        long timestamp = System.currentTimeMillis();
        String filePathAndName = "Books/" + timestamp;
        StorageReference storageReference = FirebaseStorage.getInstance().getReference(filePathAndName);
        storageReference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful()) {
                        }
                        String uploadPdfUrl = "" + uriTask.getResult();
                        uploadPdfInfoToDb(uploadPdfUrl, timestamp);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(PdfAddActivity.this, "Upload failed", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void uploadPdfInfoToDb(String uploadPdfUrl, long timestamp) {
        progressDialog.setMessage("Uploading pdf info...");

        String uid = firebaseAuth.getUid();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", "" + uid);
        hashMap.put("id", "" + timestamp);
        hashMap.put("title", "" + title);
        hashMap.put("description", "" + description);
        hashMap.put("categoryId", "" + selectedCategoryId);
        hashMap.put("url", "" + uploadPdfUrl);
        hashMap.put("timestamp", "" + timestamp);
        hashMap.put("viewsCount", 0);
        hashMap.put("downloadsCount", 0);

        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl).getReference("Books");
        ref.child("" + timestamp)
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfAddActivity.this, "Upload info successfully ", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(PdfAddActivity.this, "Upload info failed ", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loadPdfCategories() {
        Log.d(TAG, "loadPdfCategories: loading pdf categories");
        categoryTitleArrayList = new ArrayList<>();
        categoryIdArraylist = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl).getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryTitleArrayList.clear();
                categoryIdArraylist.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String categoryId = "" + ds.child("id").getValue();
                    String categoryTitle = "" + ds.child("category").getValue();

                    categoryTitleArrayList.add(categoryTitle);
                    categoryIdArraylist.add(categoryId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void categoryPickDialog() {
        Log.d(TAG, "categoryPickDialog: showing category");

        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        selectedCategoryTitle = categoryTitleArrayList.get(which);
                        selectedCategoryId = categoryIdArraylist.get(which);

                        binding.categoryTv.setText(selectedCategoryTitle);
                    }
                }).show();
    }

    private void pdfPickIntent() {
        Log.d(TAG, "pdfPickIntent: starting...............");

        Intent intent = new Intent();
        intent.setType("application/pdf");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select PDF"), PDF_PICK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == PDF_PICK_CODE) {
                Log.d(TAG, "onActivityResult: PDF picked");

                pdfUri = data.getData();
                Log.d(TAG, "onActivityResult: URI " + pdfUri);
            }
        } else {
            Log.d(TAG, "onActivityResult: cancel");
            Toast.makeText(this, "Cancel", Toast.LENGTH_LONG).show();
        }
    }


}