package com.example.e_canteenapp


data class Booking(
    val id: String = "",
    val empName: String = "",
    val department: String = "",
    val roomNo: String = "",
    val itemsFormatted: String = "",
    val orderDate: String = "",
    val deliveryTime: String = "",
    val mode: String = "",
    val paymentMode: String = "",
    val paymentStatus: String = "",
    val orderStatus: String = "",
    val cancelReason: String = "",
    val category: String = "",
    val isSpecial: Boolean = false // <-- NEW
)

