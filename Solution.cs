
using System;
using System.Collections.Generic;

public class Solution
{
    private record Order(int Price, int Quantity) { }

    private static readonly int BUY_ORDER = 0;
    private static readonly int SELL_ORDER = 1;
    private static readonly int MODULO_VALUE = (int)Math.Pow(10, 9) + 7;

    private readonly Comparer<int> comparatorBuyOrder = Comparer<int>.Create((firstPrice, secondPrice) => secondPrice - firstPrice);
    private readonly Comparer<int> comparatorSellOrder = Comparer<int>.Create((firstPrice, secondPrice) => firstPrice - secondPrice);

    public int GetNumberOfBacklogOrders(int[][] orders)
    {
        PriorityQueue<Order, int> maxHeapBuyOrdersBacklog = new(comparatorBuyOrder);
        PriorityQueue<Order, int> minHeapSellOrdersBacklog = new(comparatorSellOrder);

        ProcessAllOrders(orders, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);

        return FindUnsettledBacklogOrders(maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);
    }

    private static void ProcessAllOrders(int[][] orders, PriorityQueue<Order, int> maxHeapBuyOrdersBacklog, PriorityQueue<Order, int> minHeapSellOrdersBacklog)
    {
        foreach (int[] order in orders)
        {
            int type = order[2];

            if (type == BUY_ORDER)
            {
                ProcessBuyOrder(order, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);
                continue;
            }
            if (type == SELL_ORDER)
            {
                ProcessSellOrder(order, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);
            }
        }
    }

    private static void ProcessBuyOrder(int[] order, PriorityQueue<Order, int> maxHeapBuyOrdersBacklog, PriorityQueue<Order, int> minHeapSellOrdersBacklog)
    {
        int buyPrice = order[0];
        int buyQuantity = order[1];

        while (minHeapSellOrdersBacklog.Count > 0 && minHeapSellOrdersBacklog.Peek().Price <= buyPrice && buyQuantity > 0)
        {
            Order sell = minHeapSellOrdersBacklog.Dequeue();
            int buyQuantityBeforeUpdate = buyQuantity;
            buyQuantity -= Math.Min(buyQuantity, sell.Quantity);
            if (sell.Quantity - buyQuantityBeforeUpdate > 0)
            {
                minHeapSellOrdersBacklog.Enqueue(new Order(sell.Price, sell.Quantity - buyQuantityBeforeUpdate), sell.Price);
            }
        }
        if (buyQuantity > 0)
        {
            maxHeapBuyOrdersBacklog.Enqueue(new Order(buyPrice, buyQuantity), buyPrice);
        }
    }

    private static void ProcessSellOrder(int[] order, PriorityQueue<Order, int> maxHeapBuyOrdersBacklog, PriorityQueue<Order, int> minHeapSellOrdersBacklog)
    {
        int sellPrice = order[0];
        int sellQuantity = order[1];

        while (maxHeapBuyOrdersBacklog.Count > 0 && maxHeapBuyOrdersBacklog.Peek().Price >= sellPrice && sellQuantity > 0)
        {
            Order buy = maxHeapBuyOrdersBacklog.Dequeue();
            int sellQuantityBeforeUpdate = sellQuantity;
            sellQuantity -= Math.Min(sellQuantity, buy.Quantity);
            if (buy.Quantity - sellQuantityBeforeUpdate > 0)
            {
                maxHeapBuyOrdersBacklog.Enqueue(new Order(buy.Price, buy.Quantity - sellQuantityBeforeUpdate), buy.Price);
            }
        }
        if (sellQuantity > 0)
        {
            minHeapSellOrdersBacklog.Enqueue(new Order(sellPrice, sellQuantity), sellPrice);
        }
    }

    private static int FindUnsettledBacklogOrders(PriorityQueue<Order, int> maxHeapBuyOrdersBacklog, PriorityQueue<Order, int> minHeapSellOrdersBacklog)
    {
        long unsettledBacklogOrders = 0;
        while (maxHeapBuyOrdersBacklog.Count > 0)
        {
            Order buy = maxHeapBuyOrdersBacklog.Dequeue();
            unsettledBacklogOrders = (unsettledBacklogOrders + buy.Quantity) % MODULO_VALUE;
        }

        while (minHeapSellOrdersBacklog.Count > 0)
        {
            Order sell = minHeapSellOrdersBacklog.Dequeue();
            unsettledBacklogOrders = (unsettledBacklogOrders + sell.Quantity) % MODULO_VALUE;
        }

        return (int)unsettledBacklogOrders;
    }
}
