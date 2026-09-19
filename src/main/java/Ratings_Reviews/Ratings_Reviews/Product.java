package Ratings_Reviews.Ratings_Reviews;

public class Product {
    private final int id;
    private final String productCode;
    private final String name;
    private final String description;

    public Product(int id, String productCode, String name, String description) {
        this.id = id;
        this.productCode = productCode;
        this.name = name;
        this.description = description;
    }

    public int getId() { return id; }
    public String getProductCode() { return productCode; }
    public String getName() { return name; }
    public String getDescription() { return description; }

    @Override
    public String toString() {
        return productCode + " — " + name;
    }
}
