package com.xes.teacher.myapplication;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ShellUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xes.teacher.myapplication.databinding.ActivityMainBinding;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();

//                ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessibilitySettingsActivity");
////
//                Intent intent = new Intent();
//                intent.setComponent(componentName);
//                startActivity(intent);

                initAccessibilityService();
            }
        });

//        Toast.makeText(this, "请优先开启 " + getString(R.string.accessibilityTab) + " 的权限", Toast.LENGTH_SHORT).show();
//        startService(new Intent(this, WQAccessibilityService.class));

        Toast.makeText(this, "障碍开关：" + isStartAccessibilityServiceEnable(this), Toast.LENGTH_SHORT).show();

        PermissionUtils.requestWriteSettings(new PermissionUtils.SimpleCallback() {
            @Override
            public void onGranted() {
                ToastUtils.showLong("获取修改系统权限成功");
            }

            @Override
            public void onDenied() {
                ToastUtils.showLong("获取修改系统权限失败");
            }
        });

//        PermissionUtils.permission("android.permission.READ_PHONE_STATE", "android.permission.WRITE_SECURE_SETTINGS");

    }

    /**
     * 判断无障碍服务是否开启
     *
     * @param context
     * @return
     */
    public static boolean isStartAccessibilityServiceEnable(Context context) {
        AccessibilityManager accessibilityManager =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        assert accessibilityManager != null;
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(
                        AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().contains(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化无障碍
     */
    private void initAccessibilityService() {
        ShellUtils.execCmd("pm grant camera.app.com.backward android.permission.READ_PHONE_STATE", false);
        ShellUtils.CommandResult commandResult = ShellUtils.execCmd("pm grant camera.app.com.backward android.permission.WRITE_SECURE_SETTINGS", false);

        LogUtils.d("数据是："  +commandResult.toString());

//        if (b) {
            Log.d("system", "WRITE_SECURE_SETTINGS SUCCESS!");
//                自动开启无障碍服务
            if (!isStartAccessibilityServiceEnable(this)) {
                Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
                        getPackageName() + "/" + getPackageName() + ".service.AutoClickService");
                Settings.Secure.putInt(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, 1);
                Log.d("system", "SETTING ACCESSIBILITY SUCCESS!");
            }
//        }
    }
}