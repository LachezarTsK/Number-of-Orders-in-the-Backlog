
import java.util.PriorityQueue;

public class Solution {

    private record Order(int price, int quantity){}

    private static final int BUY_ORDER = 0;
    private static final int SELL_ORDER = 1;
    private static final int MODULO_VALUE = (int) Math.pow(10, 9) + 7;

    public int getNumberOfBacklogOrders(int[][] orders) {
        PriorityQueue<Order> maxHeapBuyOrdersBacklog = new PriorityQueue<>((first, second) -> comparatorBuyOrder(first, second));
        PriorityQueue<Order> minHeapSellOrdersBacklog = new PriorityQueue<>((first, second) -> comparatorSellOrder(first, second));

        processAllOrders(orders, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);

        return findUnsettledBacklogOrders(maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);
    }

    private static int comparatorBuyOrder(Order first, Order second) {
        return second.price - first.price;
    }

    private static int comparatorSellOrder(Order first, Order second) {
        return first.price - second.price;
    }

    private static void processAllOrders(int[][] orders, PriorityQueue<Order> maxHeapBuyOrdersBacklog, PriorityQueue<Order> minHeapSellOrdersBacklog) {
        for (int[] order : orders) {
            int type = order[2];

            if (type == BUY_ORDER) {
                processBuyOrder(order, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);
                continue;
            }
            if (type == SELL_ORDER) {
                processSellOrder(order, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);
            }
        }
    }

    private static void processBuyOrder(int[] order, PriorityQueue<Order> maxHeapBuyOrdersBacklog, PriorityQueue<Order> minHeapSellOrdersBacklog) {
        int buyPrice = order[0];
        int buyQuantity = order[1];

        while (!minHeapSellOrdersBacklog.isEmpty() && minHeapSellOrdersBacklog.peek().price <= buyPrice && buyQuantity > 0) {
            Order sell = minHeapSellOrdersBacklog.poll();
            int buyQuantityBeforeUpdate = buyQuantity;
            buyQuantity -= Math.min(buyQuantity, sell.quantity);
            if (sell.quantity - buyQuantityBeforeUpdate > 0) {
                minHeapSellOrdersBacklog.add(new Order(sell.price, sell.quantity - buyQuantityBeforeUpdate));
            }
        }
        if (buyQuantity > 0) {
            maxHeapBuyOrdersBacklog.add(new Order(buyPrice, buyQuantity));
        }
    }

    private static void processSellOrder(int[] order, PriorityQueue<Order> maxHeapBuyOrdersBacklog, PriorityQueue<Order> minHeapSellOrdersBacklog) {
        int sellPrice = order[0];
        int sellQuantity = order[1];

        while (!maxHeapBuyOrdersBacklog.isEmpty() && maxHeapBuyOrdersBacklog.peek().price >= sellPrice && sellQuantity > 0) {
            Order buy = maxHeapBuyOrdersBacklog.poll();
            int sellQuantityBeforeUpdate = sellQuantity;
            sellQuantity -= Math.min(sellQuantity, buy.quantity);
            if (buy.quantity - sellQuantityBeforeUpdate > 0) {
                maxHeapBuyOrdersBacklog.add(new Order(buy.price, buy.quantity - sellQuantityBeforeUpdate));
            }
        }        
        if (sellQuantity > 0) {
            minHeapSellOrdersBacklog.add(new Order(sellPrice, sellQuantity));
        }
    }

    private static int findUnsettledBacklogOrders(PriorityQueue<Order> maxHeapBuyOrdersBacklog, PriorityQueue<Order> minHeapSellOrdersBacklog) {
        long unsettledBacklogOrders = 0;
        while (!maxHeapBuyOrdersBacklog.isEmpty()) {
            Order buy = maxHeapBuyOrdersBacklog.poll();
            unsettledBacklogOrders = (unsettledBacklogOrders + buy.quantity) % MODULO_VALUE;
        }

        while (!minHeapSellOrdersBacklog.isEmpty()) {
            Order sell = minHeapSellOrdersBacklog.poll();
            unsettledBacklogOrders = (unsettledBacklogOrders + sell.quantity) % MODULO_VALUE;
        }

        return (int) unsettledBacklogOrders;
    }
}
