package com.ntduc.baseproject.ui.base

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.ntduc.baseproject.ui.component.main.dialog.RequestPermissionAccessSettingDialog
import com.ntduc.baseproject.ui.component.main.dialog.RequestPermissionManagerExternalStorageDialog
import com.permissionx.guolindev.PermissionX
import com.permissionx.guolindev.request.PermissionBuilder
import com.skydoves.bindables.BindingActivity

abstract class BaseActivity<T : ViewDataBinding> constructor(
    @LayoutRes val contentLayoutId: Int
) : BindingActivity<T>(contentLayoutId) {

    private lateinit var requestPermissionManagerExternalStorageDialog: RequestPermissionManagerExternalStorageDialog
    private lateinit var requestPermissionAccessSettingDialog: RequestPermissionAccessSettingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        addEvent()
        addObservers()
//        initData()
    }

    override fun onStart() {
        super.onStart()

        requestPermissionsIfNeed()
    }

    open fun initView() {
        requestPermissionManagerExternalStorageDialog = RequestPermissionManagerExternalStorageDialog()
        requestPermissionAccessSettingDialog = RequestPermissionAccessSettingDialog()
        requestPermissionAccessSettingDialog.setOnAllowListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    open fun addEvent() {}

    open fun addObservers() {}

    open fun initData() {}


    private fun requestPermissionsIfNeed() {
        val permission: PermissionBuilder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                PermissionX.init(this).permissions(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
            } else {
                PermissionX.init(this).permissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        permission.onExplainRequestReason { scope, deniedList ->
            requestPermissionManagerExternalStorageDialog.setDeniedList(deniedList)
            scope.showRequestReasonDialog(dialogFragment = requestPermissionManagerExternalStorageDialog)
        }.onForwardToSettings { scope, deniedList ->
            requestPermissionManagerExternalStorageDialog.setDeniedList(deniedList)
            scope.showForwardToSettingsDialog(dialogFragment = requestPermissionManagerExternalStorageDialog)
        }.request { allGranted, _, _ ->
            if (allGranted) {
                if (!isAccessGranted()) {
                    requestPermissionAccessSettingDialog.show(supportFragmentManager, "PermissionAccessSettingDialog")
                } else {
                    initData()
                }
            }
        }
    }

    private fun isAccessGranted(): Boolean {
        return try {
            val packageManager = packageManager
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appOpsManager = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName)
            mode == AppOpsManager.MODE_ALLOWED
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
