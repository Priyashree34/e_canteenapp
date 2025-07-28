// ✅ Kotlin utility functions for order time restriction with BreakfastActivity integration

import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.*

object OrderTimeUtils {

    fun isBreakfastOrderAllowed(): Boolean {
        val now = Calendar.getInstance()

        val breakfastTime = Calendar.getInstance().apply {
            add(Calendar.DATE, 1) // tomorrow
            set(Calendar.HOUR_OF_DAY, 10)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val earliest = (breakfastTime.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -24) }
        val latest = (breakfastTime.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -12) }

        return now.after(earliest) && now.before(latest)
    }

    fun isLunchOrderAllowed(): Boolean {
        val now = Calendar.getInstance()

        val lunchTime = Calendar.getInstance().apply {
            add(Calendar.DATE, 1) // tomorrow
            set(Calendar.HOUR_OF_DAY, 13) // 1 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val earliest = (lunchTime.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -24) }
        val latest = (lunchTime.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -12) }

        return now.after(earliest) && now.before(latest)
    }

    fun isSnacksOrderAllowed(): Boolean {
        val now = Calendar.getInstance()

        val snackTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 16) // 4 PM today
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val latest = (snackTime.clone() as Calendar).apply { add(Calendar.HOUR_OF_DAY, -2) } // 2 PM

        return now.before(latest)
    }

    fun getCurrentOrderTime(): String {
        val format = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        return format.format(Date())
    }

    fun showOrderTime(context: android.content.Context) {
        Toast.makeText(context, "Order Time: ${getCurrentOrderTime()}", Toast.LENGTH_SHORT).show()
    }
}

// ✅ Example for BreakfastActivity.kt (Button onClick integration):
/*
Button(onClick = {
    if (!OrderTimeUtils.isBreakfastOrderAllowed()) {
        Toast.makeText(context, "You can only place Breakfast orders between 10 AM and 10 PM today for tomorrow's breakfast.", Toast.LENGTH_LONG).show()
        return@Button
    }

    // Place order logic here

}, modifier = Modifier.fillMaxWidth()) {
    Text("Confirm Order")
}
*/
