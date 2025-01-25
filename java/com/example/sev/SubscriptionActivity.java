package com.example.sev;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SubscriptionActivity extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private MaterialTextView daysLeftTextView;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        // Retrieve the username from the intent
        username = getIntent().getStringExtra("username");

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance("https://sev123-82408-default-rtdb.firebaseio.com/").getReference("");

        // Find views by their IDs
        MaterialButton activateWeekPlanButton = findViewById(R.id.activateWeekPlanButton);
        MaterialButton activateMonthPlanButton = findViewById(R.id.activateMonthPlanButton);
        MaterialButton goBackButton = findViewById(R.id.goBackButton);
        daysLeftTextView = findViewById(R.id.daysLeft);

        // Fetch days left for free trial if the username is available
        if (username != null) {
            fetchDaysLeftForFreeTrial(username);
        }

        // Set onClickListeners for the buttons
        activateWeekPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activatePlan("weekly");
            }
        });

        activateMonthPlanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activatePlan("monthly");
            }
        });

        goBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void fetchDaysLeftForFreeTrial(String username) {
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String trialEndDate = userSnapshot.child("trialEndDate").getValue(String.class);
                        if (trialEndDate != null) {
                            calculateAndDisplayDaysLeft(trialEndDate);
                        } else {
                            Toast.makeText(SubscriptionActivity.this, "Trial end date not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(SubscriptionActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SubscriptionActivity.this, "Error fetching data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calculateAndDisplayDaysLeft(String trialEndDate) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date endDate = dateFormat.parse(trialEndDate);
            Date currentDate = new Date();

            long diff = endDate.getTime() - currentDate.getTime();
            long daysLeft = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

            if (daysLeft > 0) {
                // Update the TextView with the number of days left
                daysLeftTextView.setText(daysLeft + " days left for your free trial");
            } else {
                // Free trial expired
                daysLeftTextView.setText("0 days left for your free trial");
                Toast.makeText(this, "Add subscription", Toast.LENGTH_SHORT).show();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error parsing date", Toast.LENGTH_SHORT).show();
        }
    }

    private void activatePlan(String planType) {
        // You can add your subscription activation logic here
        // For demonstration purposes, this example displays a toast message
        if ("weekly".equals(planType)) {
            Toast.makeText(this, "Weekly Plan Activated. Enjoy your subscription!", Toast.LENGTH_SHORT).show();
        } else if ("monthly".equals(planType)) {
            Toast.makeText(this, "Monthly Plan Activated. Enjoy your subscription!", Toast.LENGTH_SHORT).show();
        }
    }
}
