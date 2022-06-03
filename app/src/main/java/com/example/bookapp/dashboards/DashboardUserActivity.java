package com.example.bookapp.dashboards;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.bookapp.BookUserFragment;
import com.example.bookapp.MainActivity;
import com.example.bookapp.MyApplication;
import com.example.bookapp.databinding.ActivityDashboardAdminBinding;
import com.example.bookapp.databinding.ActivityDashboardUserBinding;
import com.example.bookapp.models.ModelCategory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DashboardUserActivity extends AppCompatActivity {
    private ActivityDashboardUserBinding binding;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private static String dbUrl = "https://book-app-efa2e-default-rtdb.firebaseio.com/";
    public ArrayList<ModelCategory> categoryArrayList;
    public ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        checkUser();
        setupViewPagerAdapter(binding.viewPager);

        binding.tabLayout.setupWithViewPager(binding.viewPager);

        //logout
        binding.logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseAuth.signOut();
                checkUser();
            }
        });
    }

    private void checkUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser == null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            String email = firebaseUser.getEmail();
            binding.subTitleTv.setText(email);
        }
    }

    private void setupViewPagerAdapter(ViewPager viewPager) {
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);
        categoryArrayList = new ArrayList<>();

        DatabaseReference ref = FirebaseDatabase.getInstance(MyApplication.dbUrl).getReference("Categories");
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryArrayList.clear();

                ModelCategory modelAll = new ModelCategory("01", "All", "", 1);
                ModelCategory modelMostViewed = new ModelCategory("02", "Most Viewed", "", 1);

                categoryArrayList.add(modelAll);
                categoryArrayList.add(modelMostViewed);

                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        "" + modelAll.getId(),
                        "" + modelAll.getCategory(),
                        "" + modelAll.getUid()
                ), modelAll.getCategory());

                viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                        "" + modelMostViewed.getId(),
                        "" + modelMostViewed.getCategory(),
                        "" + modelMostViewed.getUid()
                ), modelMostViewed.getCategory());

                viewPagerAdapter.notifyDataSetChanged();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelCategory model = ds.getValue(ModelCategory.class);
                    categoryArrayList.add(model);
                    viewPagerAdapter.addFragment(BookUserFragment.newInstance(
                            "" + model.getId(),
                            "" + model.getCategory(),
                            "" + model.getUid()
                    ), model.getCategory());
                    viewPagerAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        viewPager.setAdapter(viewPagerAdapter);
    }

    public class ViewPagerAdapter extends FragmentPagerAdapter {
        private ArrayList<BookUserFragment> fragmentList = new ArrayList<>();
        private ArrayList<String> fragmentTitleList = new ArrayList<>();
        private Context context;

        public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
            super(fm, behavior);
            this.context = context;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        private void addFragment(BookUserFragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}