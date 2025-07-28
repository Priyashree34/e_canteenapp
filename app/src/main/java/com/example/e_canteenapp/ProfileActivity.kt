package com.example.e_canteenapp

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.e_canteenapp.ui.theme.EcanteenAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EcanteenAppTheme {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId)
    val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")

    var name by remember { mutableStateOf("Loading...") }
    var empId by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("Loading...") }

    var dept by remember { mutableStateOf("") }
    var room by remember { mutableStateOf("") }
    var mobile by remember { mutableStateOf("") }
    var intercom by remember { mutableStateOf("") }

    var imageUrl by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf(false) }

    val textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            storageRef.putFile(it)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        imageUrl = downloadUri.toString()
                        Toast.makeText(context, "✅ Profile photo updated", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(context, "❌ Upload failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Fetch data from Firebase
    LaunchedEffect(userId) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                name = snapshot.child("name").getValue(String::class.java) ?: "N/A"
                empId = snapshot.child("empId").getValue(String::class.java) ?: "N/A"
                email = snapshot.child("email").getValue(String::class.java) ?: "N/A"
                dept = snapshot.child("department").getValue(String::class.java)
                    ?: snapshot.child("dept").getValue(String::class.java) ?: ""
                room = snapshot.child("roomNo").getValue(String::class.java)
                    ?: snapshot.child("room").getValue(String::class.java) ?: ""
                mobile = snapshot.child("mobile").getValue(String::class.java) ?: ""
                intercom = snapshot.child("intercom").getValue(String::class.java) ?: ""
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "DB Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })

        storageRef.downloadUrl.addOnSuccessListener {
            imageUrl = it.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Profile image
        Box(modifier = Modifier.size(150.dp)) {
            Image(
                painter = imageUrl?.let { rememberAsyncImagePainter(it) }
                    ?: painterResource(id = R.drawable.ic_default_profile),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { launcher.launch("image/*") }
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_camera),
                contentDescription = "Change Photo",
                modifier = Modifier
                    .size(30.dp)
                    .align(Alignment.BottomEnd)
                    .clickable { launcher.launch("image/*") },
                tint = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Name: $name", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text("Employee ID: $empId", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Text("Email: $email", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        // Edit toggle button
        Button(onClick = { editing = !editing }) {
            Text(if (editing) "Cancel Edit" else "Edit Profile")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Editable fields
        OutlinedTextField(
            value = dept,
            onValueChange = { dept = it },
            label = { Text("Department", style = textStyle) },
            textStyle = textStyle,
            enabled = editing,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = room,
            onValueChange = { room = it },
            label = { Text("Room No.", style = textStyle) },
            textStyle = textStyle,
            enabled = editing,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text("Mobile (10-digit)", style = textStyle) },
            textStyle = textStyle,
            enabled = editing,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = intercom,
            onValueChange = { intercom = it },
            label = { Text("Intercom (8-digit)", style = textStyle) },
            textStyle = textStyle,
            enabled = editing,
            modifier = Modifier.fillMaxWidth()
        )

        // Save button
        if (editing) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (mobile.length != 10 || !mobile.all { it.isDigit() }) {
                        Toast.makeText(context, "❌ Mobile must be 10 digits", Toast.LENGTH_LONG).show()
                        return@Button
                    }
                    if (intercom.length != 8 || !intercom.all { it.isDigit() }) {
                        Toast.makeText(context, "❌ Intercom must be 8 digits", Toast.LENGTH_LONG).show()
                        return@Button
                    }

                    dbRef.updateChildren(
                        mapOf(
                            "department" to dept,
                            "roomNo" to room,
                            "mobile" to mobile,
                            "intercom" to intercom
                        )
                    ).addOnSuccessListener {
                        editing = false
                        Toast.makeText(context, "✅ Profile updated", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        Toast.makeText(context, "❌ Update failed", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
}

