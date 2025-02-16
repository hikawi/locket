package dev.frilly.locket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * The activity for handling registration.
 */
public class RegisterActivity extends AppCompatActivity {

    private Button continueButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.layout_outer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        continueButton = findViewById(R.id.button_continue);

        continueButton.setOnClickListener(e -> {
            final var intent = new Intent(this, Register2Activity.class);
            startActivity(intent);
        });
    }

}
