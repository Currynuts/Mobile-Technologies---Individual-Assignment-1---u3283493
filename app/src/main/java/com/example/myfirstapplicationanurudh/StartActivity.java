package com.example.myfirstapplicationanurudh;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_start);

        // Fixed: Use the root view of activity_start.xml instead of a non-existent ID
        View root = findViewById(android.R.id.content);
        if (root != null) {
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                return insets;
            });
        }

        // --- First Button: UI Event ---
        Button btnUI = findViewById(R.id.buttonUIEvent);
        btnUI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MainActivity.class);
                intent.putExtra("message", "Mobile Tech");
                startActivity(intent);
            }
        });

        // --- Second Button: Location Services ---
        Button btnLocation = findViewById(R.id.LocationServices);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, LocationServicesActivity.class);
                startActivity(intent);
            }
        });

        // --- Third Button: Firebase ML Kit ---
        Button btnFirebase = findViewById(R.id.FirebaseMLKit);
        btnFirebase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartActivity.this, MLKitActivity.class);
                startActivity(intent);
            }
        });
    }
}
