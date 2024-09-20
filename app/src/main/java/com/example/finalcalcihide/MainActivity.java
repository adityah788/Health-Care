package com.example.finalcalcihide;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.finalcalcihide.Activity.ImagesHidden;
import com.example.finalcalcihide.Activity.Intruder;
import com.example.finalcalcihide.Utils.FileUtils;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    RelativeLayout relativeLayoutImage,relativeLayoutNote,relativeLayoutIntruder;

    // SharedPreferences constants
    private static final String PREFS_NAME = "IntruderSelfiePrefs";
    private static final String KEY_NEW_SELFIE = "new_selfie_added";
    private static final String KEY_SELFIE_PATH = "selfie_path";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        relativeLayoutImage= findViewById(R.id.main_Images);
        relativeLayoutNote = findViewById(R.id.new_main_file);
        relativeLayoutIntruder = findViewById(R.id.r_intruder);

        // Check if a new selfie has been added
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isNewSelfie = sharedPreferences.getBoolean(KEY_NEW_SELFIE, false);
        String selfiePath = sharedPreferences.getString(KEY_SELFIE_PATH, null);

        if (isNewSelfie && selfiePath != null) {
            showNewImageAlert(selfiePath);

            // Reset the flag
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NEW_SELFIE, false);
            editor.remove(KEY_SELFIE_PATH);
            editor.apply();
        }
        relativeLayoutNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



            }
        });



        relativeLayoutImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(MainActivity.this, ImagesHidden.class));

            }
        });


        relativeLayoutIntruder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Intruder.class));
            }
        });

    }


    @Override
    protected void onResume() {
        super.onResume();

        // Check if a new selfie has been added
        SharedPreferences sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        boolean isNewSelfie = sharedPreferences.getBoolean(KEY_NEW_SELFIE, false);
        String selfiePath = sharedPreferences.getString(KEY_SELFIE_PATH, null);

        if (isNewSelfie && selfiePath != null) {
            showNewImageAlert(selfiePath);

            // Reset the flag
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(KEY_NEW_SELFIE, false);
            editor.remove(KEY_SELFIE_PATH);
            editor.apply();
        }

        Toast.makeText(this, "onResume called", Toast.LENGTH_SHORT).show();
    }



    // Method to show alert when a new image is added
    private void showNewImageAlert(String imagePath) {
        // Get the latest image file
        File latestImageFile = new File(imagePath);

        // Create an ImageView and set the image
        ImageView imageView = new ImageView(this);
        imageView.setImageURI(Uri.fromFile(latestImageFile));
        imageView.setAdjustViewBounds(true);
        imageView.setMaxWidth(800); // Adjust as needed

        new AlertDialog.Builder(this)
                .setTitle("Intruder Selfie Captured!")
                .setMessage("A new selfie has been captured due to multiple failed password attempts.")
                .setView(imageView)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
