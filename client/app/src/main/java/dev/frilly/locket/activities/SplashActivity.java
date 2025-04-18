package dev.frilly.locket.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import dev.frilly.locket.MainActivity;
import dev.frilly.locket.R;
import dev.frilly.locket.activities.ChatActivity;
import dev.frilly.locket.activities.WelcomeActivity;
import dev.frilly.locket.model.User;
import dev.frilly.locket.utils.AndroidUtil;
import dev.frilly.locket.utils.FirebaseUtil;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(getIntent().getExtras()!=null){
            //from notification
            String userId = getIntent().getExtras().getString("userId");
            FirebaseUtil.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            User model = task.getResult().toObject(User.class);

                            Intent mainIntent = new Intent(this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(mainIntent);

                            Intent intent = new Intent(this, ChatActivity.class);
                            AndroidUtil.passUserModelAsIntent(intent, model);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    });


        }else{
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));
                    finish();
                }
            },1000);
        }
    }
}