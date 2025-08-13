package com.healthtracker.offline

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for the Offline Doctor Tracker app.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class OfflineDoctorTrackerApplication : Application()