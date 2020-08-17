package com.part5.view.recycler.refresh.config;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public class RecyclerConfig {
    private volatile static RecyclerConfig instance;
    private RecyclerModule module;

    private RecyclerConfig(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appInfo == null || appInfo.metaData == null) {
            return;
        }
        String moduleName = null;
        for (String key : appInfo.metaData.keySet()) {
            if (RecyclerModule.class.getSimpleName().equals(appInfo.metaData.get(key))) {
                moduleName = key;
                break;
            }
        }
        if (moduleName == null) {
            return;
        }
        Object moduleImpl = null;
        try {
            Class<?> moduleImplClass = Class.forName(moduleName);
            moduleImpl = moduleImplClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (moduleImpl instanceof RecyclerModule) {
            module = (RecyclerModule) moduleImpl;
        }
    }

    public static RecyclerConfig getInstance(Context context) {
        if (instance == null) {
            synchronized (RecyclerConfig.class) {
                if (instance == null) {
                    instance = new RecyclerConfig(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public RecyclerModule getConfig() {
        return module;
    }

}
