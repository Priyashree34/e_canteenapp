@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.e_canteenapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

class ReceiptActivity : ComponentActivity() {
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bookingId = intent.getStringExtra("bookingId") ?: ""
        dbRef = FirebaseDatabase.getInstance().getReference("bookings").child(bookingId)

        setContent {
            var receipt by remember { mutableStateOf<ReceiptData?>(null) }

            LaunchedEffect(Unit) {
                dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userId = snapshot.child("userId").getValue(String::class.java) ?: ""
                        val paymentStatus = snapshot.child("paymentStatus").getValue(String::class.java) ?: "Pending"
                        val paymentMode = snapshot.child("paymentMode").getValue(String::class.java) ?: "N/A"
                        val mode = snapshot.child("mode").getValue(String::class.java) ?: "N/A"
                        val orderTime = snapshot.child("orderTime").getValue(String::class.java) ?: "N/A"
                        val hasItemsList = snapshot.child("items").exists()

                        FirebaseDatabase.getInstance().getReference("menuItems")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(menuSnapshot: DataSnapshot) {
                                    val itemsList = mutableListOf<ReceiptItem>()
                                    var total = 0

                                    if (hasItemsList) {
                                        for (itemSnap in snapshot.child("items").children) {
                                            val rawName = itemSnap.child("name").getValue(String::class.java) ?: continue
                                            val quantityStr = itemSnap.child("quantity").getValue(String::class.java) ?: "0"
                                            val quantity = quantityStr.toIntOrNull() ?: 0
                                            val itemName = rawName.split("-").first().trim().lowercase()
                                            val price = findPriceInMenu(menuSnapshot, itemName)
                                            total += price * quantity
                                            itemsList.add(ReceiptItem(rawName, price, quantity))
                                        }
                                    } else {
                                        val rawName = snapshot.child("itemName").getValue(String::class.java) ?: "Unknown"
                                        val quantityStr = snapshot.child("quantity").getValue(String::class.java) ?: "0"
                                        val quantity = quantityStr.toIntOrNull() ?: 0
                                        val itemName = rawName.split("-").first().trim().lowercase()
                                        val price = findPriceInMenu(menuSnapshot, itemName)
                                        total += price * quantity
                                        itemsList.add(ReceiptItem(rawName, price, quantity))
                                    }

                                    // Fetch username
                                    FirebaseDatabase.getInstance().getReference("users").child(userId)
                                        .addListenerForSingleValueEvent(object : ValueEventListener {
                                            override fun onDataChange(userSnap: DataSnapshot) {
                                                val username = userSnap.child("name").getValue(String::class.java) ?: "Unknown"

                                                receipt = ReceiptData(
                                                    username = username,
                                                    paymentStatus = paymentStatus,
                                                    paymentMode = paymentMode,
                                                    mode = mode,
                                                    orderTime = orderTime,
                                                    totalAmount = total,
                                                    items = itemsList
                                                )
                                            }

                                            override fun onCancelled(error: DatabaseError) {}
                                        })
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            })
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            receipt?.let {
                ReceiptScreen(it)
            } ?: Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Loading receipt...", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }

    private fun findPriceInMenu(menuSnapshot: DataSnapshot, targetName: String): Int {
        for (category in menuSnapshot.children) {
            for (type in category.children) {
                for (menuItem in type.children) {
                    val menuName = menuItem.child("name").getValue(String::class.java) ?: continue
                    val price = menuItem.child("price").getValue(Int::class.java) ?: 0
                    if (menuName.trim().lowercase() == targetName.trim().lowercase()) {
                        return price
                    }
                }
            }
        }
        return 0
    }
}
@Composable
fun ReceiptScreen(receipt: ReceiptData) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Order Receipt") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text("Customer: ${receipt.username}")
            Text("Order Time: ${receipt.orderTime}")
            Text("Mode: ${receipt.mode}")
            Text("Payment Mode: ${receipt.paymentMode}")
            Text("Payment Status: ${receipt.paymentStatus}")
            Spacer(modifier = Modifier.height(16.dp))

            Text("Ordered Items:")
            LazyColumn {
                items(receipt.items) { item ->
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
            Text("Total Amount: ₹${receipt.totalAmount}", style = MaterialTheme.typography.titleMedium)
        }
    }
}
