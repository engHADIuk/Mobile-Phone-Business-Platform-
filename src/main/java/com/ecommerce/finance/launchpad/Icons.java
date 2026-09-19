package com.ecommerce.finance.launchpad;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;

/**
 * Small flat glyphs for each component tile on the launchpad, built entirely
 * from primitive shapes rather than imported artwork, so there is nothing
 * external that can go missing.
 */
final class Icons {

    private Icons() {
    }

    static Group forComponent(String id, Color glyphColor) {
        return switch (id) {
            case "finance" -> finance(glyphColor);
            case "reviews" -> reviews(glyphColor);
            case "orders" -> orders(glyphColor);
            case "products" -> products(glyphColor);
            case "delivery" -> delivery(glyphColor);
            case "customers" -> customers(glyphColor);
            case "warehouse" -> warehouse(glyphColor);
            default -> new Group();
        };
    }

    private static Group finance(Color c) {
        return new Group(bar(-15, 6, 8, 12, c), bar(-3, -2, 8, 20, c), bar(9, -12, 8, 30, c));
    }

    private static Rectangle bar(double x, double y, double w, double h, Color c) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setArcWidth(3);
        r.setArcHeight(3);
        r.setFill(c);
        return r;
    }

    private static Group reviews(Color c) {
        Polygon star = new Polygon();
        double outerR = 17, innerR = 7;
        for (int i = 0; i < 10; i++) {
            double angle = Math.PI / 2 + i * Math.PI / 5;
            double r = (i % 2 == 0) ? outerR : innerR;
            star.getPoints().addAll(r * Math.cos(angle), -r * Math.sin(angle));
        }
        star.setFill(c);
        return new Group(star);
    }

    private static Group orders(Color c) {
        Rectangle board = new Rectangle(-14, -17, 28, 34);
        board.setArcWidth(6);
        board.setArcHeight(6);
        board.setFill(Color.TRANSPARENT);
        board.setStroke(c);
        board.setStrokeWidth(2.4);

        Rectangle clip = new Rectangle(-6, -20, 12, 6);
        clip.setArcWidth(3);
        clip.setArcHeight(3);
        clip.setFill(c);

        Line row2 = new Line(-7, 2, 7, 2);
        Line row3 = new Line(-7, 9, 7, 9);
        row2.setStroke(c);
        row3.setStroke(c);
        row2.setStrokeWidth(2);
        row3.setStrokeWidth(2);

        Polyline check = new Polyline(-8, -6.5, -4.5, -3, 8, -10.5);
        check.setStroke(c);
        check.setStrokeWidth(2.4);
        check.setFill(Color.TRANSPARENT);

        return new Group(board, clip, row2, row3, check);
    }

    private static Group products(Color c) {
        Rectangle body = new Rectangle(-11, -18, 22, 36);
        body.setArcWidth(10);
        body.setArcHeight(10);
        body.setFill(Color.TRANSPARENT);
        body.setStroke(c);
        body.setStrokeWidth(2.4);

        Rectangle notch = new Rectangle(-4, -15.5, 8, 2.4);
        notch.setArcWidth(2);
        notch.setArcHeight(2);
        notch.setFill(c);

        Circle home = new Circle(0, 13.5, 2.4);
        home.setFill(c);

        return new Group(body, notch, home);
    }

    private static Group delivery(Color c) {
        Rectangle cargo = new Rectangle(-18, -9, 22, 16);
        cargo.setArcWidth(3);
        cargo.setArcHeight(3);
        cargo.setFill(c);

        Polygon cab = new Polygon(4, -9, 16, -9, 20, -1, 20, 7, 4, 7);
        cab.setFill(c);

        Rectangle window = new Rectangle(8, -6, 6, 6);
        window.setArcWidth(2);
        window.setArcHeight(2);
        window.setFill(Color.web("0B1220"));

        Circle w1 = new Circle(-9, 9, 4.2);
        Circle w2 = new Circle(11, 9, 4.2);
        w1.setFill(Color.web("0B1220"));
        w2.setFill(Color.web("0B1220"));
        w1.setStroke(c);
        w2.setStroke(c);
        w1.setStrokeWidth(1.6);
        w2.setStrokeWidth(1.6);

        return new Group(cargo, cab, window, w1, w2);
    }

    private static Group customers(Color c) {
        Circle head = new Circle(0, -10, 7.5);
        head.setFill(c);
        Polygon shoulders = new Polygon(-15, 18, 15, 18, 10, 0, -10, 0);
        shoulders.setFill(c);
        return new Group(head, shoulders);
    }

    private static Group warehouse(Color c) {
        Polygon roof = new Polygon(-18, -4, 0, -18, 18, -4);
        roof.setFill(c);
        Rectangle body = new Rectangle(-15, -4, 30, 22);
        body.setFill(c);
        Rectangle door = new Rectangle(-4, 4, 8, 14);
        door.setArcWidth(2);
        door.setArcHeight(2);
        door.setFill(Color.web("0B1220"));
        return new Group(roof, body, door);
    }
}
