package com.dark_composer.simpletask.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location")
data class LocationDto(
    @PrimaryKey
    val id: Long,
    val lat: Double,
    val lon: Double,
)