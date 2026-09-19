package com.nc1605.inventorywarehouse.model;
public class TransferRecord {
    private final String product, source, destination, timestamp; private final int quantity;
    public TransferRecord(String product,String source,String destination,int quantity,String timestamp){this.product=product;this.source=source;this.destination=destination;this.quantity=quantity;this.timestamp=timestamp;}
    public String getProduct(){return product;} public String getSource(){return source;} public String getDestination(){return destination;}
    public int getQuantity(){return quantity;} public String getTimestamp(){return timestamp;}
}
