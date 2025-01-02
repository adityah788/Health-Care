package com.example.finalcalcihide.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;

import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.finalcalcihide.Adapter.FinalFileAdap;
import com.example.finalcalcihide.FileUtils.ImgVidFHandle;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.AnimationManager;
import com.example.finalcalcihide.Utils.FileUtils;
import com.example.finalcalcihide.Utils.ToolbarManager;
import com.example.finalcalcihide.ViewPager.ImageandVideoViewPager;

import java.util.ArrayList;
import java.util.List;

import abhishekti7.unicorn.filepicker.UnicornFilePicker;
import abhishekti7.unicorn.filepicker.utils.Constants;

public class FinalFileActivity extends AppCompatActivity {


    private ArrayList<String> filePaths = new ArrayList<>();
    private FinalFileAdap finalFileAdapter;
    private RecyclerView imageRecyclerView;
    private LinearLayout customBottomAppBarDelete;
    private LinearLayout customToolbarContainer;
    private LinearLayout customBottomAppBar;
    private LinearLayout customBottomAppBarVisible;
    private FrameLayout fab_container;
    private AnimationManager animationManager;
    private FrameLayout animationContainer;
    private ToolbarManager toolbarManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_final_file);


        // Set navigation bar color to black
        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.navigation_bar_color));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.FinalPrimaryColor));


        // Initialize animation container
        animationContainer = findViewById(R.id.final_file_animation_container);
        animationManager = new AnimationManager(this, animationContainer);

        // Initialize UI components
        imageRecyclerView = findViewById(R.id.final_file_image_gallery_recycler);
        fab_container = findViewById(R.id.final_file_gallary_fab_container);
        customToolbarContainer = findViewById(R.id.final_file_custom_toolbar_container);
        customBottomAppBar = findViewById(R.id.final_file_custom_bottom_appbar);
        customBottomAppBarVisible = findViewById(R.id.custom_btm_appbar_Visible);
        customBottomAppBarDelete = findViewById(R.id.custom_btm_appbar_delete);
        filePaths = FileUtils.getFilePaths(this);

        // Initialize Adapter
        finalFileAdapter = new FinalFileAdap(this, filePaths, new FinalFileAdap.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                handleItemClick(position);
            }

            @Override
            public void onSelectionChanged(boolean isSelected) {
                onSelectandDeselect_All(isSelected);
            }
        });

        // Initialize ToolbarManager
        toolbarManager = new ToolbarManager(this, customToolbarContainer, finalFileAdapter, filePaths, this);

        imageRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        imageRecyclerView.setAdapter(finalFileAdapter);

        // Initialize Toolbar and Back Press Handling
        toolbarManager.setToolbarMenu(false);
        handleOnBackPressed();


        // Handle Show/Hide Button Click
        customBottomAppBarVisible.setOnClickListener(v -> {
            List<String> selectedPaths = finalFileAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2500; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.HIDE_UNHIDE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        ImgVidFHandle.moveImagesBackToOriginalLocationsWrapper(FinalFileActivity.this, selectedPaths);
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });



        fab_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                UnicornFilePicker.from(FinalFileActivity.this)
                        .addConfigBuilder()
                        .selectMultipleFiles(false)
                        .showOnlyDirectory(true)
                        .setRootDirectory(Environment.getExternalStorageDirectory().getAbsolutePath())
                        .showHiddenFiles(false)
                        .setFilters(new String[]{"pdf", "png", "jpg", "jpeg"})
                        .addItemDivider(true)
                        .build()
                        .forResult(Constants.REQ_UNICORN_FILE);

            }
        });




        // Handle Delete Button Click
        customBottomAppBarDelete.setOnClickListener(v -> {
            List<String> selectedPaths = finalFileAdapter.getSelectedImagePaths();
            final long MINIMUM_DISPLAY_TIME = 2100; // in milliseconds

            animationManager.handleAnimationProcess(
                    AnimationManager.AnimationType.DELETE,
                    selectedPaths,
                    MINIMUM_DISPLAY_TIME,
                    () -> {
                        ImgVidFHandle.moveImagesBackToRecycleLocationsWrapper(FinalFileActivity.this, selectedPaths);
                    },
                    (processSuccess, paths) -> stopAnimationAndUpdateUI(processSuccess, paths)
            );
        });


    }



    private void handleItemClick(int position) {
        if (finalFileAdapter.isSelectedAny()) {
            finalFileAdapter.toggleSelection(position);
        } else {
            Intent intent = new Intent(this, ImageandVideoViewPager.class);
            intent.putStringArrayListExtra("imagePaths", filePaths);
            intent.putExtra("position", position);
            startActivity(intent);
        }
    }

    private void onSelectandDeselect_All(boolean isAnySelected) {
        toolbarManager.setToolbarMenu(isAnySelected);
        setCustomBottomAppBarVisibility(isAnySelected);
    }

    private void setCustomBottomAppBarVisibility(boolean visible) {
        if (visible && customBottomAppBar.getVisibility() != View.VISIBLE) {
            fab_container.setVisibility(View.GONE);
            customBottomAppBar.setTranslationY(customBottomAppBar.getHeight());
            customBottomAppBar.setVisibility(View.VISIBLE);
            customBottomAppBar.animate().translationY(0).setDuration(300).setListener(null);
        } else if (!visible && customBottomAppBar.getVisibility() == View.VISIBLE) {
            customBottomAppBar.animate()
                    .translationY(customBottomAppBar.getHeight())
                    .setDuration(300)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            customBottomAppBar.setVisibility(View.GONE);
                        }
                    });
            fab_container.setVisibility(View.VISIBLE);
        }
    }

    private void handleOnBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (finalFileAdapter.isSelectedAny()) {
                    finalFileAdapter.clearSelection();
                    onSelectandDeselect_All(false);
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void stopAnimationAndUpdateUI(boolean processSuccess, List<String> selectedPaths) {
        if (processSuccess) {
            filePaths.removeAll(selectedPaths);
            finalFileAdapter.notifyDataSetChanged();
            finalFileAdapter.clearSelection();
            Toast.makeText(FinalFileActivity.this, "Images moved back to original locations and deleted from app", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(FinalFileActivity.this, "Error moving images back", Toast.LENGTH_SHORT).show();
        }
    }

}