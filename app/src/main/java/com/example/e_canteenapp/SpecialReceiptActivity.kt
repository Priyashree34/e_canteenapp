@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.e_canteenapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class SpecialReceiptActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val username = intent.getStringExtra("username") ?: "Unknown"
        val paymentMode = intent.getStringExtra("paymentMode") ?: "Unknown"
        val paymentStatus = intent.getStringExtra("paymentStatus") ?: "Pending"
        val totalAmount = intent.getIntExtra("totalAmount", 0)
        val mode = intent.getStringExtra("mode") ?: "Dine-In" // Should be "Dine-In" or "Room Service"
        val itemList = intent.getSerializableExtra("itemList") as? ArrayList<SelectedSpecialItem> ?: arrayListOf()

        val orderTime = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        saveOrderToFirebase(username, paymentMode, paymentStatus, totalAmount, mode, itemList, orderTime)

        setContent {
            SpecialReceiptScreen(
                username = username,
                paymentMode = paymentMode,
                paymentStatus = paymentStatus,
                totalAmount = totalAmount,
                mode = mode,
                itemList = itemList,
                orderTime = orderTime
            )
        }
    }

    private fun saveOrderToFirebase(
        username: String,
        paymentMode: String,
        paymentStatus: String,
        totalAmount: Int,
        mode: String,
        itemList: List<SelectedSpecialItem>,
        orderTime: String
    ) {
        val database = FirebaseDatabase.getInstance().reference
        val orderId = database.child("specialOrders").push().key ?: return

        val orderData = mapOf(
            "username" to username,
            "paymentMode" to paymentMode,
            "paymentStatus" to paymentStatus,
            "totalAmount" to totalAmount,
            "mode" to mode,
            "orderTime" to orderTime,
            "items" to itemList.map {
                mapOf(
                    "name" to it.name,
                    "price" to it.price,
                    "quantity" to it.quantity
                )
            }
        )

        database.child("specialOrders").child(orderId).setValue(orderData)
    }
}

@Composable
fun SpecialReceiptScreen(
    username: String,
    paymentMode: String,
    paymentStatus: String,
    totalAmount: Int,
    mode: String,
    itemList: List<SelectedSpecialItem>,
    orderTime: String
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Special Order Receipt") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Customer Name: $username", style = MaterialTheme.typography.titleMedium)
            Text("Mode: $mode")
            Text("Payment Mode: $paymentMode")
            Text("Payment Status: $paymentStatus")
            Text("Order Time: $orderTime")
            Spacer(modifier = Modifier.height(16.dp))

            Text("Ordered Items:", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                items(itemList) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${item.name} × ${item.quantity}")
                        Text("₹${item.price * item.quantity}")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Amount: ₹$totalAmount", style = MaterialTheme.typography.titleLarge)
        }
    }
}
