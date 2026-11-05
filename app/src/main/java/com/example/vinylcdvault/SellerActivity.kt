package com.example.vinylcdvault

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class SellerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_seller)

        findViewById<BottomNavigationView>(R.id.sellerBottomNav).setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_seller_products -> SellerProductsFragment()
                R.id.nav_seller_orders -> SellerOrdersFragment()
                else -> SellerProfileFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.sellerContainer, fragment)
                .commit()
            true
        }
    }
}