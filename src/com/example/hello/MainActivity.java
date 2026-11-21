package com.example.hello;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int PICK_ACCOUNT = 1;
    private TextView greeting;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("hello", MODE_PRIVATE);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        int pad = (int) (24 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);

        greeting = new TextView(this);
        greeting.setTextSize(28);
        greeting.setGravity(Gravity.CENTER);
        layout.addView(greeting);

        Button pick = new Button(this);
        pick.setText("Choose account");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.topMargin = pad;
        pick.setLayoutParams(lp);
        pick.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { chooseAccount(); }
        });
        layout.addView(pick);

        setContentView(layout);

        String saved = prefs.getString("account", null);
        showGreeting(saved);
        if (saved == null && savedInstanceState == null) chooseAccount();
    }

    private void chooseAccount() {
        Intent intent = AccountManager.newChooseAccountIntent(
                null, null, new String[] {"com.google"}, null, null, null, null);
        startActivityForResult(intent, PICK_ACCOUNT);
    }

    private void showGreeting(String account) {
        greeting.setText(account == null ? "Hello, World!" : "Hello,\n" + account + "!");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_ACCOUNT && resultCode == RESULT_OK && data != null) {
            String name = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            prefs.edit().putString("account", name).apply();
            showGreeting(name);
        }
    }
}
