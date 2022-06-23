package com.samlach2222.velocityvolume.ui.volumemanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VolumeManagerViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is VolumeManager Fragment"
    }
    val text: LiveData<String> = _text
}