package com.example.e_canteenapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.Serializable

data class SpecialMenuItem(
    val id: String = "",
    val name: String = "",
    val price: Int = 0,
    val veg: Boolean = true
) : Serializable

data class SelectedSpecialItem(
    val name: String = "",
    val price: Int = 0,
    var quantity: Int = 0
) : Serializable

class SpecialItemActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var selectedCategory by remember { mutableStateOf("Veg") }
            var menuItems by remember { mutableStateOf<List<SpecialMenuItem>>(emptyList()) }
            val selectedItems = remember { mutableStateMapOf<String, SelectedSpecialItem>() }

            var paymentMode by remember { mutableStateOf("Offline") }
            var serviceMode by remember { mutableStateOf("Dine-In") }

            LaunchedEffect(selectedCategory) {
                fetchSpecialMenu(selectedCategory) { items ->
                    menuItems = items
                    selectedItems.clear()
                }
            }

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { selectedCategory = "Veg" }) { Text("Veg") }
                    Button(onClick = { selectedCategory = "NonVeg" }) { Text("Non-Veg") }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(menuItems) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${item.name} - ₹${item.price}", modifier = Modifier.weight(1f))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = {
                                    val currentQty = selectedItems[item.id]?.quantity ?: 0
                                    if (currentQty > 0) {
                                        selectedItems[item.id] = SelectedSpecialItem(item.name, item.price, currentQty - 1)
                                    }
                                }) {
                                    Icon(Icons.Default.Remove, contentDescription = "Minus")
                                }

                                Text("${selectedItems[item.id]?.quantity ?: 0}", modifier = Modifier.padding(horizontal = 4.dp))

                                IconButton(onClick = {
                                    val currentQty = selectedItems[item.id]?.quantity ?: 0
                                    selectedItems[item.id] = SelectedSpecialItem(item.name, item.price, currentQty + 1)
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Add")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Payment Mode:", style = MaterialTheme.typography.titleMedium)
                Row {
                    RadioButton(selected = paymentMode == "Online", onClick = { paymentMode = "Online" })
                    Text("Online", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(selected = paymentMode == "Offline", onClick = { paymentMode = "Offline" })
                    Text("Offline")
                }

                if (paymentMode == "Online") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Scan to Pay:", style = MaterialTheme.typography.titleSmall)
                    Image(
                        painter = painterResource(id = R.drawable.qr_code),
                        contentDescription = "QR Code",
                        modifier = Modifier
                            .height(150.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("Select Service Mode:", style = MaterialTheme.typography.titleMedium)
                Row {
                    RadioButton(selected = serviceMode == "Dine-In", onClick = { serviceMode = "Dine-In" })
                    Text("Dine-In", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(selected = serviceMode == "Room Service", onClick = { serviceMode = "Room Service" })
                    Text("Room Service")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val selectedList = selectedItems.values.filter { it.quantity > 0 }
                        if (selectedList.isEmpty()) {
                            Toast.makeText(this@SpecialItemActivity, "Select at least one item", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        if (paymentMode == "Online") {
                            Toast.makeText(this@SpecialItemActivity, "Please complete payment before confirming", Toast.LENGTH_LONG).show()
                            return@Button
                        }

                        val total = selectedList.sumOf { it.price * it.quantity }
                        val user = FirebaseAuth.getInstance().currentUser
                        val userId = user?.uid ?: "Unknown"

                        val userRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
                        userRef.child("name").get().addOnSuccessListener { snapshot ->
                            val username = snapshot.value?.toString() ?: "Guest"

                            val orderRef = FirebaseDatabase.getInstance().getReference("specialOrders").push()
                            val orderData = mapOf(
                                "userId" to userId,
                                "username" to username,
                                "items" to selectedList,
                                "total" to total,
                                "mode" to selectedCategory,
                                "paymentMode" to paymentMode,
                                "paymentStatus" to if (paymentMode == "Online") "Paid" else "Pending",
                                "serviceMode" to serviceMode
                            )
                            orderRef.setValue(orderData)

                            val intent = Intent(this@SpecialItemActivity, SpecialReceiptActivity::class.java).apply {
                                putExtra("itemList", ArrayList(selectedList))
                                putExtra("totalAmount", total)
                                putExtra("mode", serviceMode)
                                putExtra("username", username)
                                putExtra("paymentMode", paymentMode)
                                putExtra("paymentStatus", if (paymentMode == "Online") "Paid" else "Pending")
                            }
                            startActivity(intent)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirm Order")
                }
            }
        }
    }

    private fun fetchSpecialMenu(category: String, callback: (List<SpecialMenuItem>) -> Unit) {
        val dbRef = FirebaseDatabase.getInstance()
            .getReference("menuItems/Special/${if (category == "Veg") "Veg" else "NonVeg"}")

        dbRef.get().addOnSuccessListener { snapshot ->
            val items = mutableListOf<SpecialMenuItem>()
            snapshot.children.forEach { child ->
                val item = child.getValue(SpecialMenuItem::class.java)
                item?.let { items.add(it.copy(id = child.key ?: "")) }
            }
            callback(items)
        }
    }
}
