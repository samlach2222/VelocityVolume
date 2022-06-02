package com.samlach2222.velocityvolume.ui.Settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SettingsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Settings Fragment"
    }
    val text: LiveData<String> = _text
}