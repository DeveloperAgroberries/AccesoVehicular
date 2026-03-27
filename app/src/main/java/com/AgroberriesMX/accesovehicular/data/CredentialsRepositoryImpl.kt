package com.AgroberriesMX.accesovehicular.data

import com.AgroberriesMX.accesovehicular.data.local.AccesoVehicularLocalDBService
import com.AgroberriesMX.accesovehicular.domain.CredentialsRepository
import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import javax.inject.Inject

class CredentialsRepositoryImpl @Inject constructor(
    private val localDBService: AccesoVehicularLocalDBService
) : CredentialsRepository{
    override suspend fun getUserByCodeAndPassword(cUsu: String, vPassword: String): LoginModel?{
        return localDBService.getUserByCodeAndPassword(cUsu, vPassword)
    }

    override suspend fun insertUsers(users: List<LoginModel>): List<Long?> {
        return localDBService.insertUsers(users)
    }

    override suspend fun deleteAllUsers(){}
}