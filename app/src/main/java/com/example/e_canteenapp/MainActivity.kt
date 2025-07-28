package com.example.e_canteenapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.e_canteenapp.ui.theme.EcanteenAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val auth = FirebaseAuth.getInstance()

        // ✅ Redirect if already logged in
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val dbRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("role")
            dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val role = snapshot.getValue(String::class.java)?.lowercase()
                    when (role) {
                        "admin" -> {
                            startActivity(Intent(this@MainActivity, AdminDashboardActivity::class.java))
                            finish()
                        }
                        "user" -> {
                            startActivity(Intent(this@MainActivity, MenuActivity::class.java))
                            finish()
                        }
                        else -> {
                            Toast.makeText(this@MainActivity, "Invalid role", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        setContent {
            EcanteenAppTheme {
                LoginScreen(auth)
            }
        }
    }
}

@Composable
fun LoginScreen(auth: FirebaseAuth) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    auth.signInWithEmailAndPassword(email.trim(), password.trim())
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                                val dbRef = FirebaseDatabase.getInstance().getReference("users")
                                dbRef.child(uid).child("role")
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            isLoading = false
                                            val role = snapshot.getValue(String::class.java)?.lowercase()

                                            when (role) {
                                                "admin" -> {
                                                    Toast.makeText(context, "Welcome Admin", Toast.LENGTH_SHORT).show()
                                                    context.startActivity(Intent(context, AdminDashboardActivity::class.java))
                                                }
                                                "user" -> {
                                                    FirebaseMessaging.getInstance().token
                                                        .addOnCompleteListener { tokenTask ->
                                                            if (tokenTask.isSuccessful) {
                                                                val token = tokenTask.result
                                                                FirebaseDatabase.getInstance()
                                                                    .getReference("userTokens")
                                                                    .child(uid)
                                                                    .setValue(token)
                                                                    .addOnSuccessListener {
                                                                        Toast.makeText(context, "Welcome", Toast.LENGTH_SHORT).show()
                                                                        context.startActivity(Intent(context, MenuActivity::class.java))
                                                                    }
                                                            } else {
                                                                Toast.makeText(context, "FCM Token Error", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                }
                                                else -> {
                                                    Toast.makeText(context, "Invalid role", Toast.LENGTH_SHORT).show()
                                                    auth.signOut()
                                                }
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            isLoading = false
                                            Toast.makeText(context, "DB Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    })
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Logging in..." else "Login")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                }) {
                    Text("New user? Register")
                }

                TextButton(onClick = {
                    if (email.isNotEmpty()) {
                        auth.sendPasswordResetEmail(email)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Reset email sent to $email", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Forgot Password?")
                }
            }
        }
    }
}
