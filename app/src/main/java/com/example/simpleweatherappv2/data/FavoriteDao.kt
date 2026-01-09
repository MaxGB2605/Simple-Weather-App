package com.example.simpleweatherappv2.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_locations")
    fun getAllFavorites(): Flow<List<FavoriteLocation>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: FavoriteLocation)

    @Delete
    suspend fun delete(location: FavoriteLocation)

    @Query("SELECT EXISTS(SELECT * FROM favorite_locations WHERE cityName = :cityName)")
    fun isFavorite(cityName: String): Flow<Boolean>
}
