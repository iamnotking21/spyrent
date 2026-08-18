package com.ax.childapp;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SampleDeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        Toast.makeText(context,"Pwede ng i uninstall ang childApp",Toast.LENGTH_SHORT).show();
        super.onDisabled(context, intent);
    }

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        Toast.makeText(context,"Hindi na pwedeng i uninstall ang childApp",Toast.LENGTH_SHORT).show();
        super.onEnabled(context, intent);
    }

    @Nullable
    @Override
    public CharSequence onDisableRequested(@NonNull Context context, @NonNull Intent intent) {
        Toast.makeText(context," disable request device admin ",Toast.LENGTH_SHORT).show();
        return super.onDisableRequested(context, intent);
    }

    @Override
    public void onPasswordChanged(@NonNull Context context, @NonNull Intent intent, @NonNull UserHandle user) {
        super.onPasswordChanged(context, intent, user);
    }
}
