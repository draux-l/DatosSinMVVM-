package com.example.datossinmvvm

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query


@Dao
interface UserDAO {
    @Query("Select * FROM User")
    suspend fun getAll(): List<User>
    @Insert
    suspend fun insert(user: User)

    @Query("DELETE FROM User WHERE uid = :id")
    suspend fun deleteById(id: Int)
}