package fr.isen.guerrand.isensmartcompanion.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.Call
import fr.isen.guerrand.isensmartcompanion.data.Event

interface EventApiService {
    @GET("events.json")
    fun getEvents(): Call<List<Event>>
}

object RetrofitClient {
    private const val BASE_URL = "https://isen-smart-companion-default-rtdb.europe-west1.firebasedatabase.app/"

    val instance: EventApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(EventApiService::class.java)
    }
}
