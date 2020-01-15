package com.junemon.travellingapps.domain.usecase

import android.net.Uri
import androidx.lifecycle.LiveData
import com.junemon.model.domain.PlaceCacheData
import com.junemon.model.domain.PlaceRemoteData
import com.junemon.model.domain.Results
import com.junemon.travellingapps.domain.repository.PlaceRepository
import javax.inject.Inject

/**
 * Created by Ian Damping on 04,December,2019
 * Github https://github.com/iandamping
 * Indonesia.
 */
class PlaceUseCase @Inject constructor(private val repository: PlaceRepository) {

    fun getCache(): LiveData<Results<List<PlaceCacheData>>> = repository.getCache()
    fun getSelectedTypeCache(placeType: String): LiveData<Results<List<PlaceCacheData>>> = repository.getSelectedTypeCache(placeType)
    suspend fun delete() = repository.delete()
    fun uploadFirebaseData(data: PlaceRemoteData, imageUri: Uri?, success: (Boolean) -> Unit, failed: (Boolean, Throwable) -> Unit) = repository.uploadFirebaseData(data, imageUri, success, failed)
}