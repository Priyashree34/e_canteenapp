package com.example.e_canteenapp

data class AdminBooking(
    val id: String = "",
    val empName: String = "",
    val itemDetails: String = "",
    val orderDate: String = "",
    val deliveryTime: String = "",
    val mode: String = "",
    val paymentMode: String = "",
    val paymentStatus: String = "",
    val orderStatus: String = ""
)
