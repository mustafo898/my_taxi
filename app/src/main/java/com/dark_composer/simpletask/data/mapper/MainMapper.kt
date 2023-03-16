package com.dark_composer.simpletask.data.mapper

import com.dark_composer.simpletask.data.local.entity.LocationDto
import com.dark_composer.simpletask.domain.model.LocationModel

fun LocationDto.toModel(): LocationModel {
    return LocationModel(id, lat, lon)
}

fun LocationModel.toDto(): LocationDto {
    return LocationDto(id, lat, lon)
}