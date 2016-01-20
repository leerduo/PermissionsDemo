package me.jarvischen.permissionsdemo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * Created by chenfuduo on 2016/1/20.
 */
public class PermissionsChecker {

    private Context context;

    public PermissionsChecker(Context context) {
        this.context = context;
    }

    public boolean lacksPermissions(String... permissions){
        for(String permission : permissions){
            if (lacksPermission(permission)){
                return true;
            }
        }
        return false;
    }

    private boolean lacksPermission(String permission){
        return ContextCompat.checkSelfPermission(context,permission) == PackageManager.PERMISSION_DENIED;
    }
}
