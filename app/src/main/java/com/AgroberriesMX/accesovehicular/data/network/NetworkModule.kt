package com.AgroberriesMX.accesovehicular.data.network

import com.AgroberriesMX.accesovehicular.BuildConfig.BASE_URL
import com.AgroberriesMX.accesovehicular.data.RepositoryImpl
import com.AgroberriesMX.accesovehicular.data.core.interceptors.AuthInterceptor
import com.AgroberriesMX.accesovehicular.domain.Repository
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
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient):Retrofit{
        return Retrofit
            .Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient{
        return OkHttpClient
            .Builder()
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .addInterceptor(authInterceptor)
            .build()
    }

    @Provides
    fun provideAgroAccessApiService(retrofit: Retrofit):AccesoVehicularApiService{
        return retrofit.create(AccesoVehicularApiService::class.java)
    }

    @Provides
    fun provideAgroAccessRepository(agroAccessApiService: AccesoVehicularApiService): Repository {
        return RepositoryImpl(agroAccessApiService)
    }
}