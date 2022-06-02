package com.samlach2222.velocityvolume.ui.HomePage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomePageViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is HomePage Fragment"
    }
    val text: LiveData<String> = _text
}