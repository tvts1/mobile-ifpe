package com.ifpe.tanajura

import android.app.Application
import android.content.Intent
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class WeatherApp : Application() {

    private val FLAGS = Intent.FLAG_ACTIVITY_SINGLE_TOP or // Não cria atividade se no topo
            Intent.FLAG_ACTIVITY_NEW_TASK or             // Cria nova tarefa
            Intent.FLAG_ACTIVITY_CLEAR_TASK              // Limpa o backstack

    override fun onCreate() {
        super.onCreate()

        // Monitora o estado de autenticação de forma global
        Firebase.auth.addAuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null) {
                goToMain()
            } else {
                goToLogin()
            }
        }
    }

    private fun goToMain() {
        this.startActivity(Intent(this, MainActivity::class.java).setFlags(FLAGS))
    }

    private fun goToLogin() {
        this.startActivity(Intent(this, LoginActivity::class.java).setFlags(FLAGS))
    }
}