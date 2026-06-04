package com.ifpe.tanajura.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

import androidx.compose.runtime.toMutableStateList
import com.google.android.gms.maps.model.LatLng
import com.ifpe.tanajura.com.ifpe.tanajura.model.User

import com.ifpe.tanajura.model.City
private fun getCities() = List(20) { i ->
    City(name = "Cidade $i", weather = "Carregando clima...")
}

class MainViewModel : ViewModel() {

    private val _cities = getCities().toMutableStateList()

    private val _user = mutableStateOf<User?> (null)
    val user : User?
        get() = _user.value

    val cities: List<City>
        get() = _cities.toList()

    fun remove(city: City) {
        _cities.remove(city)
    }

    fun add(name: String, location: LatLng? = null) {
        _cities.add(City(name = name, location = location))
    }
}