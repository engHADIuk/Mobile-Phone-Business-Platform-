package velmora.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import velmora.db.CartDao;

public class Cart {

    private static final Cart INSTANCE = new Cart();

    private final CartDao dao = new CartDao();
    private final ObservableList<CartItem> items = FXCollections.observableArrayList();

    private Cart() {
        items.setAll(dao.findAll());
    }

    public static Cart getInstance() {
        return INSTANCE;
    }

    public ObservableList<CartItem> getItems() {
        return items;
    }

    public void addItem(Product product, int qty) {

        for (int i = 0; i < items.size(); i++) {
            CartItem existing = items.get(i);
            if (existing.getProduct().getColor().equals(product.getColor())
                    && existing.getProduct().getStorage().equals(product.getStorage())) {
                items.set(i, new CartItem(existing.getProduct(), existing.getQty() + qty));
                dao.replaceAll(items);
                return;
            }
        }
        items.add(new CartItem(product, qty));
        dao.replaceAll(items);
    }

    public void removeItem(CartItem item) {
        items.remove(item);
        dao.replaceAll(items);
    }

    public void updateQty(CartItem item, int newQty) {
        item.setQty(newQty);
        dao.replaceAll(items);
    }

    public int getItemCount() {
        int count = 0;
        for (CartItem item : items) count += item.getQty();
        return count;
    }

    public double getSubtotal() {
        double sum = 0;
        for (CartItem item : items) sum += item.getLineTotal();
        return sum;
    }

    public double getShipping() {
        return getSubtotal() > 500 || items.isEmpty() ? 0 : 15;
    }

    public double getTotal() {
        return getSubtotal() + getShipping();
    }

    public void clear() {
        items.clear();
        dao.replaceAll(items);
    }
}
