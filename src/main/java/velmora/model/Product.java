package velmora.model;

public class Product {

    private String name;
    private String color;
    private String storage;
    private double price;
    private double oldPrice;
    private int stock;

    public Product(String name, String color, String storage, double price, double oldPrice, int stock) {
        this.name = name;
        this.color = color;
        this.storage = storage;
        this.price = price;
        this.oldPrice = oldPrice;
        this.stock = stock;
    }

    public String getName() { return name; }
    public String getColor() { return color; }
    public String getStorage() { return storage; }
    public double getPrice() { return price; }
    public double getOldPrice() { return oldPrice; }
    public int getStock() { return stock; }

    public void setColor(String color) { this.color = color; }
    public void setStorage(String storage) { this.storage = storage; }
    public void setPrice(double price) { this.price = price; }
    public void setOldPrice(double oldPrice) { this.oldPrice = oldPrice; }
    public void setStock(int stock) { this.stock = stock; }

    @Override
    public String toString() {
        return name + " — " + color + ", " + storage;
    }
}
