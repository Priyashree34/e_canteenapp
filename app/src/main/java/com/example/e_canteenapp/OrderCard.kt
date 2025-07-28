package com.example.e_canteenapp

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OrderCard(order: Booking) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Items: ${order.itemsFormatted}")
            Text(text = "Date: ${order.orderDate}")
            Text(text = "Delivery Time: ${order.deliveryTime}")
            Text(text = "Mode: ${order.mode}")
            Text(text = "Payment: ${order.paymentMode} (${order.paymentStatus})")
            Text(text = "Status: ${order.orderStatus}")
        }
    }
}
