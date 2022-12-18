package com.arsvechkarev.strictbrowser

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.arsvechkarev.strictbrowser.extensions.showKeyboard

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    private val editTextSearch by lazy { findViewById<EditText>(R.id.editTextSearch) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editTextSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                openBrowsingActivity(editTextSearch.text.toString())
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
    }

    override fun onResume() {
        super.onResume()
        editTextSearch.apply {
            postDelayed({ requestFocus(); showKeyboard(this) }, 80L)
        }
    }

    override fun onStop() {
        super.onStop()
        editTextSearch.setText("")
    }

    private fun openBrowsingActivity(text: String) {
        if (text.isBlank()) return
        startActivity(Intent(this, BrowserActivity::class.java).apply {
            putExtra(BrowserActivity.SEARCH_TEXT, text)
        })
    }
}