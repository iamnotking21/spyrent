package com.ax.childapp;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class prevent_uninstalling extends AppCompatActivity implements View.OnClickListener {
    private PolicyManager policyManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.disable_uninstalling);
        policyManager = new PolicyManager(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.activate_admin:
                if(!policyManager.isAdminActive()){

                    Intent activateDeviceAdmin = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,policyManager.getAdminComponent());
                    activateDeviceAdmin.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"After activating admin, you will be alble to block application uninstallation");
                    startActivityForResult(activateDeviceAdmin,PolicyManager.DPM_ACTIVATION_REQUEST_CODE);
                }
                break;
            case R.id.deactivate_admin:
                if(policyManager.isAdminActive()){
                    policyManager.disableAdmin();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(resultCode == Activity.RESULT_OK && requestCode == PolicyManager.DPM_ACTIVATION_REQUEST_CODE){
            Log.v("successfully"," enabled the admin");
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
