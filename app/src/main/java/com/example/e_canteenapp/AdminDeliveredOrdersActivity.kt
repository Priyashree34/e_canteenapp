package com.example.e_canteenapp

import android.os.Bundle
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
import com.google.firebase.database.*

class AdminDeliveredOrdersActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdminDeliveredOrdersScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDeliveredOrdersScreen() {
    val deliveredOrders = remember { mutableStateListOf<Booking>() }
    val context = LocalContext.current

    val db = FirebaseDatabase.getInstance("https://ecanteenapp-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val bookingsRef = db.getReference("bookings")
    val specialRef = db.getReference("specialOrders")
    val usersRef = db.getReference("users")

    fun loadDeliveredOrders(ref: DatabaseReference, isSpecial: Boolean) {
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (snap in snapshot.children) {
                    val id = snap.key ?: continue
                    val userId = snap.child("userId").getValue(String::class.java) ?: continue

                    val orderStatus = snap.child("orderStatus").getValue(String::class.java) ?: ""
                    if (!orderStatus.equals("Delivered", true)) continue

                    val orderDate = if (isSpecial) "Today" else ""
                    val deliveryTime = if (isSpecial) "4:00 PM" else snap.child("deliveryTime").getValue(String::class.java) ?: ""
                    val mode = if (isSpecial)
                        snap.child("serviceMode").getValue(String::class.java) ?: "N/A"
                    else
                        snap.child("mode").getValue(String::class.java)
                            ?: snap.child("serviceMode").getValue(String::class.java) ?: "N/A"

                    val paymentMode = snap.child("paymentMode").getValue(String::class.java) ?: "Paid"
                    val paymentStatus = snap.child("paymentStatus").getValue(String::class.java) ?: "Paid"
                    val categorySnap = snap.child("category")
                    val category = if (isSpecial) "Special" else categorySnap.value?.toString() ?: "General"
                    val itemsFormatted = StringBuilder()
                    if (snap.hasChild("items")) {
                        for (itemSnap in snap.child("items").children) {
                            val name = itemSnap.child("name").getValue(String::class.java) ?: ""
                            val qtyStr = itemSnap.child("quantity").value.toString()
                            val quantity = qtyStr.toIntOrNull() ?: 1
                            itemsFormatted.append("$name x$quantity, ")
                        }
                    } else {
                        val itemName = snap.child("itemName").getValue(String::class.java) ?: "Unknown Item"
                        val qtyStr = snap.child("quantity").value.toString()
                        val quantity = qtyStr.toIntOrNull() ?: 1
                        itemsFormatted.append("$itemName x$quantity")
                    }

                    usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(userSnap: DataSnapshot) {
                            val empName = userSnap.child("name").getValue(String::class.java) ?: "Unknown"
                            val department = userSnap.child("department").getValue(String::class.java)
                                ?: userSnap.child("dept").getValue(String::class.java) ?: "N/A"
                            val roomNo = userSnap.child("roomNo").getValue(String::class.java)
                                ?: userSnap.child("room").getValue(String::class.java) ?: "N/A"

                            deliveredOrders.add(
                                Booking(
                                    id = id,
                                    empName = empName,
                                    department = department,
                                    roomNo = roomNo,
                                    itemsFormatted = itemsFormatted.toString().trimEnd(',', ' '),
                                    orderDate = orderDate,
                                    deliveryTime = deliveryTime,
                                    mode = mode,
                                    paymentMode = paymentMode,
                                    paymentStatus = paymentStatus,
                                    orderStatus = orderStatus,
                                    category = category,
                                    cancelReason = "",
                                    isSpecial = isSpecial
                                )
                            )
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    LaunchedEffect(Unit) {
        deliveredOrders.clear()
        loadDeliveredOrders(bookingsRef, isSpecial = false)
        loadDeliveredOrders(specialRef, isSpecial = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Delivered Orders") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(8.dp)
        ) {
            items(deliveredOrders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("👤 Employee: ${order.empName}")
                        Text("🏢 Dept: ${order.department}, Room: ${order.roomNo}")
                        Text("🛒 Items: ${order.itemsFormatted}")
                        if (order.isSpecial) {
                            Text("📅 Date: ${order.orderDate}")
                        } else {
                            Text("🕒 ${order.deliveryTime}")
                        }
                        Text("📂 Category: ${order.category}")
                        Text("🏠 Mode: ${order.mode}")
                        Text("💳 Payment: ${order.paymentMode} - ${order.paymentStatus}")
                        Text("✅ Status: Delivered")
                    }
                }
            }
        }
    }
}
