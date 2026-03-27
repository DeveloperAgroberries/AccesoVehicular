package com.AgroberriesMX.accesovehicular.data.core.interceptors

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().header("Authorization", tokenManager.getToken()).build()
        return chain.proceed(request)
    }
}

class TokenManager @Inject constructor(@ApplicationContext private val context: Context){
    companion object{
        private const val PRIVATE_ACCESS_TOKEN_KEY = "access_token"
    }

    private val sharedPreferences = context.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)
    fun getToken():String{
        return sharedPreferences.getString(PRIVATE_ACCESS_TOKEN_KEY, "")?:""
    }
}