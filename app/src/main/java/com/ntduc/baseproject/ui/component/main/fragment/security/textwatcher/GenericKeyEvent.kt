package com.ntduc.baseproject.ui.component.main.fragment.security.textwatcher

import android.view.KeyEvent
import android.view.View
import android.widget.EditText


class GenericKeyEvent(
    private val currentView: EditText,
    private val previousView: EditText?
) :
    View.OnKeyListener {
    override fun onKey(v: View?, keyCode: Int, event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.text.toString().isEmpty()) {
            previousView?.requestFocus()
            return true
        }
        return false
    }
}