package com.auto.click.model

open class LocationPointsModel(
    val x: Int,
    val y: Int
)
fun LocationPointsModel.clone(): LocationPointsModel {
    return LocationPointsModel(x, y)
}