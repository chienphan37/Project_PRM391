package com.example.androidproject.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.androidproject.R;


public class WelcomeActivity extends AppCompatActivity {
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        checkPermission();
    }

    private void checkPermission() {
        String[] listPermission = new String[]{
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WAKE_LOCK,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
        boolean isHaveEnoughPermission = true;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isHaveEnoughPermission = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            isHaveEnoughPermission = false;
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isHaveEnoughPermission = false;
        }
        if (!isHaveEnoughPermission) {
            showConfirmDialog(this, listPermission);
        } else {
         nextActivity();
    }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Không đủ quyền truy cập, ứng dụng tự động thoát", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    nextActivity();
                }
            }
        }
    }

    // go to main activity if have enough permission
    public void nextActivity() {
      startActivity(new Intent(this, MainActivity.class));
   }

    public void showConfirmDialog(final Activity activity, final String[] listPermission) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Xác nhận");
        builder.setMessage("Ứng dụng cần một số quyền để có thể tiếp tục, bạn có muốn tiếp tục?");
        builder.setCancelable(false);
        builder.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Toast.makeText(activity, "Không đủ quyền truy cập, ứng dụng tự động thoát", Toast.LENGTH_LONG).show();
                finish();
            }
        });
        builder.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                ActivityCompat.requestPermissions(activity, listPermission, 1);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
