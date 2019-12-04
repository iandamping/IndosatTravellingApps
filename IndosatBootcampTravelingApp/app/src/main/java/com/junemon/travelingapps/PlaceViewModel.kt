package com.junemon.travelingapps

import androidx.lifecycle.LiveData
import com.ian.app.helper.base.BaseViewModel
import com.junemon.travellingapps.domain.model.PlaceCacheData
import com.junemon.travellingapps.domain.usecase.PlaceUseCase

/**
 * Created by Ian Damping on 04,December,2019
 * Github https://github.com/iandamping
 * Indonesia.
 */
class PlaceViewModel(private val repository: PlaceUseCase) : BaseViewModel() {
    fun getCache(): LiveData<List<PlaceCacheData>> = repository.getCache()
    suspend fun delete() = repository.delete()
    suspend fun setCache() = repository.setCache()
}