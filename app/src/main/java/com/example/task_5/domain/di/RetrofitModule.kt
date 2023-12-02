package com.example.task_5.domain.di

import com.example.task_5.domain.network.AccountApiService
import com.example.task_5.domain.network.ContactsApiService
import com.example.task_5.utils.Constants.BASE_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RetrofitModule {

    @Provides
    @Singleton
    fun providesRetrofit() : Retrofit =
        Retrofit.Builder()
            .client(
                OkHttpClient.Builder()
                    .addInterceptor(
                        HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)).build()
            )
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()


    @Provides
    @Singleton
    fun providesAccountApiService(retrofit: Retrofit) : AccountApiService {
        return retrofit.create(AccountApiService::class.java)
    }

    @Provides
    @Singleton
    fun providesContactsApiService(retrofit: Retrofit) : ContactsApiService {
        return retrofit.create(ContactsApiService::class.java)
    }



    
}