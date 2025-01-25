package com.example.sev;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginPage2 extends AppCompatActivity {

    private EditText usernameInput;
    private EditText vehicleIdInput;
    private EditText newPasswordInput;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page2); // Make sure this matches your XML file name

        // Initialize Firebase Database reference
        databaseReference = FirebaseDatabase.getInstance("https://sev123-82408-default-rtdb.firebaseio.com/").getReference("");

        usernameInput = findViewById(R.id.usernameInput);
        vehicleIdInput = findViewById(R.id.vehicleIdInput);
        newPasswordInput = findViewById(R.id.newPasswordInput);
        Button resetPasswordButton = findViewById(R.id.resetPasswordButton);

        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredUsername = usernameInput.getText().toString();
                String enteredVehicleId = vehicleIdInput.getText().toString();
                String enteredNewPassword = newPasswordInput.getText().toString();

                if (enteredUsername.isEmpty() || enteredVehicleId.isEmpty() || enteredNewPassword.isEmpty()) {
                    Toast.makeText(LoginPage2.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate user information in the Firebase Realtime Database
                databaseReference.orderByChild("username").equalTo(enteredUsername)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                        String vehicleId = userSnapshot.child("vehicleId").getValue(String.class);
                                        if (enteredVehicleId.equals(vehicleId)) {
                                            // Update password in Firebase Realtime Database
                                            userSnapshot.getRef().child("password").setValue(enteredNewPassword);
                                            Toast.makeText(LoginPage2.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                            finish(); // Close the activity after updating password
                                        } else {
                                            Toast.makeText(LoginPage2.this, "Invalid vehicle ID", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(LoginPage2.this, "Invalid username", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toast.makeText(LoginPage2.this, "Database error", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}
