package com.AgroberriesMX.accesovehicular.domain.usecase

import com.AgroberriesMX.accesovehicular.data.network.request.LoginRequest
import com.AgroberriesMX.accesovehicular.domain.Repository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: Repository){
    suspend operator fun invoke(
        loginRequest: LoginRequest
    )  = repository.getToken(loginRequest)
}