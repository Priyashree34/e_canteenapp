package com.example.e_canteenapp

data class Order(
    val itemName: String = "",
    val itemPrice: Int = 0,
    val quantity: String = "",
    val totalAmount: Int = 0,
    val deliveryTime: String = "",
    val mode: String = "",
    val paymentMode: String = "",
    val orderDate: String = "",
    val orderStatus: String = "",
    val category: String = "",
    val paymentStatus: String = "",
    val userId: String = "",
    val orderTime: String = "" // ✅ Add this line

)


