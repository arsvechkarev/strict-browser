package com.arsvechkarev.strictbrowser.extensions

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun Context.showKeyboard(editText: EditText? = null) {
    val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
    val view = editText ?: (this as Activity).window.decorView
    inputMethodManager!!.showSoftInput(view, 0)
}

fun Context.hideKeyboard() {
    val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    val token = (this as Activity).window.decorView.windowToken
    inputMethodManager.hideSoftInputFromWindow(token, 0)
}
