package com.ntduc.baseproject.ui.component.main.fragment.security.textwatcher

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


class GenericTextWatcher(
    private val currentView: EditText,
    nextView: EditText?
) :
    TextWatcher {
    private val nextView: EditText?
    override fun afterTextChanged(editable: Editable) {
        // TODO Auto-generated method stub
        val text = editable.toString()
        if (nextView != null && text.length == 1) {
            nextView.requestFocus()
        }
        if (text.length > 1) {
            currentView.setText(text[text.length - 1].toString())
            currentView.setSelection(1)
        }
    }

    override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
        // TODO Auto-generated method stub
    }

    override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
        // TODO Auto-generated method stub
    }

    init {
        this.nextView = nextView
    }
}