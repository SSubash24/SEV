package com.example.sev;


import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

public class SampleSubscriptionActivity extends AppCompatActivity {

    private MaterialTextView weeklyCostTextView;
    private MaterialTextView monthlyCostTextView;
    private MaterialButton activateWeekPlanButton;
    private MaterialButton activateMonthPlanButton;
    private MaterialButton goBackButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_subscription);

        weeklyCostTextView = findViewById(R.id.weeklyCost);
        monthlyCostTextView = findViewById(R.id.monthlyCost);
        activateWeekPlanButton = findViewById(R.id.activateWeekPlanButton);
        activateMonthPlanButton = findViewById(R.id.activateMonthPlanButton);
        goBackButton = findViewById(R.id.goBackButton);

        // Set the initial values for weeklyCostTextView and monthlyCostTextView
        weeklyCostTextView.setText("Rs.10");
        monthlyCostTextView.setText("Rs.30");

        activateWeekPlanButton.setOnClickListener(v -> showSampleSubscriptionToast("weekly"));
        activateMonthPlanButton.setOnClickListener(v -> showSampleSubscriptionToast("monthly"));
        goBackButton.setOnClickListener(v -> finish());
    }

    private void showSampleSubscriptionToast(String planType) {
        String message = "This is a sample subscription page. The " + planType + " plan will be available after signup.";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
