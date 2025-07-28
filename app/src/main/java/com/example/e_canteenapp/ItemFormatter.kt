package com.example.e_canteenapp.util

import com.google.firebase.database.DataSnapshot

object ItemFormatter {

    fun formatItemDetails(bookingSnapshot: DataSnapshot): String {
        return if (bookingSnapshot.child("items").exists()) {
            val itemsList = mutableListOf<String>()
            for (itemSnap in bookingSnapshot.child("items").children) {
                val rawName = itemSnap.child("name").getValue(String::class.java) ?: continue
                val quantity = itemSnap.child("quantity").getValue(String::class.java) ?: "0"
                val name = rawName.split("-").first().trim()
                itemsList.add("$name x$quantity")
            }
            if (itemsList.isEmpty()) "No items found" else itemsList.joinToString(", ")
        } else {
            val itemName = bookingSnapshot.child("itemName").getValue(String::class.java) ?: "Unknown"
            val quantity = bookingSnapshot.child("quantity").getValue(String::class.java) ?: "0"
            "$itemName x$quantity"
        }
    }
}
