package com.mohammedaqeel.sarfitadmin;

import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class AdminActivity extends AppCompatActivity {

    private LinearLayout loginBox;
    private LinearLayout userListContainer;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        final EditText etUser = findViewById(R.id.etAdminUser);
        final EditText etPass = findViewById(R.id.etAdminPass);
        tvError = findViewById(R.id.tvAdminError);
        loginBox = findViewById(R.id.loginBox);
        userListContainer = findViewById(R.id.userListContainer);
        TextView tvLogin = findViewById(R.id.tvAdminLogin);

        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = etUser.getText().toString().trim();
                String pass = etPass.getText().toString();
                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(pass)) {
                    tvError.setText("Enter username and password.");
                    return;
                }
                String pseudoEmail = username.toLowerCase().replaceAll("[^a-z0-9._-]", "") + "@sarfit.app";
                tvError.setText("Checking...");

                FirebaseAuth.getInstance().signInWithEmailAndPassword(pseudoEmail, pass)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    loadUsers();
                                } else {
                                    tvError.setText("Login failed.");
                                }
                            }
                        });
            }
        });
    }

    private void loadUsers() {
        FirebaseFirestore.getInstance().collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(Task<QuerySnapshot> task) {
                        if (!task.isSuccessful() || task.getResult() == null) {
                            tvError.setText("Not authorized, or no accounts found.");
                            return;
                        }
                        loginBox.setVisibility(View.GONE);
                        userListContainer.removeAllViews();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String username = doc.contains("username") ? doc.getString("username") : doc.getId();
                            Long streak = doc.getLong("streak");
                            Long total = doc.getLong("total");
                            com.google.firebase.Timestamp created = doc.getTimestamp("createdAt");

                            TextView row = new TextView(AdminActivity.this);
                            StringBuilder sb = new StringBuilder();
                            sb.append(username).append("\n");
                            sb.append("Streak: ").append(streak != null ? streak : 0);
                            sb.append("   Total workouts: ").append(total != null ? total : 0).append("\n");
                            sb.append("Joined: ").append(created != null ? created.toDate().toString() : "unknown (older account)");

                            row.setText(sb.toString());
                            row.setTextColor(Color.parseColor("#EAEAEA"));
                            row.setTextSize(13);
                            row.setPadding(16, 14, 16, 14);
                            row.setBackgroundColor(Color.parseColor("#151518"));
                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                            lp.bottomMargin = 8;
                            row.setLayoutParams(lp);
                            userListContainer.addView(row);
                        }
                    }
                });
    }
}
