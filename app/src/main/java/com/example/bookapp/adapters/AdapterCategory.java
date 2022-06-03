package com.example.bookapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookapp.pdf.PdfListAdminActivity;
import com.example.bookapp.models.ModelCategory;
import com.example.bookapp.databinding.RowCategoryBinding;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.HolerCategory> {
    private Context context;
    private ArrayList<ModelCategory> categoryArrayList;
    private RowCategoryBinding binding;
    private static String dbUrl = "https://book-app-efa2e-default-rtdb.firebaseio.com/";

    public AdapterCategory(Context context, ArrayList<ModelCategory> categoryArrayList) {
        this.context = context;
        this.categoryArrayList = categoryArrayList;
    }

    @NonNull
    @Override
    public HolerCategory onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding = RowCategoryBinding.inflate(LayoutInflater.from(context), parent, false);
        return new HolerCategory(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolerCategory holder, int position) {
        ModelCategory model = categoryArrayList.get(position);

        String id = model.getId();
        String category = model.getCategory();
        String uid = model.getUid();
        long timestamp = model.getTimestamp();

        holder.categoryTv.setText(category);

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteCategory(model, holder);
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, PdfListAdminActivity.class);
                intent.putExtra("categoryId", id);
                intent.putExtra("categoryTitle", category);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryArrayList.size();
    }

    public class HolerCategory extends RecyclerView.ViewHolder {
        TextView categoryTv;
        ImageButton deleteBtn;

        public HolerCategory(@NonNull View itemView) {
            super(itemView);

            categoryTv = binding.categoryTv;
            deleteBtn = binding.deleteBtn;
        }
    }

    private void deleteCategory(ModelCategory model, HolerCategory holder) {
        String id = model.getId();
        DatabaseReference ref = FirebaseDatabase.getInstance(dbUrl).getReference("Categories");

        ref.child(id)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(context, "Delete Successfully", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context, "Deleted", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
