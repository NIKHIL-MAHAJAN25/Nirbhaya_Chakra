package com.example.nirbhaya_chakra.Data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "route_points")
data class RoutePoint(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,   // same UUID for all points in one trip
    val lat: Double,
    val lng: Double,
    val timestamp: Long
)