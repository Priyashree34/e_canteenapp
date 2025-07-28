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
import com.example.e_canteenapp.ui.theme.EcanteenAppTheme
import com.google.firebase.database.*
import com.example.e_canteenapp.ReceiptItem
import com.example.e_canteenapp.ReceiptData


class SpecialItemsAdminActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcanteenAppTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = { Text("Manage Special Items") })
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize()) {
                        AdminSpecialItemContent()
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSpecialItemContent() {
    val dbRef = FirebaseDatabase.getInstance().getReference("menuItems/Special")
    var specialItems by remember { mutableStateOf(listOf<Pair<String, ReceiptItem>>()) }
    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }

    // Fetch data from Firebase
    LaunchedEffect(Unit) {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val itemList = mutableListOf<Pair<String, ReceiptItem>>()
                for (item in snapshot.children) {
                    val id = item.key ?: continue
                    val name = item.child("name").getValue(String::class.java) ?: ""
                    val price = item.child("price").getValue(Int::class.java) ?: 0
                    itemList.add(id to ReceiptItem(name, price, 1))
                }
                specialItems = itemList
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Input fields
        OutlinedTextField(
            value = itemName,
            onValueChange = { itemName = it },
            label = { Text("Item Name") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = itemPrice,
            onValueChange = { itemPrice = it },
            label = { Text("Price") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        Button(
            onClick = {
                val id = dbRef.push().key ?: return@Button
                val price = itemPrice.toIntOrNull() ?: 0
                dbRef.child(id).setValue(ReceiptItem(itemName, price, 1))
                itemName = ""
                itemPrice = ""
            },
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
        ) {
            Text("Add Special Item")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("🌟 Special Items:", style = MaterialTheme.typography.titleMedium)

        LazyColumn {
            items(specialItems) { (id, item) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("🍜 ${item.name}")
                            Text("💵 ₹${item.price}")
                        }
                        Button(onClick = { dbRef.child(id).removeValue() }) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}