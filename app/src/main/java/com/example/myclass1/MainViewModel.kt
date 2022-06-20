package com.example.myclass1

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel :ViewModel(){
    private val _isloading= MutableStateFlow(true)
    val isLoading =_isloading

    init {

        viewModelScope.launch {
            delay(1000)
            _isloading.value=false
        }
    }

}




