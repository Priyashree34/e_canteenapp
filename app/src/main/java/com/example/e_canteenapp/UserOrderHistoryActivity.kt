package com.example.e_canteenapp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UserOrderHistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UserOrderHistoryScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOrderHistoryScreen() {
    val context = LocalContext.current
    val orders = remember { mutableStateListOf<Booking>() }
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""

    val db = FirebaseDatabase.getInstance("https://ecanteenapp-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val bookingsRef = db.getReference("bookings")
    val specialRef = db.getReference("specialOrders")

    fun loadHistory(ref: DatabaseReference, isSpecial: Boolean) {
        ref.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (snap in snapshot.children) {
                        val id = snap.key ?: continue
                        val orderStatus = snap.child("orderStatus").getValue(String::class.java) ?: ""
                        if (!orderStatus.equals("Delivered", true) && !orderStatus.equals("Cancelled", true)) continue

                        val orderDate = snap.child("orderDate").getValue(String::class.java) ?: ""
                        val mode = snap.child("mode").getValue(String::class.java) ?: snap.child("serviceMode").getValue(String::class.java) ?: "N/A"
                        val paymentMode = snap.child("paymentMode").getValue(String::class.java) ?: "Paid"
                        val paymentStatus = snap.child("paymentStatus").getValue(String::class.java) ?: "Paid"
                        val category = if (isSpecial) "Special" else snap.child("category").getValue(String::class.java) ?: "Unknown"
                        val cancelReason = snap.child("cancelReason").getValue(String::class.java) ?: ""

                        val itemsFormatted = StringBuilder()
                        if (snap.hasChild("items")) {
                            for (itemSnap in snap.child("items").children) {
                                val name = itemSnap.child("name").getValue(String::class.java) ?: ""
                                val qty = itemSnap.child("quantity").value.toString().toIntOrNull() ?: 1
                                itemsFormatted.append("$name x$qty, ")
                            }
                        } else {
                            val name = snap.child("itemName").getValue(String::class.java) ?: "Unknown Item"
                            val qty = snap.child("quantity").value.toString().toIntOrNull() ?: 1
                            itemsFormatted.append("$name x$qty")
                        }

                        orders.add(
                            Booking(
                                id = id,
                                empName = "You",
                                department = "",
                                roomNo = "",
                                itemsFormatted = itemsFormatted.toString().trimEnd(',', ' '),
                                orderDate = orderDate,
                                deliveryTime = "",
                                mode = mode,
                                paymentMode = paymentMode,
                                paymentStatus = paymentStatus,
                                orderStatus = orderStatus,
                                category = category,
                                cancelReason = cancelReason,
                                isSpecial = isSpecial
                            )
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    LaunchedEffect(Unit) {
        orders.clear()
        loadHistory(bookingsRef, isSpecial = false)
        loadHistory(specialRef, isSpecial = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Your Order History") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🛒 Items: ${order.itemsFormatted}")
                        Text("📂 Category: ${order.category}")
                        Text("🏠 Mode: ${order.mode}")
                        Text("💳 Payment: ${order.paymentMode} - ${order.paymentStatus}")
                        Text("📦 Status: ${order.orderStatus}")
                        if (order.orderStatus == "Cancelled") {
                            Text("❗ Cancel Reason: ${order.cancelReason}", color = MaterialTheme.colorScheme.error)
                        }
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                showDeleteConfirmation(context) {
                                    val ref = if (order.isSpecial) specialRef else bookingsRef
                                    ref.child(order.id).removeValue()
                                    orders.remove(order)
                                    Toast.makeText(context, "🗑️ Order deleted", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete Order")
                        }
                    }
                }
            }
        }
    }
}

fun showDeleteConfirmation(context: android.content.Context, onConfirm: () -> Unit) {
    AlertDialog.Builder(context)
        .setTitle("Delete Order")
        .setMessage("Are you sure you want to delete this order?")
        .setPositiveButton("Yes") { _, _ -> onConfirm() }
        .setNegativeButton("No", null)
        .show()
}
