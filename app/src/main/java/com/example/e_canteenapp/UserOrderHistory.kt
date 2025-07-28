data class OrderItemHistory(
    val itemName: String = "",
    val quantity: Int = 0,
    val price: String = ""
)

data class UserOrderHistory(
    val orderId: String = "",
    val date: String = "",
    val status: String = "",
    val totalPrice: String = "",
    val items: List<OrderItemHistory> = emptyList()
)
