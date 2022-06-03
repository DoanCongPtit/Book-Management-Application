package com.example.bookapp.pdf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.example.bookapp.databinding.ActivityPdfEditBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class PdfEditActivity extends AppCompatActivity {
    private ActivityPdfEditBinding binding;
    private String bookId;
    private ProgressDialog progressDialog;
    private ArrayList<String> categoryTitleArrayList, categoryIdArrayList;
    private static String dbUrl = "https://book-app-efa2e-default-rtdb.firebaseio.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPdfEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bookId = getIntent().getStringExtra("bookId");
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);

        loadCategories();
        loadBookInfo();

        binding.categoryTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                categoryDialog();
            }
        });

        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        binding.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateData();
            }
        });
    }

    private String title = "", description = "";

    private void validateData() {
        title = binding.titleEt.getText().toString().trim();
        description = binding.descriptionEt.getText().toString().trim();

        if(TextUtils.isEmpty(title)){
            Toast.makeText(this, "Title err", Toast.LENGTH_LONG).show();
        }else if(TextUtils.isEmpty(description)){
            Toast.makeText(this, "Description err", Toast.LENGTH_LONG).show();
        }else if(TextUtils.isEmpty(selectedCategoryId)){
            Toast.makeText(this, "Category err", Toast.LENGTH_LONG).show();
        }else{
            updatePdf();
        }
    }

    private void updatePdf() {
        progressDialog.setMessage("Updating...");
        progressDialog.show();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("title", ""+title);
        hashMap.put("description", ""+description);
        hashMap.put("categoryId", ""+selectedCategoryId);

        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl).getReference("Books");
        ref.child(bookId)
                .updateChildren(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        progressDialog.dismiss();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    private void loadBookInfo() {
        DatabaseReference refBooks = FirebaseDatabase.getInstance(dbUrl).getReference("Books");
        refBooks.child(bookId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        selectedCategoryId = "" + dataSnapshot.child("categoryId").getValue();
                        String description = "" + dataSnapshot.child("description").getValue();
                        String title = "" + dataSnapshot.child("title").getValue();

                        binding.titleEt.setText(title);
                        binding.descriptionEt.setText(description);

                        DatabaseReference refBookCategory = FirebaseDatabase.getInstance(dbUrl).getReference("Categories");
                        refBookCategory.child(selectedCategoryId)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        String category = "" + dataSnapshot.child("category").getValue();
                                        binding.categoryTv.setText(category);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private String selectedCategoryId = "", getSelectedCategoryTitle = "";

    private void categoryDialog() {
        String[] categoriesArray = new String[categoryTitleArrayList.size()];
        for (int i = 0; i < categoryTitleArrayList.size(); i++) {
            categoriesArray[i] = categoryTitleArrayList.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Category")
                .setItems(categoriesArray, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedCategoryId = categoryIdArrayList.get(i);
                        getSelectedCategoryTitle = categoryTitleArrayList.get(i);

                        binding.categoryTv.setText(getSelectedCategoryTitle);
                    }
                }).show();
    }

    private void loadCategories() {
        categoryIdArrayList = new ArrayList<>();
        categoryTitleArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl).getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                categoryTitleArrayList.clear();
                categoryIdArrayList.clear();

                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String id = "" + ds.child("id").getValue();
                    String category = "" + ds.child("category").getValue();

                    categoryIdArrayList.add(id);
                    categoryTitleArrayList.add(category);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}