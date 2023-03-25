package com.ntduc.baseproject.ui.base

import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow

class BasePopupWindow(
    root: View
) : PopupWindow(
    root,
    LinearLayout.LayoutParams.WRAP_CONTENT,
    LinearLayout.LayoutParams.WRAP_CONTENT,
    false
)