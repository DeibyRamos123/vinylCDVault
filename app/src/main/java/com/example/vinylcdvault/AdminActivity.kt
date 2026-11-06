package com.example.vinylcdvault

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class AdminActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        findViewById<BottomNavigationView>(R.id.adminBottomNav).setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_admin_dashboard -> AdminDashboardFragment()
                R.id.nav_admin_users -> AdminUsersFragment()
                R.id.nav_admin_products -> AdminProductsFragment()
                else -> AdminReportsFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.adminContainer, fragment)
                .commit()
            true
        }
    }
}