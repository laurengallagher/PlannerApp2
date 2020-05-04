package org.wit.plannerapp2.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import org.wit.plannerapp2.models.PlannerModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit


interface PlannerService {
    @GET("/planners")
    fun getall(): Call<List<PlannerModel>>

    @GET("/planners/{email}")
    fun findall(@Path("email") email: String?)
            : Call<List<PlannerModel>>

    @GET("/planners/{email}/{id}")
    fun get(@Path("email") email: String?,
            @Path("id") id: String): Call<PlannerModel>

    @DELETE("/planners/{email}/{id}")
    fun delete(@Path("email") email: String?,
               @Path("id") id: String): Call<PlannerWrapper>

    @POST("/planners/{email}")
    fun post(@Path("email") email: String?,
             @Body planner: PlannerModel)
            : Call<PlannerWrapper>

    @PUT("/planners/{email}/{id}")
    fun put(@Path("email") email: String?,
            @Path("id") id: String,
            @Body planner: PlannerModel
    ): Call<PlannerWrapper>

    companion object {

        val serviceURL = "https://planner2-a425b.firebaseio.com/"

        fun create() : PlannerService {

            val gson = GsonBuilder().create()

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(serviceURL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build()
            return retrofit.create(PlannerService::class.java)
        }
    }
}