package com.ifpe.tanajura.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.ifpe.tanajura.api.WeatherService
import com.ifpe.tanajura.db.fb.FBCity
import com.ifpe.tanajura.db.fb.FBDatabase
import com.ifpe.tanajura.db.fb.FBUser
import com.ifpe.tanajura.db.fb.toFBCity
import com.ifpe.tanajura.model.City
import com.ifpe.tanajura.model.User

class MainViewModel(
    private val db: FBDatabase,
    private val service: WeatherService
): ViewModel(),
    FBDatabase.Listener {

    private val _cities = mutableStateListOf<City>()

    val cities: List<City>
        get() = _cities.toList()

    private val _user = mutableStateOf<User?> (null)
    val user : User?
        get() = _user.value

    init {
        db.setListener(this)
    }

    fun remove(city: City) {
        db.remove(city.toFBCity())
    }

    fun addCity(name: String) {
        service.getLocation(name) { lat, lng ->
            if (lat != null && lng != null) {
                db.add(City(name = name, location = LatLng(lat, lng)).toFBCity())
            }
        }
    }

    fun addCity(location: LatLng) {
        service.getName(location.latitude, location.longitude) { name ->
            if (name != null) {
                db.add(City(name = name, location = location).toFBCity())
            }
        }
    }

    override fun onUserLoaded(user: FBUser) {
        _user.value = user.toUser()
    }

    override fun onUserSignOut() {
        _user.value = null
        _cities.clear()
    }

    override fun onCityAdded(city: FBCity) {
        _cities.add(city.toCity())
    }

    override fun onCityUpdated(city: FBCity) {
        val updatedCity = city.toCity()
        val index = _cities.indexOfFirst { it.name == updatedCity.name }
        if (index >= 0) {
            _cities[index] = updatedCity
        }
    }

    override fun onCityRemoved(city: FBCity) {
        _cities.removeAll { it.name == city.name }
    }
}

class MainViewModelFactory(
    private val db : FBDatabase,
    private val service : WeatherService
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(db, service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
