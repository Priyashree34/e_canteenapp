package com.example.e_canteenapp

data class ReceiptItem(
    val name: String = "",
    val price: Int = 0,
    val quantity: Int = 0
)

data class ReceiptData(
    val username: String = "",
    val paymentStatus: String = "",
    val paymentMode: String = "",
    val mode: String = "",
    val orderTime: String = "",
    val totalAmount: Int = 0,
    val items: List<ReceiptItem> = emptyList()
)
