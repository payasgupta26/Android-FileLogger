package com.bosphere.fileloggerdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bosphere.filelogger.FL;
import com.bosphere.filelogger.FLGameConfig;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int REQ_PERMISSION = 1233;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION) {
            if (grantResults.length > 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                finish();
            }
        }
    }

    public void onClickLog(View view) throws PackageManager.NameNotFoundException {
        FL.v("this is a log");
        FL.d("this is a log");
        FL.i("this is a log");
        FL.w("this is a log");
        FL.e("this is a log");
        //FL.e("this is a log with exception", new RuntimeException("dummy exception"));
        FL.x("this is x");
        FL.setGameConfig(new FLGameConfig("abcd", System.currentTimeMillis()));
        FL.g("this is game");
        File zipFile = FL.compressFiles(this);
        Uri pathUri =
                FileProvider.getUriForFile(this, getPackageName()+".provider", zipFile);
        Intent intent = ShareCompat.IntentBuilder.from(this)
                                                 .setType("application/zip")
                                                 .setStream(pathUri)
                                                 .setSubject("app logs")
                                                 .setEmailTo(new String[]{"payas@swoo.tv"})
                                                 .setChooserTitle("share logs")
                .getIntent();

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
