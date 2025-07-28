package com.example.e_canteenapp.models

data class BookingOrder(
    val itemName: String = "",
    val category: String = "",
    val quantity: Int = 0,
    val price: Int = 0,
    val total: Int = 0,
    val paymentMode: String = "",
    val timestamp: Long = 0
)
