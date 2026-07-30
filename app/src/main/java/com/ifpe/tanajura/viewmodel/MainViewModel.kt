package com.ifpe.tanajura.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.model.LatLng
import com.ifpe.tanajura.api.WeatherService
import com.ifpe.tanajura.api.toForecast
import com.ifpe.tanajura.api.toWeather
import com.ifpe.tanajura.model.City
import com.ifpe.tanajura.model.Forecast
import com.ifpe.tanajura.model.User
import com.ifpe.tanajura.model.Weather
import com.ifpe.tanajura.monitor.ForecastMonitor
import com.ifpe.tanajura.repo.Repository
import com.ifpe.tanajura.ui.nav.Route

class MainViewModel(
    private val repository: Repository,
    private val service: WeatherService,
    private val monitor: ForecastMonitor
): ViewModel(),
    Repository.Listener {

    private val _cities = mutableStateMapOf<String, City>()

    val cities: List<City>
        get() = _cities.values.toList().sortedBy { it.name }

    private val _weather = mutableStateMapOf<String, Weather>()

    private val _forecast = mutableStateMapOf<String, List<Forecast>?>()

    private val _user = mutableStateOf<User?> (null)
    val user : User?
        get() = _user.value

    private var _city = mutableStateOf<String?>(null)
    var city: String?
        get() = _city.value
        set(tmp) { _city.value = tmp }

    private var _page = mutableStateOf<Route>(Route.Home)
    var page: Route
        get() = _page.value
        set(tmp) { _page.value = tmp }

    init {
        repository.setListener(this)
    }

    fun remove(city: City) {
        repository.remove(city)
    }

    fun update(city: City) {
        repository.update(city)
    }

    fun addCity(name: String) {
        service.getLocation(name) { lat, lng ->
            if (lat != null && lng != null) {
                repository.add(City(name = name, location = LatLng(lat, lng)))
            }
        }
    }

    fun addCity(location: LatLng) {
        service.getName(location.latitude, location.longitude) { name ->
            if (name != null) {
                repository.add(City(name = name, location = location))
            }
        }
    }

    fun weather(name: String) = _weather.getOrPut(name) {
        loadWeather(name)
        Weather.LOADING
    }

    fun forecast(name: String) = _forecast.getOrPut(name) {
        loadForecast(name)
        emptyList()
    }

    private fun loadWeather(name: String) {
        service.getWeather(name) { apiWeather ->
            apiWeather?.let {
                _weather[name] = apiWeather.toWeather()
                loadBitmap(name)
            }
        }
    }

    private fun loadBitmap(name: String) {
        _weather[name]?.let { weather ->
            service.getBitmap(weather.imgUrl) { bitmap ->
                _weather[name] = weather.copy(bitmap = bitmap)
            }
        }
    }

    private fun loadForecast(name: String) {
        service.getForecast(name) { apiForecast ->
            apiForecast?.let {
                _forecast[name] = apiForecast.toForecast()
            }
        }
    }

    override fun onUserLoaded(user: User) {
        _user.value = user
    }

    override fun onUserSignOut() {
        monitor.cancelAll()
        _user.value = null
        _cities.clear()
        _weather.clear()
        _forecast.clear()
        city = null
        page = Route.Home
    }

    override fun onCityAdded(city: City) {
        _cities[city.name] = city
        monitor.updateCity(city)
    }

    override fun onCityUpdated(city: City) {
        _cities.remove(city.name)
        _cities[city.name] = city
        monitor.updateCity(city)
    }

    override fun onCityRemoved(city: City) {
        monitor.cancelCity(city)
        _cities.remove(city.name)
        _weather.remove(city.name)
        _forecast.remove(city.name)
    }
}

class MainViewModelFactory(
    private val repository: Repository,
    private val service : WeatherService,
    private val monitor: ForecastMonitor
) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, service, monitor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
