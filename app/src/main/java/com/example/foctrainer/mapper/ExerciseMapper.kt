package com.example.foctrainer.mapper

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.example.foctrainer.entity.ExerciseModel
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseMapper{

    @Query("SELECT * FROM ExerciseTable")
    fun getAllExercises(): Flow<List<ExerciseModel>>

    @Query("SELECT name FROM ExerciseTable WHERE id=:exerciseId ")
    fun getExerciseNameById(exerciseId:Int): Flow<String>

//    @Query("SELECT name FROM ExerciseTable WHERE id=:exerciseId ")
//    fun getExerciseNameById(exerciseId:Int): String
}
