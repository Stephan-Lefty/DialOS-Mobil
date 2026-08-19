package org.dialos.mobil

import android.app.Application
import com.google.android.material.color.DynamicColors

class DialOsMobilApp : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
