package com.example.e_canteenapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.e_canteenapp.ui.theme.EcanteenAppTheme
import com.google.firebase.database.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class AdminNotificationSenderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcanteenAppTheme {
                NotificationSenderScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSenderScreen() {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    val dbRef = FirebaseDatabase.getInstance().getReference("userTokens")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Send Notification") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notification Title") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = body,
                onValueChange = { body = it },
                label = { Text("Notification Message") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (title.isBlank() || body.isBlank()) {
                        Toast.makeText(context, "Fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isSending = true

                    // ✅ Replace with your generated access token
                    val accessToken = " ya29.c.c0ASRK0GbAY5wCC0S5cXG7COB5g3Vi7zpTu3RUPVSd7uRjkV4NkgFb9zxIdIro2ryVP2pTew9-e-EDvJygRPbt2Ua-Tn60L2CntSu-4NXuDwekD-IE9zT_HnivuxAn8C8U9en--FeojoEEZv7EBmp8j_QGr_lxD3veRKd7fi5q\n" +
                            "Ry8oVLEEXpzoeZ6ulh6qkHWDceiEK1Yn1hZ3b3BQKMV-tArIqtoQhVU4YE5YXSsEyfBObXCv6YdQQ9wBjwjntEvu3wsdqa5pzRkAuWbk_J2Y-A6BocxthVfH4MixwTuijMaurxyabvRxuDL8TawA-tuTAu0UACuXEdaCE3LG5pCEp69vZL\n" +
                            "U-yM9zetg-oASBGRUfkMBzEHYle8psE385A9vyXi77wdMlB9ki-FwIuXSXOkMvd8sc-tB0ufQBg92pFV5_3h3Rjawwq39kdQJZlBZVf1kvWc8VnRs1reen2bWw5iMozRqgg2f18w5zQe2eysgd4-RXSO0Z-wg5lwuXMWzyZ6_Zu848wVv6\n" +
                            "_b-iIzMBrpt-FUYjxJdYs0d741qwybsWb7Wc52BSlkQUycmkctXzZq3qzY2_wkplYMolJS0vy29u7MZmJz4fu9bVxBQhnM3bBMoVhmX7zdR39ikmOlp31B3SyrdnsgF4cuawvrc-RwFmh6Uryg47hQpwe9e_d-5mZ8qJ_hbaQ5WV8g81W9\n" +
                            "ukXbtWvVhnpsloSMBnYk9v83I0Z1QVlBMJdyjuosOrsQUf51rYJ3wyl_wxoJOlc-JqFeFqV7oI8Bfpn3ogMWIvQ78iRjeh3xoeegBhbJpIis9rIoRgiOtzhw8iW3VS_5myg_4U22_7WmtI-63an3Xw6lJquXMZa_8x6ZJVvb6J64g2oJBk\n" +
                            "-qujop6pfx_p1I9mzuIZikxeIQqcaqehjJRy0hV4fv-9Zd-s_Xtwt23qSmRQI_VsIdixgFzvjfykV5v9Ia_sI6o2BBWp3nsb8a7a0mFyU2j8uaJ-qOhfx7w3xpd3SMyVcx_7-1J\n"

                    dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            for (userSnap in snapshot.children) {
                                val token = userSnap.getValue(String::class.java)
                                token?.let {
                                    sendPushNotificationToToken(it, title, body, accessToken)
                                }
                            }
                            Toast.makeText(context, "Notifications sent", Toast.LENGTH_SHORT).show()
                            isSending = false
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                            isSending = false
                        }
                    })
                },
                enabled = !isSending,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isSending) "Sending..." else "Send Notification")
            }
        }
    }
}

fun sendPushNotificationToToken(token: String, title: String, body: String, accessToken: String) {
    val json = JSONObject().apply {
        put("message", JSONObject().apply {
            put("token", token)
            put("notification", JSONObject().apply {
                put("title", title)
                put("body", body)
            })
        })
    }

    val client = OkHttpClient()
    val mediaType = "application/json; charset=utf-8".toMediaType()
    val requestBody = json.toString().toRequestBody(mediaType)

    val request = Request.Builder()
        .url("https://fcm.googleapis.com/v1/projects/ecanteenapp/messages:send")
        .addHeader("Authorization", "Bearer $accessToken")
        .addHeader("Content-Type", "application/json")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            println("Notification send failed: ${e.message}")
        }

        override fun onResponse(call: Call, response: Response) {
            println("Notification sent: ${response.body?.string()}")
        }
    })
}
