package com.example.trucktrack.di

import android.app.Application
import android.content.Context
import com.example.trucktrack.BuildConfig
import com.example.trucktrack.rest.AppApiInterface
import com.example.trucktrack.rest.WebConstants.KEY_DEVICE_TOKEN
import com.example.trucktrack.rest.WebConstants.KEY_DEVICE_TYPE
import com.example.trucktrack.rest.WebConstants.KEY_IS_TEST_DATA
import com.example.trucktrack.rest.WebConstants.REQUEST_TIMEOUT
import com.example.trucktrack.util.SharedPref
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideContext(app: Application): Context = app

    @OverpassApiQualifier
    @Provides
    @Singleton
    fun provideOverpassApi(): AppApiInterface =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_OVERPASS_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AppApiInterface::class.java)

    @OsrmApiQualifier
    @Provides
    @Singleton
    fun provideOsrmApi(): AppApiInterface =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_OSRM_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AppApiInterface::class.java)

    @Provides
    @Singleton
    fun provideSharedPrefRepository(
        @ApplicationContext context: Context
    ) = SharedPref(context = context)

    @Provides
    internal fun provideGson(): Gson =
        GsonBuilder().apply {
            setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            serializeNulls()
        }.create()

    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        val okClient = OkHttpClient.Builder()
        okClient.connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
        okClient.writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
        okClient.readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)

        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY
        okClient.addInterceptor(interceptor)

        okClient.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .addHeader("device", KEY_DEVICE_TYPE)
                .addHeader("device_token", KEY_DEVICE_TOKEN)
                .addHeader("is_testdata", KEY_IS_TEST_DATA)
            val request = requestBuilder.build()
            chain.proceed(request)
        }

        return okClient.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(gson: Gson, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create(gson))
//            .baseUrl(BuildConfig.BASE_URL)
            .baseUrl(BuildConfig.BASE_NGROCK_URL)
            .client(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideApiInterface(retrofit: Retrofit): AppApiInterface =
        retrofit.create(AppApiInterface::class.java)
}
