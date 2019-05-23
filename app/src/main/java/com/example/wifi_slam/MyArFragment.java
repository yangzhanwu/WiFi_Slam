package com.example.wifi_slam;

import com.blankj.utilcode.util.ToastUtils;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ux.ArFragment;

public class MyArFragment extends ArFragment {
    protected void handleSessionException( UnavailableException sessionException) {
        String message = null;
        if (sessionException instanceof UnavailableArcoreNotInstalledException) {
            message = "请安装ARCore";
        } else if (sessionException instanceof UnavailableApkTooOldException) {
            message = "请升级ARCore";
        } else if (sessionException instanceof UnavailableSdkTooOldException) {
            message = "请升级app";
        } else if (sessionException instanceof UnavailableDeviceNotCompatibleException) {
            message = "当前设备部不支持AR";
        } else {
            message = "未能创建AR会话,请查看机型适配,arcore版本与系统版本";
            String var4 = sessionException.toString();
        }

        ToastUtils.showLong(message);
    }
}
