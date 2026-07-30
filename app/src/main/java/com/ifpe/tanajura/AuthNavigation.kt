package com.ifpe.tanajura

import android.app.Activity
import android.content.Intent

fun Activity.openMainScreen() {
    startActivity(
        Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    )
    finish()
}

fun Activity.openLoginScreen() {
    startActivity(
        Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    )
    finish()
}
