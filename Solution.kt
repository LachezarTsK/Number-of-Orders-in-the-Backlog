
import kotlin.math.pow
import kotlin.math.min
import java.util.*

class Solution {

    private data class Order(val price: Int, val quantity: Int) {}

    private companion object {
        const val BUY_ORDER = 0
        const val SELL_ORDER = 1
        val MODULO_VALUE = (10.0).pow(9.0).toInt() + 7
    }

    fun getNumberOfBacklogOrders(orders: Array<IntArray>): Int {
        val maxHeapBuyOrdersBacklog = PriorityQueue<Order>() { first, second -> comparatorBuyOrder(first, second) }
        val minHeapSellOrdersBacklog = PriorityQueue<Order>() { first, second -> comparatorSellOrder(first, second) }

        processAllOrders(orders, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog)

        return findUnsettledBacklogOrders(maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog)
    }

    private fun comparatorBuyOrder(first: Order, second: Order): Int {
        return second.price - first.price
    }

    private fun comparatorSellOrder(first: Order, second: Order): Int {
        return first.price - second.price
    }

    private fun processAllOrders(orders: Array<IntArray>, maxHeapBuyOrdersBacklog: PriorityQueue<Order>, minHeapSellOrdersBacklog: PriorityQueue<Order>) {
        for (order in orders) {
            val type = order[2]

            if (type == BUY_ORDER) {
                processBuyOrder(order, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog)
                continue
            }
            if (type == SELL_ORDER) {
                processSellOrder(order, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog)
            }
        }
    }

    private fun processBuyOrder(order: IntArray, maxHeapBuyOrdersBacklog: PriorityQueue<Order>, minHeapSellOrdersBacklog: PriorityQueue<Order>) {
        val buyPrice = order[0]
        var buyQuantity = order[1]

        while (!minHeapSellOrdersBacklog.isEmpty() && minHeapSellOrdersBacklog.peek().price <= buyPrice && buyQuantity > 0) {
            val sell = minHeapSellOrdersBacklog.poll()
            val buyQuantityBeforeUpdate = buyQuantity
            buyQuantity -= min(buyQuantity, sell.quantity)
            if (sell.quantity - buyQuantityBeforeUpdate > 0) {
                minHeapSellOrdersBacklog.add(Order(sell.price, sell.quantity - buyQuantityBeforeUpdate))
            }
        }
        if (buyQuantity > 0) {
            maxHeapBuyOrdersBacklog.add(Order(buyPrice, buyQuantity))
        }
    }

    private fun processSellOrder(order: IntArray, maxHeapBuyOrdersBacklog: PriorityQueue<Order>, minHeapSellOrdersBacklog: PriorityQueue<Order>) {
        val sellPrice = order[0]
        var sellQuantity = order[1]

        while (!maxHeapBuyOrdersBacklog.isEmpty() && maxHeapBuyOrdersBacklog.peek().price >= sellPrice && sellQuantity > 0) {
            val buy = maxHeapBuyOrdersBacklog.poll()
            val sellQuantityBeforeUpdate = sellQuantity
            sellQuantity -= min(sellQuantity, buy.quantity)
            if (buy.quantity - sellQuantityBeforeUpdate > 0) {
                maxHeapBuyOrdersBacklog.add(Order(buy.price, buy.quantity - sellQuantityBeforeUpdate))
            }
        }
        if (sellQuantity > 0) {
            minHeapSellOrdersBacklog.add(Order(sellPrice, sellQuantity))
        }
    }

    private fun findUnsettledBacklogOrders(maxHeapBuyOrdersBacklog: PriorityQueue<Order>, minHeapSellOrdersBacklog: PriorityQueue<Order>): Int {
        var unsettledBacklogOrders: Long = 0
        while (!maxHeapBuyOrdersBacklog.isEmpty()) {
            val buy = maxHeapBuyOrdersBacklog.poll()
            unsettledBacklogOrders = (unsettledBacklogOrders + buy.quantity) % MODULO_VALUE
        }

        while (!minHeapSellOrdersBacklog.isEmpty()) {
            val sell = minHeapSellOrdersBacklog.poll()
            unsettledBacklogOrders = (unsettledBacklogOrders + sell.quantity) % MODULO_VALUE
        }

        return unsettledBacklogOrders.toInt()
    }
}
