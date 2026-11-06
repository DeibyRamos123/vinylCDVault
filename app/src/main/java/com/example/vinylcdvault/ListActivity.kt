package com.example.vinylcdvault

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)

        val title = findViewById<TextView>(R.id.tvListTitle)
        val rv = findViewById<RecyclerView>(R.id.rvList)
        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        if (intent.getStringExtra("mode") == "favorites") {
            title.setText(R.string.profile_favorites_title)
            rv.layoutManager = GridLayoutManager(this, 2)
            rv.adapter = ProductAdapter(SampleData.products.take(3), R.layout.item_product) {
                openProductDetail(it)
            }
        } else {
            title.setText(R.string.profile_orders_title)
            rv.layoutManager = LinearLayoutManager(this)
            rv.adapter = OrderAdapter(SampleOrders.orders)
        }
    }
}