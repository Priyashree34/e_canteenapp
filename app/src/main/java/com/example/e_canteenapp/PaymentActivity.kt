package com.example.e_canteenapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class PaymentActivity : ComponentActivity(), PaymentResultListener {

    private val auth = FirebaseAuth.getInstance()
    private val dbRef = FirebaseDatabase.getInstance().getReference("bookings")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Razorpay setup
        Checkout.preload(applicationContext)

        val orderId = intent.getStringExtra("orderId") ?: return
        val amount = intent.getDoubleExtra("amount", 0.0)

        startPayment(amount, orderId)
    }

    private fun startPayment(amount: Double, orderId: String) {
        val checkout = Checkout()
        checkout.setKeyID("YOUR_RAZORPAY_KEY_ID") // 👈 Put your real key

        try {
            val options = JSONObject().apply {
                put("name", "E-Canteen")
                put("description", "Order #$orderId")
                put("currency", "INR")
                put("amount", (amount * 100).toInt())  // Razorpay needs in paise
                put("prefill", JSONObject().apply {
                    put("email", auth.currentUser?.email)
                })
            }

            checkout.open(this, options)

        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentID: String?) {
        val orderId = intent.getStringExtra("orderId") ?: return

        dbRef.child(orderId).child("paymentStatus").setValue("Done")
        Toast.makeText(this, "Payment Successful ✅", Toast.LENGTH_LONG).show()

        // Go to ReceiptActivity
        startActivity(Intent(this, ReceiptActivity::class.java).putExtra("orderId", orderId))
        finish()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "❌ Payment Failed: $response", Toast.LENGTH_LONG).show()
        finish()
    }
}
