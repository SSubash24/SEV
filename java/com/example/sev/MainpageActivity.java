package com.example.sev;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainpageActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_CREATE_FILE = 1;
    private TextInputEditText vehicleIdInput;
    private MaterialTextView chargingCyclesTextView, totalConsumptionTextView, totalCostTextView;
    private DatabaseReference databaseReference;
    private String username;
    private int totalConsumption = 0;
    private int totalCost = 0;
    private String fileName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainpage);

        // Retrieve the username from the intent
        username = getIntent().getStringExtra("username");

        // Initialize views
        vehicleIdInput = findViewById(R.id.vehicle_id_input);
        chargingCyclesTextView = findViewById(R.id.charging_cycles_text_view);
        totalConsumptionTextView = findViewById(R.id.total_consumption_text_view);
        totalCostTextView = findViewById(R.id.total_cost_text_view);

        MaterialButton fetchDataButton = findViewById(R.id.fetch_data_button);
        MaterialButton subscriptionButton = findViewById(R.id.subscription_button);
        MaterialButton weekConsumptionButton = findViewById(R.id.week_charging_cycles_button);
        MaterialButton monthConsumptionButton = findViewById(R.id.month_charging_cycles_button);
        MaterialButton weekCostButton = findViewById(R.id.week_total_cost_button);
        MaterialButton monthCostButton = findViewById(R.id.month_total_cost_button);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance("https://sev123-82408-default-rtdb.firebaseio.com/").getReference("");

        fetchDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredVehicleId = vehicleIdInput.getText().toString().trim();
                if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(enteredVehicleId)) {
                    checkSubscriptionStatus(username, enteredVehicleId);
                } else {
                    Toast.makeText(MainpageActivity.this, "Please enter both username and vehicle ID", Toast.LENGTH_SHORT).show();
                }
            }
        });

        subscriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to SubscriptionActivity
                Intent intent = new Intent(MainpageActivity.this, SubscriptionActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        weekConsumptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to AnalyticsActivity for weekly consumption data
                Intent intent = new Intent(MainpageActivity.this, Analytics2Activity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        monthConsumptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate and download the report for monthly consumption data
                fileName = "consumption_data.csv";
                createCsvFile();
            }
        });

        weekCostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to Analytics2Activity for weekly cost data
                Intent intent = new Intent(MainpageActivity.this, AnalyticsActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        monthCostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Generate and download the report for monthly cost data
                fileName = "cost_data.csv";
                createCsvFile();
            }
        });
    }

    private void checkSubscriptionStatus(String username, String vehicleId) {
        Query query = databaseReference.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        Boolean hasSubscription = userSnapshot.child("hasSubscription").getValue(Boolean.class);
                        String trialEndDate = userSnapshot.child("trialEndDate").getValue(String.class);

                        if (hasSubscription != null && hasSubscription) {
                            fetchChargingCyclesData(username, vehicleId);
                        } else if (isTrialActive(trialEndDate)) {
                            fetchChargingCyclesData(username, vehicleId);
                        } else {
                            showSubscriptionAlert();
                        }
                        return;
                    }
                } else {
                    Toast.makeText(MainpageActivity.this, "Username not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainpageActivity", "Error fetching data", databaseError.toException());
                Toast.makeText(MainpageActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isTrialActive(String trialEndDate) {
        if (trialEndDate == null) {
            return false;
        }
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date endDate = dateFormat.parse(trialEndDate);
            Date currentDate = new Date();
            return endDate != null && currentDate.before(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void showSubscriptionAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Add Subscription")
                .setMessage("You need an active subscription to view your data. Please add a subscription or exit.")
                .setPositiveButton("Add Subscription", (dialog, which) -> {
                    // Redirect to SubscriptionActivity
                    Intent intent = new Intent(MainpageActivity.this, SubscriptionActivity.class);
                    intent.putExtra("username", username);
                    startActivity(intent);
                })
                .setNegativeButton("Exit", (dialog, which) -> {
                    // Redirect to LoginActivity
                    Intent intent = new Intent(MainpageActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void fetchChargingCyclesData(String enteredUsername, String enteredVehicleId) {
        Query query = databaseReference.orderByChild("username").equalTo(enteredUsername);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String vehicleId = userSnapshot.child("vehicleId").getValue(String.class);
                        if (enteredVehicleId.equals(vehicleId)) {
                            Integer chargingCycles = userSnapshot.child("chargingCyclesWeek").getValue(Integer.class);
                            if (chargingCycles != null) {
                                chargingCyclesTextView.setText("Charging Cycles: " + chargingCycles);
                            } else {
                                chargingCyclesTextView.setText("Charging Cycles: N/A");
                            }

                            // Fetch total consumption and cost
                            fetchConsumptionAndCostData(userSnapshot);
                            return;
                        }
                    }
                    chargingCyclesTextView.setText("Vehicle ID not found for the entered username.");
                } else {
                    chargingCyclesTextView.setText("Username not found.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("MainpageActivity", "Error fetching data", databaseError.toException());
                Toast.makeText(MainpageActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchConsumptionAndCostData(DataSnapshot userSnapshot) {
        for (DataSnapshot dataSnapshot : userSnapshot.getChildren()) {
            String key = dataSnapshot.getKey();
            if (key != null && key.startsWith("con-")) {
                Integer value = dataSnapshot.getValue(Integer.class);
                if (value != null) {
                    totalConsumption += value;
                }
            } else if (key != null && key.startsWith("cost-")) {
                Integer value = dataSnapshot.getValue(Integer.class);
                if (value != null) {
                    totalCost += value;
                }
            }
        }

        totalConsumptionTextView.setText("Total Consumption: " + totalConsumption);
        totalCostTextView.setText("Total Cost: " + totalCost);
    }

    private void createCsvFile() {
        // Use the Storage Access Framework to create a new file
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, REQUEST_CODE_CREATE_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CREATE_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    generateAndSaveReport(uri);
                } else {
                    Toast.makeText(this, "Error creating file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void generateAndSaveReport(Uri uri) {
        try {
            String reportContent = generateReportContent();
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                writer.write(reportContent);
                writer.flush();
                writer.close();
                outputStream.close();
                Toast.makeText(this, "Report generated successfully", Toast.LENGTH_SHORT).show();

                // Open a file chooser dialog to allow the user to pick an app to view the file
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, "text/csv");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(intent, "Open File Using"));
            } else {
                Toast.makeText(this, "Error creating file output stream", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error saving report", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateReportContent() {
        // Modify this method to generate the CSV content based on your data
        StringBuilder csvContent = new StringBuilder();
        // Example content generation
        csvContent.append("Date,Total Consumption,Total Cost\n");
        csvContent.append("2024-06-01," + totalConsumption + "," + totalCost + "\n");
        return csvContent.toString();
    }
}
