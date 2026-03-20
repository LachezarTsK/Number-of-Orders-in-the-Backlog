
package main

import (
    "container/heap"
    "math"
)

const BUY_ORDER = 0
const SELL_ORDER = 1

var MODULO_VALUE = int64(math.Pow(10.0, 9.0)) + 7

type Order struct {
    price    int
    quantity int
}

func NewOrder(price int, quantity int) Order {
    order := Order{
        price:    price,
        quantity: quantity,
    }
    return order
}

func getNumberOfBacklogOrders(orders [][]int) int {
    maxHeapBuyOrdersBacklog := NewPriorityQueue(comparatorBuyOrder)
    minHeapSellOrdersBacklog := NewPriorityQueue(comparatorSellOrder)

    processAllOrders(orders, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog)

    return findUnsettledBacklogOrders(maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog)
}

func comparatorBuyOrder(first Order, second Order) bool {
    return second.price < first.price
}

func comparatorSellOrder(first Order, second Order) bool {
    return first.price < second.price
}

func processAllOrders(orders [][]int, maxHeapBuyOrdersBacklog *PriorityQueue, minHeapSellOrdersBacklog *PriorityQueue) {
    for _, order := range orders {
        orderType := order[2]

        if orderType == BUY_ORDER {
            processBuyOrder(order, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog)
            continue
        }
        if orderType == SELL_ORDER {
            processSellOrder(order, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog)
        }
    }
}

func processBuyOrder(order []int, maxHeapBuyOrdersBacklog *PriorityQueue, minHeapSellOrdersBacklog *PriorityQueue) {
    buyPrice := order[0]
    buyQuantity := order[1]

    for !minHeapSellOrdersBacklog.IsEmpty() && minHeapSellOrdersBacklog.Peek().(Order).price <= buyPrice && buyQuantity > 0 {
        sell := heap.Pop(minHeapSellOrdersBacklog).(Order)
        buyQuantityBeforeUpdate := buyQuantity
        buyQuantity -= min(buyQuantity, sell.quantity)
        if sell.quantity - buyQuantityBeforeUpdate > 0 {
            heap.Push(minHeapSellOrdersBacklog, NewOrder(sell.price, sell.quantity - buyQuantityBeforeUpdate))
        }
    }
    if buyQuantity > 0 {
        heap.Push(maxHeapBuyOrdersBacklog, NewOrder(buyPrice, buyQuantity))
    }
}

func processSellOrder(order []int, maxHeapBuyOrdersBacklog *PriorityQueue, minHeapSellOrdersBacklog *PriorityQueue) {
    sellPrice := order[0]
    sellQuantity := order[1]

    for !maxHeapBuyOrdersBacklog.IsEmpty() && maxHeapBuyOrdersBacklog.Peek().(Order).price >= sellPrice && sellQuantity > 0 {
        buy := heap.Pop(maxHeapBuyOrdersBacklog).(Order)
        sellQuantityBeforeUpdate := sellQuantity
        sellQuantity -= min(sellQuantity, buy.quantity)
        if buy.quantity - sellQuantityBeforeUpdate > 0 {
            heap.Push(maxHeapBuyOrdersBacklog, NewOrder(buy.price, buy.quantity - sellQuantityBeforeUpdate))
        }
    }
    if sellQuantity > 0 {
        heap.Push(minHeapSellOrdersBacklog, NewOrder(sellPrice, sellQuantity))
    }
}

func findUnsettledBacklogOrders(maxHeapBuyOrdersBacklog *PriorityQueue, minHeapSellOrdersBacklog *PriorityQueue) int {
    var unsettledBacklogOrders int64 = 0
    for !maxHeapBuyOrdersBacklog.IsEmpty() {
        buy := heap.Pop(maxHeapBuyOrdersBacklog).(Order)
        unsettledBacklogOrders = (unsettledBacklogOrders + int64(buy.quantity)) % MODULO_VALUE
    }

    for !minHeapSellOrdersBacklog.IsEmpty() {
        sell := heap.Pop(minHeapSellOrdersBacklog).(Order)
        unsettledBacklogOrders = (unsettledBacklogOrders + int64(sell.quantity)) % MODULO_VALUE
    }

    return int(unsettledBacklogOrders)
}

type PriorityQueue struct {
    container  []Order
    comparator func(Order, Order) bool
}

func NewPriorityQueue(comparator func(Order, Order) bool) *PriorityQueue {
    priorityQueue := &PriorityQueue{
        container:  []Order{},
        comparator: comparator,
    }
    return priorityQueue
}

func (pq PriorityQueue) Len() int {
    return len(pq.container)
}

func (pq PriorityQueue) Less(first int, second int) bool {
    return pq.comparator(pq.container[first], pq.container[second])
}

func (pq PriorityQueue) Swap(first int, second int) {
    pq.container[first], pq.container[second] = pq.container[second], pq.container[first]
}

func (pq *PriorityQueue) Push(object any) {
    order := object.(Order)
    pq.container = append(pq.container, order)
}

func (pq *PriorityQueue) Pop() any {
    order := (*pq).container[pq.Len() - 1]
    pq.container = pq.container[0 : pq.Len() - 1]
    return order
}

func (pq PriorityQueue) IsEmpty() bool {
    return pq.Len() == 0
}

func (pq PriorityQueue) Peek() any {
    return pq.container[0]
}
