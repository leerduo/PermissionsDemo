package me.jarvischen.permissionsdemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;

public class PermissionsActivity extends Activity {

    private static final int PERMISSION_REQUEST_CODE = 0;

    private static final String EXTRA_PERMISSIONS = "me.jarvischen.permissionsdemo.EXTRA_PERMISSIONS";

    private static final String EXTRA_FINISH = "me.jarvischen.permissionsdemo.EXTRA_FINISH";

    private static final String PACKAGE_URL_SCHEME = "package:";
    public static final int PERMISSIONS_DENIED = 1;
    public static final int PERMISSIONS_GRANTED = 0;

    private PermissionsChecker checker;

    private boolean requiresCheck;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);
        if (getIntent() == null || !getIntent().hasExtra(EXTRA_PERMISSIONS)) {
            throw new RuntimeException("This Activity needs to be launched using the static startActivityForResult() method.");
        }
        checker = new PermissionsChecker(this);
        requiresCheck = true;
    }


    public static void startActivityForResult(Activity activity, int requestCode, String... permissions) {
        Intent intent = new Intent(activity, PermissionsActivity.class);
        intent.putExtra(EXTRA_PERMISSIONS, permissions);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, null);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (requiresCheck){
            String[] permissions = getPermissions();
            if (checker.lacksPermissions(permissions)){
                requestPermissions(permissions);
            }else{
                allPermissionsGranted();
            }
        }else{
            requiresCheck = true;
        }
    }

    private String[] getPermissions(){
        return getIntent().getStringArrayExtra(EXTRA_PERMISSIONS);
    }

    private void allPermissionsGranted(){
        setResult(PERMISSIONS_GRANTED);
        finish();
    }


    private void requestPermissions(String... permissions){
        ActivityCompat.requestPermissions(this,permissions,PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE && hasAllPermissionsGranted(grantResults)){
            requiresCheck = true;
            allPermissionsGranted();
        }else{
            requiresCheck = false;
            showMissingPermissionDialog();
        }
    }

    private void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(PermissionsActivity.this);
        builder.setTitle(R.string.help);
        builder.setMessage(R.string.string_help_text);
        builder.setNegativeButton(R.string.quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setResult(PERMISSIONS_DENIED);
                finish();
            }
        });
        builder.setPositiveButton(R.string.settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startAppSettings();
            }
        });
        builder.show();
    }

    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }

    private boolean hasAllPermissionsGranted(int[] grantResults){
        for (int grantResult : grantResults){
            if (grantResult == PackageManager.PERMISSION_DENIED){
                return false;
            }
        }
        return true;
    }

}
