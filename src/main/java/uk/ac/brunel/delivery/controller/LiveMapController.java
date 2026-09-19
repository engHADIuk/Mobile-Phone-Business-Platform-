package uk.ac.brunel.delivery.controller;

import javafx.fxml.FXML;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import uk.ac.brunel.delivery.model.Delivery;
import uk.ac.brunel.delivery.model.Driver;
import uk.ac.brunel.delivery.service.AppServices;
import uk.ac.brunel.delivery.service.DeliveryEventBus;
import uk.ac.brunel.delivery.service.SessionContext;
import uk.ac.brunel.delivery.util.SceneManager;
import uk.ac.brunel.delivery.util.Theme;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class LiveMapController implements LifecycleController {
    @FXML private Label activeCountLabel;
    @FXML private Label driverCountLabel;
    @FXML private Label userLabel;
    @FXML private Pane routePane;
    @FXML private Button homeButton;
    @FXML private Button deliveriesButton;
    @FXML private Button logoutButton;

    private DeliveryEventBus.Subscription subscription;

    @FXML
    private void initialize() {
        Theme.stylePrimary(homeButton);
        Theme.styleSecondary(deliveriesButton);
        Theme.styleSecondary(logoutButton);
        userLabel.setText(SessionContext.requireUser().displayName());
        routePane.widthProperty().addListener((observable, oldValue, newValue) -> drawMap());
        routePane.heightProperty().addListener((observable, oldValue, newValue) -> drawMap());
    }

    @Override
    public void onShow() {
        drawMap();
        subscription = AppServices.events().subscribe(this::drawMap);
    }

    @Override
    public void onHide() {
        if (subscription != null) {
            subscription.close();
            subscription = null;
        }
    }

    @FXML
    private void drawMap() {
        double width = Math.max(routePane.getWidth(), 760);
        double height = Math.max(routePane.getHeight(), 500);
        routePane.getChildren().clear();
        drawGrid(width, height);

        List<Delivery> deliveries = AppServices.deliveries().findAll("", null).stream()
                .filter(delivery -> delivery.assignedDriverId() != null && !delivery.status().isTerminal())
                .toList();
        Map<String, Delivery> byDriver = deliveries.stream().collect(Collectors.toMap(
                Delivery::assignedDriverId, Function.identity(), (first, second) ->
                        first.status().isActive() ? first : second
        ));

        List<Driver> drivers = AppServices.deliveries().findDrivers();
        activeCountLabel.setText(Long.toString(deliveries.stream().filter(d -> d.status().isActive()).count()));
        driverCountLabel.setText(Integer.toString(drivers.size()));
        for (Driver driver : drivers) {
            drawDriver(driver, byDriver.get(driver.driverId()), width, height);
        }
    }

    private void drawGrid(double width, double height) {
        for (int x = 50; x < width; x += 70) {
            Line line = new Line(x, 0, x, height);
            line.setStroke(Color.web("#171919"));
            routePane.getChildren().add(line);
        }
        for (int y = 45; y < height; y += 65) {
            Line line = new Line(0, y, width, y);
            line.setStroke(Color.web("#171919"));
            routePane.getChildren().add(line);
        }
    }

    private void drawDriver(Driver driver, Delivery delivery, double width, double height) {
        double startX = 90 + driver.latitude() * Math.max(300, width - 300);
        double startY = 60 + driver.longitude() * Math.max(250, height - 180);
        double progress = delivery == null ? 0 : delivery.progress() / 100.0;
        double endX = Math.min(width - 80, startX + 150);
        double endY = Math.min(height - 70, startY + 55);
        double x = startX + (endX - startX) * progress;
        double y = startY + (endY - startY) * progress;

        Line route = new Line(startX, startY, endX, endY);
        route.setStroke(Color.web(delivery == null ? "#343636" : Theme.statusColor(delivery.status())));
        route.setStrokeWidth(3);
        route.getStrokeDashArray().addAll(9.0, 7.0);
        Circle destination = new Circle(endX, endY, 6, Color.web("#D5C38B"));
        Circle marker = new Circle(x, y, 13,
                Color.web(delivery == null ? "#505352" : Theme.statusColor(delivery.status())));

        Text initials = new Text(x, y + 3, initials(driver.fullName()));
        initials.setFill(Color.web("#080909"));
        initials.setFont(Font.font(9));
        initials.setTextAlignment(TextAlignment.CENTER);
        initials.setTextOrigin(VPos.CENTER);
        Text label = new Text(x + 18, y - 8,
                driver.fullName() + "\n" + (delivery == null ? "No scheduled delivery" : delivery.deliveryId() + " - " + delivery.status()));
        label.setFill(Color.web(Theme.TEXT));
        label.setFont(Font.font(11));
        routePane.getChildren().addAll(route, destination, marker, initials, label);
    }

    private String initials(String name) {
        String[] parts = name.split("\\s+");
        return parts.length < 2 ? parts[0].substring(0, 1) : parts[0].substring(0, 1) + parts[1].substring(0, 1);
    }

    @FXML private void openHome() { SceneManager.getInstance().showAdminDashboard(); }
    @FXML private void openDeliveries() { SceneManager.getInstance().showDeliveries(); }

    @FXML
    private void logout() {
        SessionContext.logout();
        SceneManager.getInstance().showLogin();
    }
}
