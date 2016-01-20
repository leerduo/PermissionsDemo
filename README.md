Android6.0在权限上做了调整，在Android5.1以及之前的版本，用户安装app的时候，会授权应用需要的权限，6.0之后，根据权限类型(下文提到)的不同，会在程序运行的时候，提示用户去授权。

权限主要分为两大类，一类是普通权限(normal)一类是危险权限(dangerous)。其中普通权限在安装的时候会直接授权，而危险权限则会在程序运行需要的时候，弹出来让用户去选择是否授权。


# 运行时权限

Android6.0在权限上做了调整，在Android5.1以及之前的版本，用户安装app的时候，会授权应用需要的权限，6.0之后，根据权限类型(下文提到)的不同，会在程序运行的时候，提示用户去授权。

权限主要分为两大类，一类是普通权限(normal)一类是危险权限(dangerous)。其中普通权限在安装的时候会直接授权，而危险权限则会在程序运行需要的时候，弹出来让用户去选择是否授权。

普通权限列表如下：
```
ACCESS_LOCATION_EXTRA_COMMANDS
ACCESS_NETWORK_STATE
ACCESS_NOTIFICATION_POLICY
ACCESS_WIFI_STATE
BLUETOOTH
BLUETOOTH_ADMIN
BROADCAST_STICKY
CHANGE_NETWORK_STATE
CHANGE_WIFI_MULTICAST_STATE
CHANGE_WIFI_STATE
DISABLE_KEYGUARD
EXPAND_STATUS_BAR
GET_PACKAGE_SIZE
INTERNET
KILL_BACKGROUND_PROCESSES
MODIFY_AUDIO_SETTINGS
NFC
READ_SYNC_SETTINGS
READ_SYNC_STATS
RECEIVE_BOOT_COMPLETED
REORDER_TASKS
REQUEST_INSTALL_PACKAGES
SET_TIME_ZONE
SET_WALLPAPER
SET_WALLPAPER_HINTS
TRANSMIT_IR
USE_FINGERPRINT
VIBRATE
WAKE_LOCK
WRITE_SYNC_SETTINGS
SET_ALARM
INSTALL_SHORTCUT
UNINSTALL_SHORTCUT
```

危险权限可以分为以下几组:
```
CALENDAR
CAMERA
CONTACTS
LOCATION
MICROPHONE
PHONE
SENSORS
SMS
STORAGE
```

具体的如下图：

![危险权限列表](http://ww4.sinaimg.cn/large/6a195423jw1ezwpc11cs0j20hr0majwm.jpg)


# 检测权限是否授权


下面以一个具体的案例，来说明涉及到的api如何使用。在清单文件中声明下面的权限：
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="me.jarvischen.permissionsdemo">

    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity
            android:name=".MainActivity"
            android:label="@string/title_activity_main"
            >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

其中:

```xml
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

是一个普通的权限。而：

```xml
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

则是一个危险的权限。

那么，如何检测一个权限是否授权了，可以通过下面的API：

```java
// Assume thisActivity is the current activity
int permissionCheck = ContextCompat.checkSelfPermission(thisActivity,
        Manifest.permission.WRITE_CALENDAR);
```

如果app已经有了该权限，`ContextCompat.checkSelfPermission(context,permission)`方法返回`PackageManager.PERMISSION_GRANTED`，否则返回`PERMISSION_DENIED`.

接下来写一个工具类去检测应用的权限是否授权。
```java
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
```
在MainActivity中使用：
```java
package me.jarvischen.permissionsdemo;

import android.Manifest;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView tv = (TextView) findViewById(R.id.tv);
        PermissionsChecker checker = new PermissionsChecker(this);
        if (checker.lacksPermissions(PERMISSIONS)){
            tv.setText("no permissions");
        }
		//此处如果运行的设备低于6.0，那么可以加个else处理下。
    }
}
```
运行，看到下面的结果：
![运行结果](http://7xljei.com1.z0.glb.clouddn.com/withoutpermissions.PNG)


# 检查和请求需要权限的模式

普通权限在安装的时候就已经授权了，危险权限在用户第一次运行app的时候，会弹出对话框，让用户去选择授权或者拒绝权限，如果用户选择授权，那么这个很好处理，但是如果用户选择拒绝，那么下一次用户运行app的时候，会再次弹出提醒，如果用户勾上了`以后不再提醒`的CheckBox，并且再次拒绝了权限，那么以后再也不会弹出授权的对话框，那么相应的功能也会收到限制。

还有一种情况是用户可以随时去到setting页面，打开app，关闭或者打开某个权限，所以需要有检测权限的代码。

解决这个问题的办法是在应用中单独新建一个PermissionsActivity，这个PermissionsActivity去负责请求权限，应用中的其他Activity检测自己是否具备需要的权限，如果不具备，那么传值给PermissionsActivity，告诉它，"我"没有我需要的权限，你快去请求吧。

现在更该上面的MainActivity的代码，更该如下：

```java
package me.jarvischen.permissionsdemo;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private static final String[] PERMISSIONS = new String[]{Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS};

    private PermissionsChecker checker;
    
    private static final int REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checker = new PermissionsChecker(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checker.lacksPermissions(PERMISSIONS)) {
            startPermissionsActivity();
        }
    }

    private void startPermissionsActivity() {
        PermissionsActivity.startActivityForResult(this, REQUEST_CODE, PERMISSIONS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == PermissionsActivity.PERMISSIONS_DENIED){
            finish();
        }
    }
}
```


`onActivityResult(int requestCode, int resultCode, Intent data)`当`PermissionsActivity`类处理完请求后回调，返回相应的结果。

将检测的代码放到了`onResume()`方法中，用户可能在我们的app中，打开了setting，然后关闭了某个需要的权限，然后再返回到我们的应用，这样可能如果不在`onResume()`中处理，应用可能会崩掉。

上面的这种模式是一个很好的处理，从封装上来说，检测权限的代码全部在`PermissionsChecker`类中，请求的代码全部在`PermissionsActivity`这个Activity中。

# 请求权限

那么在`PermissionsActivity`具体的请求逻辑是这样的：
```java
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

    private void requestPermissions(String... permissions){
        ActivityCompat.requestPermissions(this,permissions,PERMISSION_REQUEST_CODE);
    }


    private void allPermissionsGranted(){
        setResult(PERMISSIONS_GRANTED);
        finish();
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
```

需要注意的第一点是在需要权限的Activity中，通过`startActivityForResult(...)`去启动的`PermissionsActivity`.

第二点就是具体的请求逻辑：

```java
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
```

其中，`ActivityCompat.requestPermissions(...)`方法和`startActivityForResult(...)`回调机制类似，将控制权(Control)传递给另外的一个Activity，当它完成的时候，再回调(通过`onRequestPermissionsResult(...)`)，


具体的运行效果如下：

![运行效果](http://7xljei.com1.z0.glb.clouddn.com/permissions.gif)


总结：
文章先是阐述了运行时权限，然后给出了怎么去检测权限是否已经授权，接着阐述了在Activity中检查(check)和请求(request)我们需要权限的模式，然后具体讲述了如何请求权限，以及提醒用户，如果拒绝了这些权限会怎样。

[源码](https://github.com/leerduo/PermissionsDemo)


参考文献：

* [Google关于runtime permissions的training文档](http://developer.android.com/training/permissions/index.html)
* [permissions](https://blog.stylingandroid.com/permissions-part-1/)

