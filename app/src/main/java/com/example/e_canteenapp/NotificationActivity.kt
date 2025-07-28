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
import androidx.compose.ui.unit.dp
import com.example.e_canteenapp.ui.theme.EcanteenAppTheme
import com.google.firebase.database.*

class NotificationActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcanteenAppTheme {
                NotificationScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen() {
    var notifications by remember { mutableStateOf(listOf<NotificationModel>()) }

    LaunchedEffect(true) {
        val ref = FirebaseDatabase.getInstance().getReference("notifications")
        ref.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val tempList = mutableListOf<NotificationModel>()
                for (child in snapshot.children) {
                    val notification = child.getValue(NotificationModel::class.java)
                    notification?.let { tempList.add(it) }
                }

                // Only keep notifications from the last 24 hours
                val oneDayAgo = System.currentTimeMillis() - 24 * 60 * 60 * 1000
                notifications = tempList
                    .filter { it.timestamp > oneDayAgo }
                    .sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                // You can log the error if needed
            }
        })
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Notifications") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            items(notifications) { notification ->
                NotificationCard(notification)
            }
        }
    }
}

@Composable
fun NotificationCard(notification: NotificationModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = notification.title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = notification.body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
