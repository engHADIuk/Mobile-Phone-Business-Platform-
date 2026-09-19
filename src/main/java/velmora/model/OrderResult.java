package velmora.model;

public class OrderResult {

    private final boolean success;
    private final String message;
    private final Order order;

    private OrderResult(boolean success, String message, Order order) {
        this.success = success;
        this.message = message;
        this.order = order;
    }

    public static OrderResult success(Order order) {
        return new OrderResult(true, "", order);
    }

    public static OrderResult failure(String message) {
        return new OrderResult(false, message, null);
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Order getOrder() { return order; }
}
