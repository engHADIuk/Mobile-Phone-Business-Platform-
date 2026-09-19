package velmora.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import velmora.db.Database;
import velmora.db.OrderDao;
import velmora.db.OrderItemDao;
import velmora.db.ProductDao;

public class OrderHistory {

    private static final OrderHistory INSTANCE = new OrderHistory();

    private final OrderDao dao = new OrderDao();
    private final OrderItemDao itemDao = new OrderItemDao();
    private final ProductDao productDao = new ProductDao();
    private final ObservableList<Order> orders = FXCollections.observableArrayList();
    private int nextId = 1007;

    private OrderHistory() {
        Database.initialize();

        if (dao.isEmpty()) {
            seedSampleData();
        }
        orders.setAll(dao.findAll());
        nextId = computeNextId();
    }

    private int computeNextId() {
        int max = 1006;
        for (Order order : orders) {
            String digits = order.getId().replaceAll("[^0-9]", "");
            if (!digits.isEmpty()) {
                max = Math.max(max, Integer.parseInt(digits));
            }
        }
        return max + 1;
    }

    private void seedSampleData() {
        LocalDateTime now = LocalDateTime.now();

        Order o1 = new Order("VLM-1004", "Velmora X1 — Ocean Blue, 128GB x1",
                LocalDate.now().minusDays(65), 799, Order.Status.DELIVERED, "Alex Chen", "alex.chen@example.com",
                now, "12 Baker Street, London", "4242");
        Order o2 = new Order("VLM-1005", "Velmora X1 — Midnight Black, 256GB x1",
                LocalDate.now().minusDays(12), 999, Order.Status.DELIVERED, "Demo Customer", "demo@velmora.com",
                now, "1 Kingston Lane, London", "1881");
        Order o3 = new Order("VLM-1006", "Velmora X1 — Starlight Silver, 512GB x2",
                LocalDate.now().minusDays(3), 2398, Order.Status.SHIPPED, "Demo Customer", "demo@velmora.com",
                now, "1 Kingston Lane, London", "1881");

        dao.insert(o1);
        dao.insert(o2);
        dao.insert(o3);

        itemDao.insertAll("VLM-1004", List.of(new CartItem(new Product("Velmora X1", "Ocean Blue", "128GB", 799, 999, 16), 1)));
        itemDao.insertAll("VLM-1005", List.of(new CartItem(new Product("Velmora X1", "Midnight Black", "256GB", 999, 1199, 9), 1)));
        itemDao.insertAll("VLM-1006", List.of(new CartItem(new Product("Velmora X1", "Starlight Silver", "512GB", 1199, 1399, 3), 2)));
    }

    public static OrderHistory getInstance() {
        return INSTANCE;
    }

    public ObservableList<Order> getOrders() {
        return orders;
    }

    public List<OrderItemDao.Row> getLineItems(String orderId) {
        return itemDao.findByOrderId(orderId);
    }

    public List<Order> findByDateRange(LocalDate from, LocalDate to) {
        return dao.findByDateRange(from, to);
    }

    public String checkStock(Cart cart) {
        for (CartItem item : cart.getItems()) {
            int available = productDao.getStock(item.getProduct().getStorage());
            if (item.getQty() > available) {
                return "Only " + available + " left of " + item.getProduct().getStorage()
                        + " (you asked for " + item.getQty() + ").";
            }
        }
        return "";
    }

    public OrderResult createOrderFromCart(Cart cart, String shippingAddress, String cardLast4) {
        String stockProblem = checkStock(cart);
        if (!stockProblem.isEmpty()) {
            return OrderResult.failure(stockProblem);
        }

        StringBuilder summary = new StringBuilder();
        for (CartItem item : cart.getItems()) {
            if (summary.length() > 0) summary.append("; ");
            summary.append(item.getProduct().toString()).append(" x").append(item.getQty());
        }
        Order order = new Order("VLM-" + (nextId++), summary.toString(),
                LocalDate.now(), cart.getTotal(), Order.Status.PROCESSING,
                Session.getName(), Session.getEmail(), LocalDateTime.now(), shippingAddress, cardLast4);

        dao.insert(order);
        itemDao.insertAll(order.getId(), cart.getItems());

        for (CartItem item : cart.getItems()) {
            productDao.adjustStock(item.getProduct().getStorage(), -item.getQty());
        }

        orders.add(0, order);
        return OrderResult.success(order);
    }

    public void updateStatus(Order order, Order.Status newStatus) {
        order.setStatus(newStatus);
        dao.updateStatus(order.getId(), newStatus);
    }
}
