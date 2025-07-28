package com.example.e_canteenapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_canteenapp.ui.theme.EcanteenAppTheme
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MenuActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcanteenAppTheme {
                MenuScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuScreen() {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Menu") },
                actions = {
                    // 🔔 Notifications
                    IconButton(onClick = {
                        context.startActivity(Intent(context, NotificationActivity::class.java))
                    }) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications"
                        )
                    }

                    // 👤 Profile
                    IconButton(onClick = {
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_profile),
                            contentDescription = "Profile"
                        )
                    }

                    // 🚪 Logout
                    IconButton(onClick = {
                        showLogoutDialog = true
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout),
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        // 🚨 Logout Confirmation Dialog
        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(onClick = {
                        showLogoutDialog = false
                        Firebase.auth.signOut()
                        val intent = Intent(context, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        context.startActivity(intent)
                    }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // 📋 Main Content
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Select Category", fontSize = 20.sp)

            CategoryItem("A - Breakfast")
            CategoryItem("B - Lunch")
            CategoryItem("C - Special Item")
            CategoryItem("D - Snacks")

            // 🧾 E-Order History Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        context.startActivity(Intent(context, UserOrderHistoryActivity::class.java))
                    }
                    .padding(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🧾 E-Order History", fontSize = 18.sp)
                    Text("View your digital order history", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun CategoryItem(categoryName: String) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                when {
                    categoryName.startsWith("A") -> {
                        context.startActivity(Intent(context, BreakfastActivity::class.java))
                    }
                    categoryName.startsWith("B") -> {
                        context.startActivity(Intent(context, LunchActivity::class.java))
                    }
                    categoryName.startsWith("C") -> {
                        context.startActivity(Intent(context, SpecialItemActivity::class.java))
                    }
                    categoryName.startsWith("D") -> {
                        context.startActivity(Intent(context, SnacksActivity::class.java))
                    }
                }
            }
            .padding(8.dp)
    ) {
        Text(
            text = categoryName,
            modifier = Modifier.padding(16.dp)
        )
    }
}
