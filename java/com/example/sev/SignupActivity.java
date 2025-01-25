package com.example.sev;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SignupActivity extends AppCompatActivity {

    private static final String TAG = "SignupActivity";
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance("https://sev123-82408-default-rtdb.firebaseio.com/").getReference("");

        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        EditText vehicleIdInput = findViewById(R.id.vehicleIdInput);
        EditText stateInput = findViewById(R.id.stateInput);
        EditText cityInput = findViewById(R.id.cityInput);
        Button signupButton = findViewById(R.id.signupButton);
        Button viewSubscriptionButton = findViewById(R.id.viewSubscriptionButton);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                String vehicleId = vehicleIdInput.getText().toString();
                String state = stateInput.getText().toString();
                String city = cityInput.getText().toString();

                if (username.isEmpty() || password.isEmpty() || vehicleId.isEmpty() || state.isEmpty() || city.isEmpty()) {
                    Toast.makeText(SignupActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                checkIfUsernameExists(username, password, vehicleId, state, city);
            }
        });

        viewSubscriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to Subscription Activity on clicking "View Subscription" button
                Intent subscriptionIntent = new Intent(SignupActivity.this, SampleSubscriptionActivity.class);
                startActivity(subscriptionIntent);
            }
        });
    }

    private void checkIfUsernameExists(String username, String password, String vehicleId, String state, String city) {
        databaseReference.orderByChild("username").equalTo(username).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Username already exists
                    Toast.makeText(SignupActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                } else {
                    // Username does not exist, proceed with signup
                    createUser(username, password, vehicleId, state, city);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.w(TAG, "checkIfUsernameExists:onCancelled", databaseError.toException());
                Toast.makeText(SignupActivity.this, "Error checking username", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUser(String username, String password, String vehicleId, String state, String city) {
        // Create a unique ID for the user
        String userId = databaseReference.push().getKey();

        // Set 30 days for the free trial
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        calendar.add(Calendar.DAY_OF_YEAR, 30);
        String trialEndDate = dateFormat.format(calendar.getTime());

        User user = new User(username, password, vehicleId, state, city, trialEndDate);

        if (userId != null) {
            // Store user data in Firebase Realtime Database
            databaseReference.child(userId).setValue(user)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User data stored successfully");
                                Toast.makeText(SignupActivity.this, "Signup successful", Toast.LENGTH_SHORT).show();
                                // Redirect to Login Activity after signup
                                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Log.w(TAG, "User data storage failed", task.getException());
                                Toast.makeText(SignupActivity.this, "User data storage failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Log.w(TAG, "Failed to create user ID");
            Toast.makeText(SignupActivity.this, "Signup failed: could not generate user ID", Toast.LENGTH_SHORT).show();
        }
    }

    private static class User {
        public String username;
        public String password;
        public String vehicleId;
        public String state;
        public String city;
        public String trialEndDate;

        public User(String username, String password, String vehicleId, String state, String city, String trialEndDate) {
            this.username = username;
            this.password = password;
            this.vehicleId = vehicleId;
            this.state = state;
            this.city = city;
            this.trialEndDate = trialEndDate;
        }
    }
}
