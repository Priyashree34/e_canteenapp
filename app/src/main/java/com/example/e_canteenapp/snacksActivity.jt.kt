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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.e_canteenapp.ui.theme.EcanteenAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class SnacksActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcanteenAppTheme {
                SnacksScreen()
            }
        }
    }
}

@Composable
fun SnacksScreen() {
    val context = LocalContext.current
    val database = FirebaseDatabase.getInstance().getReference("menuItems/Snacks")

    var snacksItems by remember { mutableStateOf(listOf<MenuItem>()) }
    var selectedItem by remember { mutableStateOf<MenuItem?>(null) }
    var showItemDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") }

    val calendar = Calendar.getInstance()
    val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)

    var quantity by remember { mutableStateOf("") }
    var mode by remember { mutableStateOf("Dine In") }
    var paymentMode by remember { mutableStateOf("Offline") }
    var showQrImage by remember { mutableStateOf(false) }

    // ✅ Load menu items based on filter using Lunch-style logic
    LaunchedEffect(selectedFilter) {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<MenuItem>()
                snapshot.children.forEach { typeSnap ->
                    val isVeg = typeSnap.key == "Veg"
                    typeSnap.children.forEach { itemSnap ->
                        val name = itemSnap.child("name").getValue(String::class.java) ?: return@forEach
                        val price = itemSnap.child("price").getValue(Int::class.java) ?: 0
                        val item = MenuItem(name = name, price = price, veg = isVeg)

                        when (selectedFilter) {
                            "Veg" -> if (isVeg) items.add(item)
                            "Non-Veg" -> if (!isVeg) items.add(item)
                            else -> items.add(item)
                        }
                    }
                }
                snacksItems = items
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load items", Toast.LENGTH_SHORT).show()
            }
        })
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Snacks Order") }) }) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Order Date: $currentDate")
            Text("Order Time: $currentTime")

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Filter:")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedFilter == "All", onClick = { selectedFilter = "All" })
                    Text("All")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedFilter == "Veg", onClick = { selectedFilter = "Veg" })
                    Text("Veg", color = Color.Green)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = selectedFilter == "Non-Veg", onClick = { selectedFilter = "Non-Veg" })
                    Text("Non-Veg", color = Color.Red)
                }
            }

            OutlinedTextField(
                value = selectedItem?.let { "${it.name} - ₹${it.price}" } ?: "Select Item",
                onValueChange = {},
                label = { Text("Select Item") },
                trailingIcon = {
                    IconButton(onClick = { showItemDialog = true }) {
                        Icon(Icons.Default.ArrowDropDown, null)
                    }
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (showItemDialog) {
                AlertDialog(
                    onDismissRequest = { showItemDialog = false },
                    title = { Text("Select your item") },
                    text = {
                        LazyColumn {
                            items(snacksItems) { item ->
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
                                    Text(
                                        text = if (item.veg) "[Veg]" else "[Non-Veg]",
                                        color = if (item.veg) Color.Green else Color.Red
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "${item.name} - ₹${item.price}", color = Color.Black)
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showItemDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Text("Mode")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = mode == "Dine In", onClick = { mode = "Dine In" })
                Text("Dine In")
                Spacer(Modifier.width(16.dp))
                RadioButton(selected = mode == "Room Service", onClick = { mode = "Room Service" })
                Text("Room Service")
            }

            Text("Payment")
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = paymentMode == "Online", onClick = { paymentMode = "Online" })
                Text("Online")
                Spacer(Modifier.width(16.dp))
                RadioButton(selected = paymentMode == "Offline", onClick = { paymentMode = "Offline" })
                Text("Offline")
            }

            if (paymentMode == "Online") {
                Button(onClick = {
                    if (selectedItem != null && quantity.isNotBlank()) {
                        showQrImage = true
                        Toast.makeText(context, "Scan the QR shown below.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Please select item and quantity", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Pay via QR")
                }

                if (showQrImage) {
                    Image(
                        painter = painterResource(id = R.drawable.qr_code),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val now = Calendar.getInstance()
                    val startWindow = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 14)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }
                    val endWindow = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, 15)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }

                    if (now.before(startWindow) || now.after(endWindow)) {
                        Toast.makeText(
                            context,
                            "Order can be placed only between 2:00 PM to 3:00 PM for 4:00 PM delivery.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    if (paymentMode == "Online" && !showQrImage) {
                        Toast.makeText(context, "No payment proof. Scan QR before confirming.", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    if (selectedItem == null || quantity.isEmpty()) {
                        Toast.makeText(context, "Please fill all details!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val ref = FirebaseDatabase.getInstance().getReference("bookings")
                    val bookingId = ref.push().key
                    val data = mapOf(
                        "userId" to (FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"),
                        "itemName" to selectedItem!!.name,
                        "itemPrice" to selectedItem!!.price,
                        "quantity" to quantity,
                        "orderDate" to currentDate,
                        "orderTime" to currentTime,
                        "deliveryTime" to "04:00 PM",
                        "mode" to mode,
                        "paymentMode" to paymentMode,
                        "paymentStatus" to if (paymentMode == "Online") "Pending" else "Paid",
                        "orderStatus" to "Confirmed",
                        "category" to "Snacks"

                    )
                    bookingId?.let {
                        ref.child(it).setValue(data)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Order Confirmed!", Toast.LENGTH_SHORT).show()
                                val i = Intent(context, ReceiptActivity::class.java)
                                i.putExtra("bookingId", bookingId)
                                context.startActivity(i)
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirm Order")
            }
        }
    }
}

