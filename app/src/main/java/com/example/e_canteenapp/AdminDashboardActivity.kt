package com.example.e_canteenapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AdminDashboardActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        // Redirect to login if not authenticated
        if (auth.currentUser == null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContent {
            AdminDashboardScreen(onLogout = {
                auth.signOut()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            })
        }

        deleteOldOrders()
    }

    private fun deleteOldOrders() {
        val ref = FirebaseDatabase.getInstance().getReference("bookings")
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -30)
        }
        val cutoffDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (orderSnap in snapshot.children) {
                    val orderDateStr = orderSnap.child("orderDate").getValue(String::class.java)
                    if (!orderDateStr.isNullOrEmpty()) {
                        try {
                            val orderDate = dateFormat.parse(orderDateStr)
                            val cutoff = dateFormat.parse(cutoffDate)
                            if (orderDate != null && cutoff != null && orderDate.before(cutoff)) {
                                orderSnap.ref.removeValue()
                            }
                        } catch (_: Exception) {
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(onLogout: () -> Unit) {
    val context = LocalContext.current
    var showLogoutDialog by remember { mutableStateOf(false) }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to logout?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logout), // make sure you have this icon
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DashboardButton("Order History") {
                context.startActivity(Intent(context, AdminOrderHistoryActivity::class.java))
            }

            DashboardButton("Add / Edit Items") {
                context.startActivity(Intent(context, AddEditItemActivity::class.java))
            }

            DashboardButton("Pending Orders") {
                context.startActivity(Intent(context, AdminPendingOrdersActivity::class.java))
            }

            DashboardButton("Delivered Orders") {
                context.startActivity(Intent(context, AdminDeliveredOrdersActivity::class.java))
            }
        }
    }
}

@Composable
fun DashboardButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(text = label)
    }
}
