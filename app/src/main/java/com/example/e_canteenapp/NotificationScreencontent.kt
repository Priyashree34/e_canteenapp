package com.example.e_canteenapp

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*

// ✅ Data model
data class NotificationItem(
    val title: String = "",
    val body: String = "",
    val timestamp: Long = 0L
)

// ✅ Notification screen
@Composable
fun NotificationScreencontent() {
    var notifications by remember { mutableStateOf(listOf<NotificationItem>()) }
    val ref = FirebaseDatabase.getInstance().getReference("notifications")

    // 🔁 Real-time listener using DisposableEffect
    DisposableEffect(Unit) {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val notifList = mutableListOf<NotificationItem>()
                for (child in snapshot.children) {
                    val item = child.getValue(NotificationItem::class.java)
                    item?.let { notifList.add(it) }
                }
                notifications = notifList.sortedByDescending { it.timestamp }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("NotifScreen", "Failed to load notifications: ${error.message}")
            }
        }

        ref.orderByChild("timestamp").addValueEventListener(listener)

        // 🔚 Clean up when Composable leaves
        onDispose {
            ref.removeEventListener(listener)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Notifications", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {
            items(notifications) { notification ->
                NotificationCard(notification)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

// ✅ Card view for each notification
@Composable
fun NotificationCard(item: NotificationItem) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(item.body, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Text(formatTimestamp(item.timestamp), style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ✅ Timestamp formatter
@SuppressLint("SimpleDateFormat")
fun formatTimestamp(timestamp: Long): String {
    return try {
        val sdf = java.text.SimpleDateFormat("dd MMM yyyy • hh:mm a")
        sdf.format(java.util.Date(timestamp))
    } catch (e: Exception) {
        ""
    }
}
