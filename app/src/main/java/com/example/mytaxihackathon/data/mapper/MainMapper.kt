package com.example.mytaxihackathon.data.mapper

import com.example.mytaxihackathon.data.local.entity.LocationDto
import com.example.mytaxihackathon.domain.model.LocationModel

fun LocationDto.toModel(): LocationModel {
    return LocationModel(id.toLong(), lat = latitude, lon = longitude)
}

fun LocationModel.toDto(): LocationDto {
    return LocationDto(id.toDouble(), lat, lon)
}