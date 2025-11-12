package com.example.trucktrack.di

import javax.inject.Qualifier


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OverpassApiQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OsrmApiQualifier