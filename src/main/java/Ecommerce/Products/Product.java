package Ecommerce.Products;

public class Product {

    private String id;
    private String name;
    private String description;
    private double price;
    private int stock;
    private String imageUrl;
    private String status;
    private String categories; // comma-separated, e.g. "Compact, Gaming"

    public Product(String id, String name, String description, double price,
                    int stock, String imageUrl, String status, String categories) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.imageUrl = imageUrl;
        this.status = status;
        this.categories = categories == null ? "" : categories;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCategories() { return categories; }
    public void setCategories(String categories) { this.categories = categories; }

    public String getPriceFormatted() { return String.format("£%.2f", price); }

    public boolean isOutOfStock() { return stock <= 0; }
}