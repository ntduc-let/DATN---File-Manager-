package com.permissionx.guolindev.dialog.base

import androidx.annotation.LayoutRes
import androidx.databinding.ViewDataBinding
import com.skydoves.bindables.BindingDialogFragment

open class BaseRationaleDialogFragment<T : ViewDataBinding> constructor(
    @LayoutRes val contentLayoutId: Int
) : BindingDialogFragment<T>(contentLayoutId)