package com.nc1605.inventorywarehouse.data;

import com.nc1605.inventorywarehouse.model.InventoryItem;
import com.nc1605.inventorywarehouse.model.ProductStock;
import com.nc1605.inventorywarehouse.model.TransferRecord;
import com.nc1605.inventorywarehouse.model.Warehouse;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/** SQLite data access. The database file is created automatically beside the project. */
public final class Database {
    private static final String URL = "jdbc:sqlite:inventory_warehouse.db";
    private Database() { }
    private static Connection connect() throws SQLException { return DriverManager.getConnection(URL); }

    public static void initialise() {
        try (Connection c=connect(); Statement s=c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE, sku TEXT NOT NULL UNIQUE)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS warehouses (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE, location TEXT NOT NULL, capacity INTEGER NOT NULL CHECK(capacity > 0))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS inventory (product_id INTEGER, warehouse_id INTEGER, quantity INTEGER NOT NULL CHECK(quantity >= 0), threshold INTEGER NOT NULL CHECK(threshold >= 0), PRIMARY KEY(product_id, warehouse_id), FOREIGN KEY(product_id) REFERENCES products(id), FOREIGN KEY(warehouse_id) REFERENCES warehouses(id))");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS transfers (id INTEGER PRIMARY KEY AUTOINCREMENT, product_id INTEGER NOT NULL, source_id INTEGER NOT NULL, destination_id INTEGER NOT NULL, quantity INTEGER NOT NULL CHECK(quantity > 0), created_at TEXT NOT NULL)");
            try (ResultSet r=s.executeQuery("SELECT COUNT(*) FROM products")) { if (r.next() && r.getInt(1)==0) seed(c); }
        } catch (SQLException e) { throw new IllegalStateException("Could not create the database: " + e.getMessage(), e); }
    }
    private static void seed(Connection c) throws SQLException {
        try (Statement s=c.createStatement()) {
            s.executeUpdate("INSERT INTO products VALUES (1,'VELMORA Compact','VEL-COM'),(2,'VELMORA Gaming','VEL-GAM'),(3,'VELMORA Flip','VEL-FLP'),(4,'VELMORA Fold','VEL-FLD')");
            s.executeUpdate("INSERT INTO warehouses VALUES (1,'London Central','London',500),(2,'Birmingham Hub','Birmingham',400)");
            s.executeUpdate("INSERT INTO inventory VALUES (1,1,7,16),(2,1,14,20),(3,1,28,15),(4,1,9,14),(1,2,36,16),(2,2,8,20),(3,2,12,15),(4,2,22,14)");
        }
    }
    public static List<InventoryItem> inventory(String search, String status) {
        List<InventoryItem> result=new ArrayList<>();
        String sql="SELECT p.id,p.name,w.id,w.name,i.quantity,i.threshold FROM inventory i JOIN products p ON p.id=i.product_id JOIN warehouses w ON w.id=i.warehouse_id WHERE p.name LIKE ? ORDER BY p.name,w.name";
        try(Connection c=connect(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setString(1,"%"+search+"%"); ResultSet r=ps.executeQuery();
            while(r.next()) { InventoryItem item=new InventoryItem(r.getInt(1),r.getString(2),r.getInt(3),r.getString(4),r.getInt(5),r.getInt(6)); if("All".equals(status)||item.getStatus().equals(status)) result.add(item); }
        } catch(SQLException e) { throw new IllegalStateException(e); } return result;
    }
    public static List<Warehouse> warehouses() {
        List<Warehouse> result=new ArrayList<>(); String sql="SELECT w.id,w.name,w.location,w.capacity,COALESCE(SUM(i.quantity),0) FROM warehouses w LEFT JOIN inventory i ON w.id=i.warehouse_id GROUP BY w.id ORDER BY w.name";
        try(Connection c=connect(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) { while(r.next()) result.add(new Warehouse(r.getInt(1),r.getString(2),r.getString(3),r.getInt(4),r.getInt(5))); } catch(SQLException e){throw new IllegalStateException(e);} return result;
    }
    public static List<ProductStock> products() {
        List<ProductStock> result=new ArrayList<>();
        String sql="SELECT p.id,p.name,COALESCE(SUM(i.quantity),0) FROM products p LEFT JOIN inventory i ON p.id=i.product_id GROUP BY p.id,p.name ORDER BY CASE p.name WHEN 'VELMORA Compact' THEN 1 WHEN 'VELMORA Fold' THEN 2 WHEN 'VELMORA Gaming' THEN 3 WHEN 'VELMORA Flip' THEN 4 ELSE 5 END";
        try(Connection c=connect(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) {
            while(r.next()) result.add(new ProductStock(r.getInt(1),r.getString(2),r.getInt(3)));
        } catch(SQLException e) { throw new IllegalStateException("Could not load products.",e); }
        return result;
    }
    public static void addWarehouse(String name, String location, int capacity) {
        String sql="INSERT INTO warehouses(name,location,capacity) VALUES(?,?,?)";
        try(Connection c=connect(); PreparedStatement p=c.prepareStatement(sql)) {
            p.setString(1,name); p.setString(2,location); p.setInt(3,capacity); p.executeUpdate();
        } catch(SQLException e) {
            if(e.getMessage().toLowerCase().contains("unique")) throw new IllegalArgumentException("A warehouse with this name already exists.");
            throw new IllegalStateException("Could not add the warehouse.",e);
        }
    }
    public static void updateWarehouse(int id, String name, String location, int capacity) {
        int used=0;
        try(Connection c=connect()) {
            try(PreparedStatement check=c.prepareStatement("SELECT COALESCE(SUM(quantity),0) FROM inventory WHERE warehouse_id=?")) {
                check.setInt(1,id); try(ResultSet r=check.executeQuery()){if(r.next()) used=r.getInt(1);}
            }
            if(capacity<used) throw new IllegalArgumentException("Capacity cannot be lower than the "+used+" units currently stored here.");
            try(PreparedStatement p=c.prepareStatement("UPDATE warehouses SET name=?,location=?,capacity=? WHERE id=?")) {
                p.setString(1,name); p.setString(2,location); p.setInt(3,capacity); p.setInt(4,id); p.executeUpdate();
            }
        } catch(IllegalArgumentException e) { throw e;
        } catch(SQLException e) {
            if(e.getMessage().toLowerCase().contains("unique")) throw new IllegalArgumentException("A warehouse with this name already exists.");
            throw new IllegalStateException("Could not update the warehouse.",e);
        }
    }
    public static List<TransferRecord> transfers() {
        List<TransferRecord> result=new ArrayList<>(); String sql="SELECT p.name,src.name,dst.name,t.quantity,t.created_at FROM transfers t JOIN products p ON p.id=t.product_id JOIN warehouses src ON src.id=t.source_id JOIN warehouses dst ON dst.id=t.destination_id ORDER BY t.id DESC";
        try(Connection c=connect(); Statement s=c.createStatement(); ResultSet r=s.executeQuery(sql)) { while(r.next()){String raw=r.getString(5); String display=LocalDateTime.parse(raw).format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));result.add(new TransferRecord(r.getString(1),r.getString(2),r.getString(3),r.getInt(4),display));} }catch(SQLException e){throw new IllegalStateException(e);}return result;
    }
    public static void transfer(InventoryItem item, Warehouse destination, int quantity) {
        if (item == null || destination == null) throw new IllegalArgumentException("Choose an inventory item and destination.");
        if (item.getWarehouseId()==destination.getId()) throw new IllegalArgumentException("Choose a different destination warehouse.");
        if (quantity<=0) throw new IllegalArgumentException("Transfer quantity must be a whole number greater than zero.");
        if (quantity>item.getQuantity()) throw new IllegalArgumentException("Only "+item.getQuantity()+" unit(s) are available at the source warehouse.");
        if (destination.getUsed() + quantity > destination.getCapacity()) throw new IllegalArgumentException("This transfer would exceed the destination warehouse capacity.");
        try(Connection c=connect()) { c.setAutoCommit(false); try {
            try(PreparedStatement p=c.prepareStatement("UPDATE inventory SET quantity=quantity-? WHERE product_id=? AND warehouse_id=?")){p.setInt(1,quantity);p.setInt(2,item.getProductId());p.setInt(3,item.getWarehouseId());p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO inventory(product_id,warehouse_id,quantity,threshold) VALUES(?,?,?,?) ON CONFLICT(product_id,warehouse_id) DO UPDATE SET quantity=quantity+excluded.quantity")){p.setInt(1,item.getProductId());p.setInt(2,destination.getId());p.setInt(3,quantity);p.setInt(4,item.getThreshold());p.executeUpdate();}
            try(PreparedStatement p=c.prepareStatement("INSERT INTO transfers(product_id,source_id,destination_id,quantity,created_at) VALUES(?,?,?,?,?)")){p.setInt(1,item.getProductId());p.setInt(2,item.getWarehouseId());p.setInt(3,destination.getId());p.setInt(4,quantity);p.setString(5,LocalDateTime.now().toString());p.executeUpdate();}
            c.commit();
        } catch(SQLException e){c.rollback();throw e;} }catch(SQLException e){throw new IllegalStateException(e);}
    }
}
