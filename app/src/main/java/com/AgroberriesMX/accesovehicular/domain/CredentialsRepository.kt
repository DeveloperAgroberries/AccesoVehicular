package com.AgroberriesMX.accesovehicular.domain

import com.AgroberriesMX.accesovehicular.domain.model.LoginModel

interface CredentialsRepository {
    suspend fun getUserByCodeAndPassword(cUsu: String, vPassword: String): LoginModel?
    suspend fun insertUsers(user: List<LoginModel>): List<Long?>
    suspend fun deleteAllUsers()
}