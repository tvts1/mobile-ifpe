package com.ifpe.tanajura.repo

import com.ifpe.tanajura.db.fb.FBCity
import com.ifpe.tanajura.db.fb.FBDatabase
import com.ifpe.tanajura.db.fb.FBUser
import com.ifpe.tanajura.db.fb.toFBCity
import com.ifpe.tanajura.db.local.LocalDatabase
import com.ifpe.tanajura.db.local.toCity
import com.ifpe.tanajura.db.local.toLocalCity
import com.ifpe.tanajura.model.City
import com.ifpe.tanajura.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Repository(
    private val fbDB: FBDatabase,
    private val localDB: LocalDatabase
) : FBDatabase.Listener {

    interface Listener {
        fun onUserLoaded(user: User)
        fun onUserSignOut()
        fun onCityAdded(city: City)
        fun onCityUpdated(city: City)
        fun onCityRemoved(city: City)
    }

    private var listener: Listener? = null
    private val ioScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var cityCollectionJob: Job? = null
    private var cityMap = emptyMap<String, City>()

    init {
        fbDB.setListener(this)
    }

    fun setListener(listener: Listener? = null) {
        this.listener = listener

        if (listener == null) {
            cityCollectionJob?.cancel()
            cityCollectionJob = null
        } else if (cityCollectionJob == null) {
            collectLocalCities()
        }
    }

    private fun collectLocalCities() {
        cityCollectionJob = ioScope.launch {
            localDB.getCities().collect { localCityList ->
                val cityList = localCityList.map { it.toCity() }

                withContext(Dispatchers.Main.immediate) {
                    val nameList = cityList.map { it.name }
                    val deletedCities = cityMap
                        .filter { it.key !in nameList }
                        .values
                    val updatedCities = cityList
                        .filter { it.name in cityMap.keys }
                    val newCities = cityList
                        .filter { it.name !in cityMap.keys }

                    newCities.forEach { listener?.onCityAdded(it) }
                    updatedCities.forEach { listener?.onCityUpdated(it) }
                    deletedCities.forEach { listener?.onCityRemoved(it) }

                    cityMap = cityList.associateBy { it.name }
                }
            }
        }
    }

    fun add(city: City) = fbDB.add(city.toFBCity())

    fun remove(city: City) = fbDB.remove(city.toFBCity())

    fun update(city: City) = fbDB.update(city.toFBCity())

    override fun onUserLoaded(user: FBUser) {
        listener?.onUserLoaded(user.toUser())
    }

    override fun onUserSignOut() {
        listener?.onUserSignOut()
    }

    override fun onCityAdded(city: FBCity) {
        localDB.insert(city.toCity().toLocalCity())
    }

    override fun onCityUpdated(city: FBCity) {
        localDB.update(city.toCity().toLocalCity())
    }

    override fun onCityRemoved(city: FBCity) {
        localDB.delete(city.toCity().toLocalCity())
    }
}
