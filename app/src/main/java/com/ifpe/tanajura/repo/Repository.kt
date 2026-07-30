package com.ifpe.tanajura.repo

import com.ifpe.tanajura.db.fb.FBDatabase
import com.ifpe.tanajura.db.fb.toFBCity
import com.ifpe.tanajura.db.local.LocalDatabase
import com.ifpe.tanajura.db.local.toCity
import com.ifpe.tanajura.db.local.toLocalCity
import com.ifpe.tanajura.model.City
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class Repository(
    private val fbDB: FBDatabase,
    private val localDB: LocalDatabase
) {
    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var cityMap = emptyMap<String, City>()

    val cities = localDB.getCities().map { list ->
        list.map { city -> city.toCity() }
    }

    val user = fbDB.user.map { it.toUser() }

    init {
        ioScope.launch {
            fbDB.cities.collect { fbCityList ->
                val cityList = fbCityList.map { it.toCity() }
                val nameList = cityList.map { it.name }
                val deletedCities = cityMap.filter { it.key !in nameList }
                val updatedCities = cityList.filter { it.name in cityMap.keys }
                val newCities = cityList.filter { it.name !in cityMap.keys }

                newCities.forEach { localDB.insert(it.toLocalCity()) }
                updatedCities.forEach { localDB.update(it.toLocalCity()) }
                deletedCities.forEach { localDB.delete(it.value.toLocalCity()) }

                cityMap = cityList.associateBy { it.name }
            }
        }
    }

    fun add(city: City) = ioScope.launch {
        fbDB.add(city.toFBCity())
    }

    fun remove(city: City) = ioScope.launch {
        fbDB.remove(city.toFBCity())
    }

    fun update(city: City) = ioScope.launch {
        fbDB.update(city.toFBCity())
    }
}
