@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.e_canteenapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

class AddEditItemActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AddEditItemScreen()
        }
    }
}

@Composable
fun AddEditItemScreen() {
    val context = LocalContext.current
    val categories = listOf("Breakfast", "Lunch", "Snacks", "Special")
    var selectedCategory by remember { mutableStateOf("Breakfast") }
    var expanded by remember { mutableStateOf(false) }

    var itemName by remember { mutableStateOf("") }
    var itemPrice by remember { mutableStateOf("") }
    var editItemId by remember { mutableStateOf<String?>(null) }
    var veg by remember { mutableStateOf(true) }

    val dbRef = FirebaseDatabase.getInstance().getReference("menuItems")
    var itemsList by remember { mutableStateOf(listOf<MenuItem>()) }

    DisposableEffect(selectedCategory, veg) {
        val typeFolder = if (veg) "Veg" else "NonVeg"
        val ref = dbRef.child(selectedCategory).child(typeFolder)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<MenuItem>()
                for (snap in snapshot.children) {
                    val item = snap.getValue(MenuItem::class.java)
                    val id = snap.key ?: continue
                    item?.let { tempList.add(it.copy(id = id)) }
                }
                itemsList = tempList
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load items", Toast.LENGTH_SHORT).show()
            }
        }

        ref.addValueEventListener(listener)
        onDispose { ref.removeEventListener(listener) }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add/Edit/Delete Item") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Veg / Non-Veg toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Type:")
                Spacer(modifier = Modifier.width(8.dp))
                RadioButton(
                    selected = veg,
                    onClick = {
                        veg = true
                        editItemId = null
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = Color.Green)
                )
                Text("Veg", color = Color.Green)
                Spacer(modifier = Modifier.width(12.dp))
                RadioButton(
                    selected = !veg,
                    onClick = {
                        veg = false
                        editItemId = null
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = Color.Red)
                )
                Text("Non-Veg", color = Color.Red)
            }

            // Category dropdown
            Box {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Category") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                selectedCategory = it
                                expanded = false
                                editItemId = null
                            }
                        )
                    }
                }
            }

            // Name and Price fields
            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = itemPrice,
                onValueChange = { itemPrice = it.filter { ch -> ch.isDigit() } },
                label = { Text("Item Price") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Button(
                onClick = {
                    if (itemName.isNotBlank() && itemPrice.isNotBlank()) {
                        val priceInt = itemPrice.toIntOrNull() ?: 0
                        if (priceInt <= 0) {
                            Toast.makeText(context, "Enter valid price", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        val typeFolder = if (veg) "Veg" else "NonVeg"
                        val itemId = editItemId
                            ?: dbRef.child(selectedCategory).child(typeFolder).push().key
                            ?: return@Button

                        val newItem = MenuItem(
                            name = itemName,
                            price = priceInt,
                            veg = veg,
                            id = itemId
                        )

                        dbRef.child(selectedCategory).child(typeFolder).child(itemId)
                            .setValue(newItem)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    context,
                                    if (editItemId != null) "Item updated" else "Item added",
                                    Toast.LENGTH_SHORT
                                ).show()
                                itemName = ""
                                itemPrice = ""
                                editItemId = null
                            }
                    } else {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (editItemId != null) "Update Item" else "Add Item")
            }

            Divider()

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(itemsList) { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                itemName = item.name
                                itemPrice = item.price.toString()
                                veg = item.veg
                                editItemId = item.id
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Name: ${item.name}")
                                Text("Price: ₹${item.price}")
                                Text(
                                    text = if (item.veg) "Veg" else "Non-Veg",
                                    color = if (item.veg) Color.Green else Color.Red
                                )
                            }
                            Button(onClick = {
                                val typeFolder = if (item.veg) "Veg" else "NonVeg"
                                dbRef.child(selectedCategory).child(typeFolder).child(item.id)
                                    .removeValue()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Item deleted", Toast.LENGTH_SHORT).show()
                                        if (editItemId == item.id) {
                                            itemName = ""
                                            itemPrice = ""
                                            editItemId = null
                                        }
                                    }
                            }) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
