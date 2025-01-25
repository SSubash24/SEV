package com.example.sev;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class AnalyticsActivity extends AppCompatActivity {

    private WebView webView;
    private DatabaseReference databaseReference;
    private String username;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        // Initialize WebView
        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance("https://sev123-82408-default-rtdb.firebaseio.com/").getReference("");

        // Retrieve username from intent
        username = getIntent().getStringExtra("username");

        // Fetch data for the current username
        fetchData(username);
    }

    private void fetchData(final String username) {
        // Query Firebase to fetch user data for the username
        Query query = databaseReference.orderByChild("username").equalTo(username);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Assuming there's only one user matching the username
                    DataSnapshot userSnapshot = dataSnapshot.getChildren().iterator().next();

                    // Extract cost data
                    Long costMon = userSnapshot.child("cost-mon").getValue(Long.class);
                    Long costTue = userSnapshot.child("cost-tues").getValue(Long.class);
                    Long costWed = userSnapshot.child("cost-wed").getValue(Long.class);
                    Long costThu = userSnapshot.child("cost-thurs").getValue(Long.class);
                    Long costFri = userSnapshot.child("cost-fri").getValue(Long.class);
                    Long costSat = userSnapshot.child("cost-sat").getValue(Long.class);
                    Long costSun = userSnapshot.child("cost-sun").getValue(Long.class);

                    // Pass data to display graph
                    displayWeeklyCostGraph(costMon, costTue, costWed, costThu, costFri, costSat, costSun);
                } else {
                    // Handle case where username data does not exist
                    showRetryDialog();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
                webView.loadDataWithBaseURL(null, "<html><body><h1>Error fetching data.</h1></body></html>", "text/html", "UTF-8", null);
            }
        });
    }

    private void displayWeeklyCostGraph(Long costMon, Long costTue, Long costWed, Long costThu, Long costFri, Long costSat, Long costSun) {
        // Prepare data for JavaScript function call
        String data = "{" +
                "\"Mon\":" + costMon + "," +
                "\"Tue\":" + costTue + "," +
                "\"Wed\":" + costWed + "," +
                "\"Thu\":" + costThu + "," +
                "\"Fri\":" + costFri + "," +
                "\"Sat\":" + costSat + "," +
                "\"Sun\":" + costSun +
                "}";

        // Load HTML content with JavaScript for rendering graph
        String htmlContent = "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<title>Weekly Cost Analytics</title>" +
                "<script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>" +
                "<style>" +
                "canvas { width: 100%; height: auto; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<canvas id=\"myChart\"></canvas>" +
                "<script src=\"file:///android_asset/script.js\"></script>" + // Link to your JavaScript file
                "<script>" +
                "function displayGraph(data) {" +
                "const ctx = document.getElementById('myChart').getContext('2d');" +
                "const myChart = new Chart(ctx, {" +
                "type: 'bar'," +
                "data: {" +
                "labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']," +
                "datasets: [{" +
                "label: 'Weekly Cost'," +
                "data: [data.Mon, data.Tue, data.Wed, data.Thu, data.Fri, data.Sat, data.Sun]," +
                "backgroundColor: 'rgba(54, 162, 235, 0.2)'," +
                "borderColor: 'rgba(54, 162, 235, 1)'," +
                "borderWidth: 1" +
                "}]" +
                "}," +
                "options: {" +
                "scales: {" +
                "y: { beginAtZero: true }" +
                "}" +
                "}" +
                "});" +
                "}" +
                "displayGraph(" + data + ");" +
                "</script>" +
                "</body>" +
                "</html>";

        webView.setWebViewClient(new WebViewClient());
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null);
    }

    private void showRetryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Username Not Found");
        builder.setMessage("No data found for username. Enter your username to retry.");

        // Set up the input
        final EditText input = new EditText(this);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        input.setLayoutParams(layoutParams);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newUsername = input.getText().toString();
                fetchData(newUsername); // Retry fetching data with the new username
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel(); // Close the dialog without retrying
            }
        });

        builder.show();
    }
}

