package com.AgroberriesMX.accesovehicular.data.local;

import android.content.Context;
import com.AgroberriesMX.accesovehicular.data.RecordsRepositoryImpl
import com.AgroberriesMX.accesovehicular.domain.RecordsRepository
import javax.inject.Singleton;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabaseHelper(@ApplicationContext context: Context): DatabaseHelper{
        return DatabaseHelper(context)
    }

    @Provides
    @Singleton
    fun provideAgroAccessLocalDBService(databaseHelper: DatabaseHelper): AccesoVehicularLocalDBService {
        return AccesoVehicularLocalDBServiceImpl(databaseHelper)
    }

    @Provides
    @Singleton
    fun provideRecordsRepository(localDBService: AccesoVehicularLocalDBService): RecordsRepository {
        return RecordsRepositoryImpl(localDBService)
    }
}
