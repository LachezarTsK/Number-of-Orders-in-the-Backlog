
// const {PriorityQueue} = require('@datastructures-js/priority-queue');
/*
 PriorityQueue is internally included in the solution file on leetcode.
 When running the code on leetcode it should stay commented out. 
 It is mentioned here just for information about the external library 
 that is applied for this data structure.
 */

function getNumberOfBacklogOrders(orders: number[][]): number {
    const maxHeapBuyOrdersBacklog = new PriorityQueue<Order>((first, second) => comparatorBuyOrder(first, second));
    const minHeapSellOrdersBacklog = new PriorityQueue<Order>((first, second) => comparatorSellOrder(first, second));

    processAllOrders(orders, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);

    return findUnsettledBacklogOrders(maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);
};

function comparatorBuyOrder(first: Order, second: Order): number {
    return second.price - first.price;
}

function comparatorSellOrder(first: Order, second: Order): number {
    return first.price - second.price;
}

function processAllOrders(orders: number[][], maxHeapBuyOrdersBacklog: PriorityQueue<Order>, minHeapSellOrdersBacklog: PriorityQueue<Order>): void {
    for (let order of orders) {
        const type = order[2];
        if (type === Util.BUY_ORDER) {
            processBuyOrder(order, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);
            continue;
        }
        if (type === Util.SELL_ORDER) {
            processSellOrder(order, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);
        }
    }
}

function processBuyOrder(order: number[], maxHeapBuyOrdersBacklog: PriorityQueue<Order>, minHeapSellOrdersBacklog: PriorityQueue<Order>): void {
    const buyPrice = order[0];
    let buyQuantity = order[1];

    while (!minHeapSellOrdersBacklog.isEmpty() && minHeapSellOrdersBacklog.front().price <= buyPrice && buyQuantity > 0) {
        const sell = minHeapSellOrdersBacklog.dequeue();
        const buyQuantityBeforeUpdate = buyQuantity;
        buyQuantity -= Math.min(buyQuantity, sell.quantity);
        if (sell.quantity - buyQuantityBeforeUpdate > 0) {
            minHeapSellOrdersBacklog.enqueue(new Order(sell.price, sell.quantity - buyQuantityBeforeUpdate));
        }
    }
    if (buyQuantity > 0) {
        maxHeapBuyOrdersBacklog.enqueue(new Order(buyPrice, buyQuantity));
    }
}

function processSellOrder(order: number[], maxHeapBuyOrdersBacklog: PriorityQueue<Order>, minHeapSellOrdersBacklog: PriorityQueue<Order>): void {
    const sellPrice = order[0];
    let sellQuantity = order[1];

    while (!maxHeapBuyOrdersBacklog.isEmpty() && maxHeapBuyOrdersBacklog.front().price >= sellPrice && sellQuantity > 0) {
        const buy = maxHeapBuyOrdersBacklog.dequeue();
        const sellQuantityBeforeUpdate = sellQuantity;
        sellQuantity -= Math.min(sellQuantity, buy.quantity);
        if (buy.quantity - sellQuantityBeforeUpdate > 0) {
            maxHeapBuyOrdersBacklog.enqueue(new Order(buy.price, buy.quantity - sellQuantityBeforeUpdate));
        }
    }
    if (sellQuantity > 0) {
        minHeapSellOrdersBacklog.enqueue(new Order(sellPrice, sellQuantity));
    }
}

function findUnsettledBacklogOrders(maxHeapBuyOrdersBacklog: PriorityQueue<Order>, minHeapSellOrdersBacklog: PriorityQueue<Order>): number {
    let unsettledBacklogOrders = 0;
    while (!maxHeapBuyOrdersBacklog.isEmpty()) {
        const buy = maxHeapBuyOrdersBacklog.dequeue();
        unsettledBacklogOrders = (unsettledBacklogOrders + buy.quantity) % Util.MODULO_VALUE;
    }

    while (!minHeapSellOrdersBacklog.isEmpty()) {
        const sell = minHeapSellOrdersBacklog.dequeue();
        unsettledBacklogOrders = (unsettledBacklogOrders + sell.quantity) % Util.MODULO_VALUE;
    }

    return unsettledBacklogOrders;
}

class Order {
    constructor(public price: number, public quantity: number) { }
}

class Util {
    static BUY_ORDER = 0;
    static SELL_ORDER = 1;
    static MODULO_VALUE = Math.pow(10, 9) + 7;
}
