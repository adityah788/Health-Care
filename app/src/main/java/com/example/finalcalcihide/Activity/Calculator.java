package com.example.finalcalcihide.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.finalcalcihide.MainActivity;
import com.example.finalcalcihide.R;
import com.example.finalcalcihide.Utils.IntruderUtils;
import com.example.finalcalcihide.databinding.ActivityCalculatorBinding;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Calculator extends AppCompatActivity {
    private ActivityCalculatorBinding binding;
    private boolean lastNumeric = false;
    private boolean stateError = false;
    private boolean lastDot = false;
    private boolean equalclicked = false;
    private Expression expression;
    private static boolean isInstanceActive = false;

    private int wrongPasswordCount = 0; // Counter for wrong password attempts
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;

    private SharedPreferences sharedPreferences,sharedPreferencesintru;
    private boolean isPasswordSet;
    private boolean isTakeSelfieEnabled;

    private StringBuilder inputPassword = new StringBuilder();

    private EditText passtxt1, passtxt2, passtxt3, passtxt4;
    private TextView tvPassDetail;

    private Boolean isInitialPassdone=false;
    private Boolean flagInidone=false;
    private String firstPassword;
    boolean resetPassword;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment if using EdgeToEdge
        binding = ActivityCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.calculator_nav_bar_color));

        passtxt1 = findViewById(R.id.Passtxt1);
        passtxt2 = findViewById(R.id.Passtxt2);
        passtxt3 = findViewById(R.id.Passtxt3);
        passtxt4 = findViewById(R.id.Passtxt4);
        tvPassDetail = findViewById(R.id.tvPassDetail);


        isInstanceActive = true;


        // Shared Preferences to store the password
        sharedPreferences = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE);
        sharedPreferencesintru = getSharedPreferences("MyIntruder",MODE_PRIVATE);
        isPasswordSet = sharedPreferences.contains("password");
        isTakeSelfieEnabled = sharedPreferencesintru.getBoolean("take_selfie", false);


        // Retrieve the state of isPasswordSet from SharedPreferences
        isPasswordSet = sharedPreferences.getBoolean("isPasswordSet", false);

        // Check for incoming intent to reset the password
        intent = getIntent();
        resetPassword = intent.getBooleanExtra("RESET_PASSWORD", false); // Retrieve the boolean


        if (!isPasswordSet || resetPassword) {
            // No password set, prompt user to create one
            tvPassDetail.setText("Enter a 4 digit password and press =");

            Log.d("Reset Password", "!isPasswordSet || resetPassword clicked");

            Toast.makeText(this, "!isPasswordSet || resetPassword clicked", Toast.LENGTH_SHORT).show();

            binding.calculatorLinearNewPassword.setVisibility(View.VISIBLE);
            binding.calculatorRelativeCalculationTxt.setVisibility(View.GONE);

        } else {
            // Password already set, prompt to enter password
//            tvPassDetail.setText("Enter your 4 digit password and press =");

            binding.calculatorLinearNewPassword.setVisibility(View.GONE);
            binding.calculatorRelativeCalculationTxt.setVisibility(View.VISIBLE);

        }


        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (resetPassword) {
                    // Just go back without showing the alert
                    finish(); // or call super.onBackPressed();
                } else {
                    // Show the alert dialog
                    new AlertDialog.Builder(Calculator.this)
                            .setTitle("Exit App")
                            .setMessage("Are you sure you want to exit the app?")
                            .setPositiveButton("Yes", (dialog, which) -> finishAffinity())
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });

        // Check and request camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, set up the camera
            IntruderUtils.setupCamera(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                IntruderUtils.setupCamera(this);
            } else {
                // Permission denied
                Toast.makeText(this, "Camera permission is required to take selfies.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onAllClearClick(View view) {
        Log.d("Calculator", "onAllClearClick called. isPasswordSet: " + isPasswordSet);

        if (!isPasswordSet || resetPassword) {
            // Clear all inputPassword when the password is not set
            inputPassword.setLength(0); // Clear the StringBuilder
            Log.d("Calculator", "Input password cleared.");

            // Update the password fields to reflect the cleared password
            updatePasswordFields(inputPassword.toString());

            Toast.makeText(this, "All password inputs cleared. Set a password to use the calculator.", Toast.LENGTH_SHORT).show();
        } else {
            // Clear calculator inputs
            binding.dataTv.setText("");
            binding.resultTv.setText("");
            stateError = false;
            lastNumeric = false;
            lastDot = false;
            binding.resultTv.setVisibility(View.GONE);
            Log.d("Calculator", "Calculator inputs cleared.");
        }
    }


//    public void onDigitClick(View view) {
//        if (stateError) {
//            binding.dataTv.setText(((Button) view).getText());
//            stateError = false;
//        } else if (equalclicked) {
//            onAllClearClick(view);
//            binding.dataTv.append(((Button) view).getText());
//            equalclicked = false;
//        } else {
//            binding.dataTv.append(((Button) view).getText());
//        }
//        lastNumeric = true;
//        onEqual();
//    }


    // Method to handle digit button clicks
    public void onDigitClick(View view) {
        if (!isPasswordSet || resetPassword) {
            // If password is not set, treat the input as password input
            if (inputPassword.length() < 4) {
                // Append digit to inputPassword
                inputPassword.append(((TextView) view).getText().toString());
                updatePasswordFields(inputPassword.toString());
            }
        } else {
            // If password is set, treat as regular calculator operation
            if (stateError) {
                binding.dataTv.setText(((Button) view).getText());
                stateError = false;
            } else if (equalclicked) {
                onAllClearClick(view);
                binding.dataTv.append(((Button) view).getText());
                equalclicked = false;
            } else {
                binding.dataTv.append(((Button) view).getText());
            }
            lastNumeric = true;
            onEqual();
        }
    }


//    public void onEqualClick(View view) {
//        onEqual();
//
//        String enteredPassword = binding.dataTv.getText().toString();
//
//        if (enteredPassword.equals("11223344")) {
//            Toast.makeText(this, "Password 11223344 is clicked", Toast.LENGTH_SHORT).show();
//            wrongPasswordCount = 0; // Reset counter
//        } else if (enteredPassword.length() == 4 && !enteredPassword.equals("6666")) {
//            Toast.makeText(this, "You MF Fraudster", Toast.LENGTH_SHORT).show();
//            wrongPasswordCount++;
//
//            if (wrongPasswordCount > 2) {
//                // More than 2 wrong attempts, trigger selfie capture
//                IntruderUtils.takeSelfie(this);
//                Toast.makeText(this, "Selfie capture triggered due to multiple failed attempts", Toast.LENGTH_SHORT).show();
//            }
//        } else if (enteredPassword.length() == 4 && enteredPassword.equals("6666")) {
//            // Correct password logic
//            if (isTaskRoot()) {
//                startActivity(new Intent(Calculator.this, MainActivity.class));
//                finish();
//            } else {
//                finish();
//            }
//            wrongPasswordCount = 0; // Reset counter
//        } else if (binding.resultTv.getText().length() > 1) {
//            binding.dataTv.setText(binding.resultTv.getText().toString().substring(1));
//        }
//
//        equalclicked = true;
//        binding.resultTv.setText("");
//    }


    public void onEqualClick(View view) {


        // If the password is not set yet
        if (!isPasswordSet || resetPassword) {

            // Shared Preferences instance
            SharedPreferences sharedPreferences = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();


            // Combine the four EditText fields into a single password string
            String enteredPassword = passtxt1.getText().toString() +
                    passtxt2.getText().toString() +
                    passtxt3.getText().toString() +
                    passtxt4.getText().toString();


            if (enteredPassword.length() == 4) {
                if (!flagInidone) {
                    firstPassword = enteredPassword;
                    flagInidone = true;
                    enteredPassword="";
                    inputPassword.setLength(0);
                    updatePasswordFields(inputPassword.toString());

                }
                if (isInitialPassdone) {

                    if (enteredPassword.equals(firstPassword)) {

                        // Save the entered password in SharedPreferences
                        editor.putString("password", enteredPassword);
                        editor.putBoolean("isPasswordSet", true); // Mark password as set
                        editor.apply(); // Save changes

                        isPasswordSet = true; // Update the local flag
                        Toast.makeText(this, "Password set successfully", Toast.LENGTH_SHORT).show();
                        wrongPasswordCount = 0; // Reset counter


                        // show original calci
                        binding.calculatorLinearNewPassword.setVisibility(View.GONE);
                        binding.calculatorRelativeCalculationTxt.setVisibility(View.VISIBLE);

                        startActivity(new Intent(Calculator.this,SecutityQues.class));
                        finish();

                    }
                    else {
                        Toast.makeText(this, "Enter the correct Password", Toast.LENGTH_LONG).show();
                        Log.d("Hamar calcu","Enter the correct Password is shown");

                    }
                }

                isInitialPassdone=true;
                binding.tvPassDetail.setText("Confirm Your 4 digit Password ");
                Toast.makeText(Calculator.this, "Confirm the password", Toast.LENGTH_SHORT).show();
//                inputPassword.setLength(0); // Clear the StringBuilder


            } else {
                Toast.makeText(this, "Please enter a valid 4-digit password", Toast.LENGTH_SHORT).show();
            }
        }
        // If the password is already set

        else {

            onEqual();

            // Retrieve the saved password from SharedPreferences
            String savedPassword = sharedPreferences.getString("password", "");
            String enteredPassword = binding.dataTv.getText().toString();


            if (enteredPassword.equals(savedPassword)) {
                Toast.makeText(this, "Password is correct", Toast.LENGTH_SHORT).show();
                wrongPasswordCount = 0; // Reset counter

                // Proceed with correct password logic
                if (isTaskRoot()) {
                    startActivity(new Intent(Calculator.this, MainActivity.class));
                    finish();
                } else {
                    finish();
                }
            } else if (enteredPassword.length() == 4 && !enteredPassword.equals(savedPassword) && isTakeSelfieEnabled ) {
                // Handle incorrect password
                Toast.makeText(this, "You MF Fraudster", Toast.LENGTH_SHORT).show();
                wrongPasswordCount++;

                if (wrongPasswordCount > 2) {
                    // Trigger selfie capture after 3 wrong attempts
                    IntruderUtils.takeSelfie(this);
                    Toast.makeText(this, "Selfie capture triggered due to multiple failed attempts", Toast.LENGTH_SHORT).show();
                }
            } else if (binding.resultTv.getText().length() > 1) {
                binding.dataTv.setText(binding.resultTv.getText().toString().substring(1));
            }

            equalclicked = true;
            binding.resultTv.setText("");
        }

//        // Reset the EditText fields after the equal button is pressed
//        passtxt1.setText("");
//        passtxt2.setText("");
//        passtxt3.setText("");
//        passtxt4.setText("");

        // Set equalclicked to true after pressing the equal button
//        equalclicked = true;
    }


//    public void onOperatorClick(View view) {
//        if (lastNumeric && !stateError) {
//            binding.dataTv.append(((Button) view).getText());
//            lastDot = false;
//            lastNumeric = false;
//            onEqual();
//        }
//    }


    public void onOperatorClick(View view) {
        // Retrieve the state of isPasswordSet from SharedPreferences
//        SharedPreferences sharedPreferences = getSharedPreferences("CalculatorPrefs", MODE_PRIVATE);
//        boolean isPasswordSet = sharedPreferences.getBoolean("isPasswordSet", false);

        // If the password is not set, disable the functionality
        if (!isPasswordSet || resetPassword) {
            Toast.makeText(this, "Set the password first to use the calculator", Toast.LENGTH_SHORT).show();
            return; // Exit the method, disabling the operation
        }

        // Proceed with normal functionality if the password is set
        if (lastNumeric && !stateError) {
            binding.dataTv.append(((Button) view).getText());
            lastDot = false;
            lastNumeric = false;
            onEqual();
        }
    }


    public void onClearClick(View view) {

        if (!isPasswordSet || resetPassword) {
            // Clear all inputPassword when the password is not set
            inputPassword.setLength(0); // Clear the StringBuilder

            // Update the password fields to reflect the cleared password
            updatePasswordFields(inputPassword.toString());

            Toast.makeText(this, "All password inputs cleared. Set a password to use the calculator.", Toast.LENGTH_SHORT).show();
        } else {

            binding.dataTv.setText("");
            binding.resultTv.setText("");
            lastNumeric = false;
        }
    }

//    public void onBackClick(View view) {
//        if (binding.dataTv.getText().length() > 0) {
//            binding.dataTv.setText(binding.dataTv.getText().toString().substring(0, binding.dataTv.getText().toString().length() - 1));
//            try {
//                if (binding.dataTv.getText().length() > 0) {
//                    char lastChar = binding.dataTv.getText().toString().charAt(binding.dataTv.getText().toString().length() - 1);
//                    if (Character.isDigit(lastChar)) {
//                        onEqual();
//                    }
//                } else {
//                    binding.resultTv.setText("");
//                    binding.resultTv.setVisibility(View.GONE);
//                }
//            } catch (Exception e) {
//                binding.resultTv.setText("");
//                binding.resultTv.setVisibility(View.GONE);
//                Log.e("last char Error", e.toString());
//            }
//        }
//    }


    public void onBackClick(View view) {
        // If the password is not set, remove the last digit from password fields
        if (!isPasswordSet || resetPassword) {
            if (inputPassword.length() > 0) {
                // Remove the last digit from the inputPassword
                inputPassword.deleteCharAt(inputPassword.length() - 1);
                updatePasswordFields(inputPassword.toString()); // Update the password fields
            }
            Toast.makeText(this, "Backspace is working... if passwrd is not set", Toast.LENGTH_SHORT).show();
        } else {
            if (binding.dataTv.getText().length() > 0) {
                binding.dataTv.setText(binding.dataTv.getText().toString().substring(0, binding.dataTv.getText().toString().length() - 1));
                try {
                    if (binding.dataTv.getText().length() > 0) {
                        char lastChar = binding.dataTv.getText().toString().charAt(binding.dataTv.getText().toString().length() - 1);
                        if (Character.isDigit(lastChar)) {
                            onEqual();
                        }
                    } else {
                        binding.resultTv.setText("");
                        binding.resultTv.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    binding.resultTv.setText("");
                    binding.resultTv.setVisibility(View.GONE);
                    Log.e("last char Error", e.toString());
                }
            }
        }

    }


    @SuppressLint("SetTextI18n")
    private void onEqual() {
        if (lastNumeric && !stateError) {
            String txt = binding.dataTv.getText().toString();
            expression = new ExpressionBuilder(txt).build();
            try {
                double result = expression.evaluate();
                binding.resultTv.setVisibility(View.VISIBLE);
                binding.resultTv.setText("=" + result);
            } catch (ArithmeticException ex) {
                Log.e("Evaluate Error", ex.toString());
                binding.resultTv.setText("Errorrrr");
                stateError = true;
                lastNumeric = false;
            }
        }
    }

    public void onPointClick(View view) {

        // If the password is not set, disable the functionality
        if (!isPasswordSet || resetPassword) {
            Toast.makeText(this, "Set the password first to use the calculator", Toast.LENGTH_SHORT).show();
            return; // Exit the method, disabling the operation
        }


        if (!stateError && !lastDot) {
            binding.dataTv.append(((Button) view).getText());
            lastDot = true;
            lastNumeric = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Set the flag to false when this activity is destroyed
        isInstanceActive = false;
    }


    // If calci rerelated error of calci not opening when background  then
    // remove this onResume and onPause
    @Override
    protected void onResume() {
        super.onResume();
        isInstanceActive = true;
    }
    // If calci rerelated error of calci not opening when background  then
    // remove this onResume and onPause
    @Override
    protected void onPause() {
        super.onPause();
        isInstanceActive = false;
    }




    public static boolean isActivityActive() {
        return isInstanceActive;
    }


    // Update password fields with the entered digits
    private void updatePasswordFields(String password) {
        passtxt1.setText(password.length() > 0 ? String.valueOf(password.charAt(0)) : "");
        passtxt2.setText(password.length() > 1 ? String.valueOf(password.charAt(1)) : "");
        passtxt3.setText(password.length() > 2 ? String.valueOf(password.charAt(2)) : "");
        passtxt4.setText(password.length() > 3 ? String.valueOf(password.charAt(3)) : "");

//        if (password.length() == 4) {
//            binding.tvPassDetail.setText("Press = to save or verify your password.");
//        }
    }

}
