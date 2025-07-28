package com.example.e_canteenapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.e_canteenapp.ui.theme.EcanteenAppTheme
import com.google.firebase.database.*

// Assuming MenuItem is already declared elsewhere, so we don't redeclare it here

class AdminMenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcanteenAppTheme {
                AdminMenuScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMenuScreen() {
    val context = LocalContext.current
    val categories = listOf("Breakfast", "Lunch", "Snacks", "Special")
    var selectedCategory by remember { mutableStateOf("Breakfast") }

    val db = FirebaseDatabase.getInstance().getReference("menuItems").child(selectedCategory)
    var itemList by remember { mutableStateOf(listOf<com.example.e_canteenapp.MenuItem>()) }

    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    var editItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(selectedCategory) {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<com.example.e_canteenapp.MenuItem>()
                for (child in snapshot.children) {
                    val item = child.getValue(com.example.e_canteenapp.MenuItem::class.java)
                    item?.let {
                        list.add(it.copy(id = child.key ?: ""))
                    }
                }
                itemList = list
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Manage Menu Items") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Select Category")
            DropdownMenuBox(
                options = categories,
                selectedOption = selectedCategory,
                onOptionSelected = {
                    selectedCategory = it
                    name = ""
                    price = ""
                    editItemId = null
                }
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price") }, modifier = Modifier.fillMaxWidth())

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    if (name.isNotBlank() && price.isNotBlank()) {
                        val priceInt = price.toIntOrNull()
                        if (priceInt != null) {
                            val itemMap = mapOf("name" to name, "price" to priceInt)
                            if (editItemId != null) {
                                db.child(editItemId!!).updateChildren(itemMap)
                                Toast.makeText(context, "Item Updated", Toast.LENGTH_SHORT).show()
                                editItemId = null
                            } else {
                                db.push().setValue(itemMap)
                                Toast.makeText(context, "Item Added", Toast.LENGTH_SHORT).show()
                            }
                            name = ""
                            price = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (editItemId != null) "Update Item" else "Add Item")
            }

            Spacer(Modifier.height(20.dp))

            Text("Current Items:", style = MaterialTheme.typography.titleMedium)
            LazyColumn {
                itemsIndexed(itemList) { _, item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("${item.name} - ₹${item.price}")
                        }
                        Row {
                            IconButton(onClick = {
                                name = item.name
                                price = item.price.toString()
                                editItemId = item.id
                            }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = {
                                db.child(item.id).removeValue()
                                Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownMenuBox(options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = selectedOption,
            onValueChange = {},
            label = { Text("Category") },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach {
                DropdownMenuItem(text = { Text(it) }, onClick = {
                    expanded = false
                    onOptionSelected(it)
                })
            }
        }
    }
}
