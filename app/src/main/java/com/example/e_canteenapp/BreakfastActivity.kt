@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.e_canteenapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.e_canteenapp.ui.theme.EcanteenAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class BreakfastActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcanteenAppTheme {
                BreakfastOrderScreen()
            }
        }
    }
}

@Composable
fun BreakfastOrderScreen() {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().getReference("menuItems/Breakfast")

    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
    var showItemDialog by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("Dine In") }
    var paymentMode by remember { mutableStateOf("Online") }
    var paymentDone by remember { mutableStateOf(false) }
    var showQR by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }
    var menuItems by remember { mutableStateOf(listOf<MenuItem>()) }

    val currentDate = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
    }

    val orderTime = remember {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }

    // Load menu items with veg/non-veg support
    LaunchedEffect(selectedFilter) {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<MenuItem>()
                snapshot.children.forEach { child ->
                    if (child.hasChild("name") && child.hasChild("price")) {
                        val name = child.child("name").getValue(String::class.java) ?: return@forEach
                        val price = child.child("price").getValue(Int::class.java) ?: 0
                        val vegRaw = child.child("veg").value
                        val isVeg = when (vegRaw) {
                            is Boolean -> vegRaw
                            is String -> vegRaw.trim().lowercase() == "true"
                            else -> true
                        }
                        val id = child.key ?: ""
                        val item = MenuItem(name = name, price = price, id = id, veg = isVeg)

                        when (selectedFilter) {
                            "Veg" -> if (item.veg) items.add(item)
                            "Non-Veg" -> if (!item.veg) items.add(item)
                            else -> items.add(item)
                        }
                    } else {
                        child.children.forEach { nested ->
                            val name = nested.child("name").getValue(String::class.java) ?: return@forEach
                            val price = nested.child("price").getValue(Int::class.java) ?: 0
                            val vegRaw = nested.child("veg").value
                            val isVeg = when (vegRaw) {
                                is Boolean -> vegRaw
                                is String -> vegRaw.trim().lowercase() == "true"
                                else -> true
                            }
                            val id = nested.key ?: ""
                            val item = MenuItem(name = name, price = price, id = id, veg = isVeg)

                            when (selectedFilter) {
                                "Veg" -> if (item.veg) items.add(item)
                                "Non-Veg" -> if (!item.veg) items.add(item)
                                else -> items.add(item)
                            }
                        }
                    }
                }
                menuItems = items
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Breakfast Order") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Order Date: $currentDate")
            Text("Order Time: $orderTime")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Filter:")
                RadioButton(selected = selectedFilter == "All", onClick = { selectedFilter = "All" })
                Text("All")
                RadioButton(selected = selectedFilter == "Veg", onClick = { selectedFilter = "Veg" })
                Text("Veg", color = Color.Green)
                RadioButton(selected = selectedFilter == "Non-Veg", onClick = { selectedFilter = "Non-Veg" })
                Text("Non-Veg", color = Color.Red)
            }

            OutlinedTextField(
                value = selectedItem?.let { "${it.name} - ₹${it.price}" } ?: "Select Item",
                onValueChange = {},
                readOnly = true,
                label = { Text("Select Item") },
                trailingIcon = {
                    IconButton(onClick = { showItemDialog = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            if (showItemDialog) {
                AlertDialog(
                    onDismissRequest = { showItemDialog = false },
                    confirmButton = {},
                    dismissButton = {
                        TextButton(onClick = { showItemDialog = false }) {
                            Text("Close")
                        }
                    },
                    title = { Text("Select Item") },
                    text = {
                        LazyColumn {
                            items(menuItems) { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedItem = item
                                            showItemDialog = false
                                        }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = if (item.veg) "[Veg]" else "[Non-Veg]",
                                        color = if (item.veg) Color.Green else Color.Red)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "${item.name} - ₹${item.price}")
                                }
                            }
                        }
                    }
                )
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Mode:")
            Row {
                RadioButton(selected = mode == "Dine In", onClick = { mode = "Dine In" })
                Text("Dine In")
                RadioButton(selected = mode == "Take Away", onClick = { mode = "Take Away" })
                Text("Take Away")
            }

            Text("Payment:")
            Row {
                RadioButton(selected = paymentMode == "Online", onClick = {
                    paymentMode = "Online"
                    paymentDone = false
                    showQR = false
                })
                Text("Online")
                RadioButton(selected = paymentMode == "Offline", onClick = {
                    paymentMode = "Offline"
                    paymentDone = false
                    showQR = false
                })
                Text("Offline")
            }

            if (paymentMode == "Online" && !paymentDone) {
                Button(onClick = { showQR = true }) {
                    Text("Pay via QR")
                }
            }

            if (showQR && !paymentDone) {
                Image(
                    painter = painterResource(id = R.drawable.qr_code),
                    contentDescription = "QR Code",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(8.dp)
                )
                Text("Scan the above QR to pay", modifier = Modifier.align(Alignment.CenterHorizontally))
                Button(onClick = {
                    paymentDone = true
                    Toast.makeText(context, "Payment marked as completed.", Toast.LENGTH_SHORT).show()
                }) {
                    Text("I have paid")
                }
            }

            Button(
                onClick = {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val item = selectedItem
                    val qty = quantity.toIntOrNull() ?: 0

                    if (userId == null || item == null || qty == 0) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    if (paymentMode == "Online" && !paymentDone) {
                        Toast.makeText(context, "Complete payment before confirming order.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val totalAmount = item.price * qty
                    val order = Order(
                        itemName = item.name,
                        itemPrice = item.price,
                        quantity = qty.toString(),
                        category = "Breakfast",
                        totalAmount = totalAmount,
                        deliveryTime = "9:00 AM",
                        mode = mode,
                        paymentMode = paymentMode,
                        orderDate = currentDate,
                        orderStatus = "Pending",
                        paymentStatus = if (paymentDone) "Paid" else "Pending",
                        userId = userId,
                        orderTime = orderTime
                    )

                    val orderRef = FirebaseDatabase.getInstance().getReference("bookings").push()
                    orderRef.setValue(order).addOnSuccessListener {
                        Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                        quantity = ""
                        selectedItem = null
                        paymentDone = false
                        showQR = false

                        val intent = Intent(context, ReceiptActivity::class.java)
                        intent.putExtra("bookingId", orderRef.key)
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Order")
            }
        }
    }
}
