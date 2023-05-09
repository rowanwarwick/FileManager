package com.example.filemanager

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelForPath: ViewModel() {
    val liveDataPath = MutableLiveData<String>()
}