data class SpecialReceiptItem(
    val name: String,
    val price: Int,
    val quantity: Int
)

data class SpecialReceiptData(
    val username: String,
    val paymentStatus: String,
    val paymentMode: String,
    val mode: String,
    val orderTime: String,
    val totalAmount: Int,
    val items: List<SpecialReceiptItem>
)
