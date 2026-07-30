package com.ifpe.tanajura.api

import android.content.Context
import android.graphics.Bitmap
import androidx.core.graphics.drawable.toBitmap
import com.google.android.gms.maps.model.LatLng
import coil.ImageLoader
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherService(private val context: Context) {
    private var weatherAPI: WeatherServiceAPI
    private val imageLoader = ImageLoader.Builder(context)
        .allowHardware(false)
        .build()

    init {
        val retrofitAPI = Retrofit.Builder()
            .baseUrl(WeatherServiceAPI.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        weatherAPI = retrofitAPI.create(WeatherServiceAPI::class.java)
    }

    suspend fun getName(lat: Double, lng: Double): String? = withContext(Dispatchers.IO) {
        search("$lat,$lng")?.name
    }

    suspend fun getLocation(name: String): LatLng? = withContext(Dispatchers.IO) {
        search(name)?.let { location ->
            val lat = location.lat
            val lng = location.lon
            if (lat != null && lng != null) LatLng(lat, lng) else null
        }
    }

    private fun search(query: String): APILocation? {
        val call: Call<List<APILocation>?> = weatherAPI.search(query)
        val apiLocation = call.execute().body()
        return if (!apiLocation.isNullOrEmpty()) apiLocation[0] else null
    }

    suspend fun getWeather(name: String): APICurrentWeather? =
        withContext(Dispatchers.IO) {
        val call: Call<APICurrentWeather?> = weatherAPI.weather(name)
        call.execute().body()
    }

    suspend fun getForecast(name: String): APIWeatherForecast? =
        withContext(Dispatchers.IO) {
        val call: Call<APIWeatherForecast?> = weatherAPI.forecast(name)
        call.execute().body()
    }

    suspend fun getBitmap(imgUrl: String): Bitmap? = withContext(Dispatchers.IO) {
        val request = ImageRequest.Builder(context)
            .data(imgUrl)
            .allowHardware(false)
            .build()
        imageLoader.execute(request).drawable?.toBitmap()
    }
}
