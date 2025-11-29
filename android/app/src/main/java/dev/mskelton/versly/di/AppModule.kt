package dev.mskelton.versly.di

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.mskelton.versly.api.BASE_URL
import dev.mskelton.versly.api.VerslyService
import dev.mskelton.versly.persistence.AppPreferences
import dev.mskelton.versly.persistence.BibleDatabase
import javax.inject.Singleton
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(
                GsonConverterFactory.create(
                    GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create()
                )
            )
            .baseUrl(BASE_URL)
            .build()
    }

    @Provides
    @Singleton
    fun provideVerslyService(retrofit: Retrofit): VerslyService {
        return retrofit.create(VerslyService::class.java)
    }

    @Provides
    @Singleton
    fun provideBibleDatabase(
        @ApplicationContext context: Context,
        verslyService: VerslyService,
    ): BibleDatabase {
        return BibleDatabase(context, verslyService)
    }

    @Provides
    @Singleton
    fun provideAppPreferences(@ApplicationContext context: Context): AppPreferences {
        return AppPreferences(context)
    }
}
