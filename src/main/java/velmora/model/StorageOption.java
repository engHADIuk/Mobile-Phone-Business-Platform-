package velmora.model;

public class StorageOption {

    private final String storage;
    private final double price;
    private final double oldPrice;
    private final int stock;

    public StorageOption(String storage, double price, double oldPrice, int stock) {
        this.storage = storage;
        this.price = price;
        this.oldPrice = oldPrice;
        this.stock = stock;
    }

    public String getStorage() { return storage; }
    public double getPrice() { return price; }
    public double getOldPrice() { return oldPrice; }
    public int getStock() { return stock; }
}
