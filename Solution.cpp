
#include <queue>
#include <vector>
#include <algorithm>
using namespace std;

class Solution {

    struct Order {
        int price{};
        int quantity{};
        Order(int price, int quantity): price{ price }, quantity{ quantity } {}
    };

    struct ComparatorBuyOrder {
        bool operator()(const Order& first, const Order& second) const {
            return second.price > first.price;
        }
    };

    struct ComparatorSellOrder {
        bool operator()(const Order& first, const Order& second) const {
            return first.price > second.price;
        }
    };

    using MaxHeapBuy = priority_queue<Order, vector<Order>, ComparatorBuyOrder>;
    using MinHeapSell = priority_queue<Order, vector<Order>, ComparatorSellOrder>;

    static const int BUY_ORDER = 0;
    static const int SELL_ORDER = 1;
    inline static int MODULO_VALUE = pow(10, 9) + 7;

public:
    int getNumberOfBacklogOrders(vector<vector<int>>& orders) {
        MaxHeapBuy maxHeapBuyOrdersBacklog;
        MinHeapSell minHeapSellOrdersBacklog;

        processAllOrders(orders, maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);

        return findUnsettledBacklogOrders(maxHeapBuyOrdersBacklog, minHeapSellOrdersBacklog);
    }

private:
    static void processAllOrders(span<const vector<int>> orders, MaxHeapBuy& maxHeapBuyOrdersBacklog, MinHeapSell& minHeapSellOrdersBacklog) {
        for (const auto& order : orders) {
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

    static void processBuyOrder(const vector<int>& order, MaxHeapBuy& maxHeapBuyOrdersBacklog, MinHeapSell& minHeapSellOrdersBacklog) {
        int buyPrice = order[0];
        int buyQuantity = order[1];

        while (!minHeapSellOrdersBacklog.empty() && minHeapSellOrdersBacklog.top().price <= buyPrice && buyQuantity > 0) {
            Order sell = minHeapSellOrdersBacklog.top();
            minHeapSellOrdersBacklog.pop();

            int buyQuantityBeforeUpdate = buyQuantity;
            buyQuantity -= min(buyQuantity, sell.quantity);
            if (sell.quantity - buyQuantityBeforeUpdate > 0) {
                minHeapSellOrdersBacklog.emplace(sell.price, sell.quantity - buyQuantityBeforeUpdate);
            }
        }
        if (buyQuantity > 0) {
            maxHeapBuyOrdersBacklog.emplace(buyPrice, buyQuantity);
        }
    }

    static void processSellOrder(const vector<int>& order, MaxHeapBuy& maxHeapBuyOrdersBacklog, MinHeapSell& minHeapSellOrdersBacklog) {
        int sellPrice = order[0];
        int sellQuantity = order[1];

        while (!maxHeapBuyOrdersBacklog.empty() && maxHeapBuyOrdersBacklog.top().price >= sellPrice && sellQuantity > 0) {
            Order buy = maxHeapBuyOrdersBacklog.top();
            maxHeapBuyOrdersBacklog.pop();

            int sellQuantityBeforeUpdate = sellQuantity;
            sellQuantity -= min(sellQuantity, buy.quantity);
            if (buy.quantity - sellQuantityBeforeUpdate > 0) {
                maxHeapBuyOrdersBacklog.emplace(buy.price, buy.quantity - sellQuantityBeforeUpdate);
            }
        }
        if (sellQuantity > 0) {
            minHeapSellOrdersBacklog.emplace(sellPrice, sellQuantity);
        }
    }

    static int findUnsettledBacklogOrders(MaxHeapBuy& maxHeapBuyOrdersBacklog, MinHeapSell& minHeapSellOrdersBacklog) {
        long long unsettledBacklogOrders = 0;
        while (!maxHeapBuyOrdersBacklog.empty()) {
            Order buy = maxHeapBuyOrdersBacklog.top();
            maxHeapBuyOrdersBacklog.pop();
            unsettledBacklogOrders = (unsettledBacklogOrders + buy.quantity) % MODULO_VALUE;
        }

        while (!minHeapSellOrdersBacklog.empty()) {
            Order sell = minHeapSellOrdersBacklog.top();
            minHeapSellOrdersBacklog.pop();
            unsettledBacklogOrders = (unsettledBacklogOrders + sell.quantity) % MODULO_VALUE;
        }

        return unsettledBacklogOrders;
    }
};
