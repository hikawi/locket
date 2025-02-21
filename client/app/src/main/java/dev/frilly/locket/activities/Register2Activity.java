package dev.frilly.locket.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Pattern;

import dev.frilly.locket.Authentication;
import dev.frilly.locket.Constants;
import dev.frilly.locket.R;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Second registration screen.
 */
public class Register2Activity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final Pattern DATE_PATTERN = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");
    private final Calendar calendar = Calendar.getInstance();

    private TextInputLayout dobLayout;
    private EditText dobField;
    private TextView errorText;

    private Button continueButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register2_screen);

        dobLayout = findViewById(R.id.layout_field_dob);
        dobField = findViewById(R.id.field_dob);
        errorText = findViewById(R.id.text_error);
        continueButton = findViewById(R.id.button_continue);

        dobLayout.setEndIconOnClickListener(this::onEndIconClick);
        dobField.addTextChangedListener(new DateTextWatcher());
        continueButton.setOnClickListener(this::onContinue);
    }

    private void onContinue(View view) {
        final var matcher = DATE_PATTERN.matcher(dobField.getText());
        if (!matcher.matches()) {
            errorText.setText(R.string.error_birthdate_invalid);
            dobField.requestFocus();
            return;
        }

        try {
            final var body = new JSONObject();
            body.put("birthdate", dobField.getText());

            final var req = new Request.Builder()
                    .url(Constants.BACKEND_URL + "profiles")
                    .header("Authorization", "Bearer " + Authentication.getToken(this))
                    .put(RequestBody.create(body.toString(), Constants.JSON))
                    .build();

            Constants.HTTP_CLIENT.newCall(req).enqueue(new PutProfilesCallback());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onEndIconClick(View view) {
        final var dialog = new DatePickerDialog(this, this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int y, int m, int d) {
        calendar.set(Calendar.YEAR, y);
        calendar.set(Calendar.MONTH, m);
        calendar.set(Calendar.DAY_OF_MONTH, d);

        final var formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        dobField.setText(formatter.format(calendar.getTime()));
    }

    private class DateTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            try {
                final var date = LocalDate.parse(editable);
                calendar.set(Calendar.YEAR, date.getYear());
                calendar.set(Calendar.MONTH, date.getMonthValue());
                calendar.set(Calendar.DAY_OF_MONTH, date.getDayOfMonth());
            } catch (DateTimeParseException exception) {
                // Intentionally left blank.
            }
        }

    }

    private class PutProfilesCallback implements Callback {

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> errorText.setText(R.string.error_unknown));
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            Log.d("dev.frilly.locket", String.valueOf(response.code()));
            Log.d("dev.frilly.locket", response.body().string());

            runOnUiThread(() -> {
                if (response.code() != 200) {
                    errorText.setText(R.string.error_unknown);
                    return;
                }

                final var intent = new Intent(Register2Activity.this, WelcomeActivity.class);
                startActivity(intent);
            });
        }

    }

}