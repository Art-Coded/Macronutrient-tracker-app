package com.example.macrominder.Main

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface USDAApiService {
    @GET("foods/search")
    fun searchFoods(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("page") page: Int = 1,
        @Query("dataType") dataType: String? = null,
        @Query("pageSize") pageSize: Int = 25
    ): Call<USDAResponse>
}
