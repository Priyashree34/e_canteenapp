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
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            var isRegistered by remember { mutableStateOf(false) }

            if (isRegistered) {
                finish() // safely finish activity from composable
            }

            RegisterScreen(auth = auth) {
                isRegistered = true
            }
        }
    }
}

@Composable
fun RegisterScreen(auth: FirebaseAuth, onRegisterSuccess: () -> Unit) {
    val context = LocalContext.current

    var empId by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }
    var roomNo by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var intercom by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Error states
    var empIdError by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    var deptError by remember { mutableStateOf(false) }
    var roomError by remember { mutableStateOf(false) }
    var mobileError by remember { mutableStateOf(false) }
    var intercomError by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var confirmPasswordError by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Register", fontSize = 24.sp)
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = empId,
                onValueChange = {
                    empId = it
                    empIdError = it.length != 8
                },
                isError = empIdError,
                label = { Text("Employee ID (8-digit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (empIdError) Text("Employee ID must be 8 digits", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = it.isBlank()
                },
                isError = nameError,
                label = { Text("Employee Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (nameError) Text("Name cannot be empty", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = department,
                onValueChange = {
                    department = it
                    deptError = it.isBlank()
                },
                isError = deptError,
                label = { Text("Department") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (deptError) Text("Department cannot be empty", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = roomNo,
                onValueChange = {
                    roomNo = it
                    roomError = it.isBlank()
                },
                isError = roomError,
                label = { Text("Room No") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (roomError) Text("Room number cannot be empty", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = mobile,
                onValueChange = {
                    mobile = it
                    mobileError = it.length != 10
                },
                isError = mobileError,
                label = { Text("Mobile Number (10-digit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (mobileError) Text("Mobile number must be 10 digits", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = intercom,
                onValueChange = {
                    intercom = it
                    intercomError = it.length != 3
                },
                isError = intercomError,
                label = { Text("Intercom (3-digit)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (intercomError) Text("Intercom must be 3 digits", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = it.isBlank()
                },
                isError = emailError,
                label = { Text("Email") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (emailError) Text("Email cannot be empty", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = it.length < 6
                },
                isError = passwordError,
                label = { Text("Password (min 6 chars)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            if (passwordError) Text("Password must be at least 6 characters", color = MaterialTheme.colorScheme.error)

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    confirmPasswordError = it != password
                },
                isError = confirmPasswordError,
                label = { Text("Confirm Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )
            if (confirmPasswordError) Text("Passwords do not match", color = MaterialTheme.colorScheme.error)

            Spacer(modifier = Modifier.height(24.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        // Validate all fields
                        empIdError = empId.length != 8
                        nameError = name.isBlank()
                        deptError = department.isBlank()
                        roomError = roomNo.isBlank()
                        mobileError = mobile.length != 10
                        intercomError = intercom.length != 3
                        emailError = email.isBlank()
                        passwordError = password.length < 6
                        confirmPasswordError = confirmPassword != password

                        if (empIdError || nameError || deptError || roomError ||
                            mobileError || intercomError || emailError || passwordError || confirmPasswordError
                        ) {
                            Toast.makeText(context, "Please fix the errors", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        isLoading = true

                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    val uid = auth.currentUser?.uid ?: ""
                                    val userMap = mapOf(
                                        "empId" to empId,
                                        "name" to name,
                                        "department" to department,
                                        "roomNo" to roomNo,
                                        "mobile" to mobile,
                                        "intercom" to intercom,
                                        "email" to email,
                                        "role" to "user"
                                    )

                                    FirebaseDatabase.getInstance().getReference("users")
                                        .child(uid)
                                        .setValue(userMap)
                                        .addOnCompleteListener {
                                            Toast.makeText(context, "✅ Registered successfully", Toast.LENGTH_SHORT).show()
                                            auth.signOut()
                                            context.startActivity(Intent(context, MainActivity::class.java))
                                            onRegisterSuccess()
                                        }
                                } else {
                                    Toast.makeText(context, "❌ Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                                }
                            }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register")
                }
            }
        }
    }
}
