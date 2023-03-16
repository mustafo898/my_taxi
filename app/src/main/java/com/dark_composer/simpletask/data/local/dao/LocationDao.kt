package com.dark_composer.simpletask.data.local.dao

import androidx.room.*
import com.dark_composer.simpletask.data.local.entity.LocationDto

@Dao
interface LocationDao {

    /**  LOCATION type CRUD */

    @Query("SELECT * FROM location")
    fun getLocation(): List<LocationDto>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setLocation(users: List<LocationDto>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun setLocation(user: LocationDto)

    @Delete
    fun deleteLocation(user: LocationDto)

    @Query("DELETE FROM location")
    fun deleteAllLocation()

    /**  LOCATION type CRUD */

}