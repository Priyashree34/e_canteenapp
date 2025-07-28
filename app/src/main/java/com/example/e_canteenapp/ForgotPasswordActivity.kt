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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ForgotPasswordActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcanteenAppTheme {
                ForgotPasswordScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen() {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val dbRef = FirebaseDatabase.getInstance().getReference("users")

    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Forgot Password") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Enter your registered email") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        isLoading = true

                        // Step 1: Check if email exists in FirebaseAuth
                        auth.fetchSignInMethodsForEmail(email)
                            .addOnCompleteListener { authTask ->
                                if (authTask.isSuccessful) {
                                    val signInMethods = authTask.result?.signInMethods
                                    if (signInMethods.isNullOrEmpty()) {
                                        Toast.makeText(context, "❌ Email is not registered", Toast.LENGTH_SHORT).show()
                                        isLoading = false
                                    } else {
                                        // Step 2: Optional - Check if email exists in Realtime DB
                                        dbRef.orderByChild("email").equalTo(email)
                                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                                override fun onDataChange(snapshot: DataSnapshot) {
                                                    isLoading = false
                                                    if (snapshot.exists()) {
                                                        // ✅ Registered user - Send reset email
                                                        auth.sendPasswordResetEmail(email)
                                                            .addOnSuccessListener {
                                                                Toast.makeText(context, "📩 Reset email sent to $email", Toast.LENGTH_LONG).show()
                                                            }
                                                            .addOnFailureListener {
                                                                Toast.makeText(context, "❌ Failed: ${it.message}", Toast.LENGTH_SHORT).show()
                                                            }
                                                    } else {
                                                        Toast.makeText(context, "❌ Email not found in database", Toast.LENGTH_SHORT).show()
                                                    }
                                                }

                                                override fun onCancelled(error: DatabaseError) {
                                                    isLoading = false
                                                    Toast.makeText(context, "DB Error: ${error.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            })
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(context, "Auth error: ${authTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                    } else {
                        Toast.makeText(context, "Please enter your email", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text(if (isLoading) "Checking..." else "Send Reset Email")
            }
        }
    }
}

