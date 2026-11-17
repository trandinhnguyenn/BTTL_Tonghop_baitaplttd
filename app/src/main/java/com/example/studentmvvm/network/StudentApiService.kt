package com.example.studentmvvm.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface StudentApiService {
    @GET("api/v1/students")
    suspend fun getStudents(): List<StudentDto>

    @POST("api/v1/students")
    suspend fun createStudent(@Body student: StudentDto): StudentDto
}
