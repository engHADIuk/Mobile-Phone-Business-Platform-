package com.ecommerce.finance.launchpad;

import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The screen shown before login: seven components of the group project.
 * A tile is live when the host application supplies an action for its id;
 * every other tile is shown for context but is not interactive, and is
 * clearly marked as such.
 *
 * <p>Presentation only: the tiles are rendered as "liquid glass" panels - a
 * bright refracted rim, a cool translucent body, and a specular highlight that
 * follows the pointer across the surface. None of this changes what a tile
 * does; the id -&gt; action wiring is exactly as it was.</p>
 */
public final class LaunchpadView {

    private static final String INK = "#FFFFFF";
    private static final String BODY = "#C7D6EE";
    private static final String MUTED = "#8AA1C2";
    private static final String FAINT = "#5B739C";
    private static final String ACCENT = "#6FA8FF";
    private static final String ACCENT_DEEP = "#3B7BF0";
    private static final String ICE = "#DCEAFF";

    /** Corner radius of a glass panel; the inner fills sit one hair inside it. */
    private static final double GLASS_RADIUS = 20;
    private static final double GLASS_RIM = 1.25;

    private record Tile(String id, String name, String desc, boolean active) {
    }

    private static final List<Tile> TILES = List.of(
            new Tile("finance", "Finance & Reporting",
                    "Checkout totals, VAT, invoicing and the admin sales dashboard.", true),
            new Tile("reviews", "Reviews & Ratings",
                    "Product ratings, written reviews and moderation.", true),
            new Tile("orders", "Orders Tracking",
                    "Order status, history and fulfilment.", true),
            new Tile("products", "Products",
                    "Catalogue browsing, product detail and search.", true),
            new Tile("delivery", "Delivery & Logistics",
                    "Driver dashboard, live map and delivery status.", true),
            new Tile("customers", "Customers",
                    "Customer accounts, addresses and payment methods.", true),
            new Tile("warehouse", "Warehouse",
                    "Stock levels, transfers and warehouse oversight.", true)
    );

    private final Map<String, Runnable> actions;

    /**
     * @param actions tile id -> what happens when that tile is clicked. A tile
     *                with no entry here stays greyed out and non-interactive,
     *                so adding a component is a one-line change in MainApp.
     */
    public LaunchpadView(Map<String, Runnable> actions) {
        this.actions = actions == null ? Map.of() : Map.copyOf(actions);
    }

    /** Kept so the original single-component entry point still compiles. */
    public LaunchpadView(Runnable onEnterFinance) {
        this(Map.of("finance", onEnterFinance));
    }

    /** Kept so the two-component entry point still compiles. */
    public LaunchpadView(Runnable onEnterFinance, Runnable onEnterDelivery) {
        this(Map.of("finance", onEnterFinance, "delivery", onEnterDelivery));
    }

    public Parent build() {
        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #060B16 0%, #102A54 55%, #081226 100%);");
        root.getChildren().addAll(
                glow(460, ACCENT_DEEP, 0.50, -430, -270),
                glow(380, ACCENT, 0.28, 470, 250),
                glow(300, ACCENT_DEEP, 0.30, -380, 300),
                // A fourth, cooler pool of light. Glass only reads as glass when
                // there is something varied behind it to refract.
                glow(340, "#28E0C8", 0.16, 300, -320));

        VBox content = new VBox(26);
        content.setPadding(new Insets(38, 46, 34, 46));
        content.setMaxWidth(1180);
        content.getChildren().addAll(header(), tileGrid());
        StackPane.setAlignment(content, Pos.TOP_CENTER);

        ScrollPane scroller = new ScrollPane(content);
        scroller.setFitToWidth(true);
        scroller.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scroller.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        root.getChildren().add(scroller);
        return root;
    }

    private VBox header() {
        Label eyebrow = new Label("VELMORA  ·  GROUP PROJECT (1605B)");
        eyebrow.setTextFill(Color.web(ACCENT));
        eyebrow.setFont(Font.font("Arial", FontWeight.BOLD, 11.5));
        eyebrow.setStyle("-fx-letter-spacing: 2px;");

        Label title = new Label("Launchpad");
        title.setTextFill(Color.web(INK));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 36));

        Label subtitle = new Label(
                "Each live tile opens that team member's component exactly as they built it. "
                        + "Greyed-out tiles are shown for context but are not part of this build.");
        subtitle.setTextFill(Color.web(BODY));
        subtitle.setFont(Font.font("Arial", 13.5));
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(760);

        return new VBox(10, eyebrow, title, subtitle);
    }

    private FlowPane tileGrid() {
        FlowPane flow = new FlowPane();
        flow.setHgap(20);
        flow.setVgap(20);
        flow.setAlignment(Pos.TOP_CENTER);
        for (Tile t : TILES) {
            Runnable action = actions.get(t.id());
            flow.getChildren().add(action != null ? buildActiveTile(t, action) : buildInactiveTile(t));
        }
        return flow;
    }

    private VBox buildActiveTile(Tile t, Runnable action) {
        StackPane badge = badge(t.id(), ACCENT_DEEP, ICE, 1.0);

        Label title = new Label(t.name());
        title.setTextFill(Color.web(INK));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14.5));
        title.setWrapText(true);

        Label desc = new Label(t.desc());
        desc.setTextFill(Color.web(MUTED));
        desc.setFont(Font.font("Arial", 11));
        desc.setWrapText(true);
        desc.setMinHeight(46);

        Circle dot = new Circle(4, Color.web("#57D38C"));
        Label status = new Label("Enter  →");
        status.setTextFill(Color.web("#57D38C"));
        status.setFont(Font.font("Arial", FontWeight.BOLD, 10.5));
        javafx.scene.layout.HBox statusRow = new javafx.scene.layout.HBox(7, dot, status);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);

        VBox card = new VBox(10, badge, title, desc, grow, statusRow);
        card.setPadding(new Insets(20));
        card.setPrefWidth(258);
        card.setMinHeight(214);
        card.setCursor(Cursor.HAND);
        applyGlass(card, false, 1.0);

        // Where the specular highlight currently sits, so we can skip redundant
        // restyles while the pointer creeps along a pixel at a time.
        final double[] spot = {-999, -999};

        card.setOnMouseEntered(e -> {
            spot[0] = -999;
            spot[1] = -999;
            applyGlass(card, true, 1.0);
            lift(card, 1.02, -6);
        });
        card.setOnMouseMoved(e -> {
            double w = Math.max(1, card.getWidth());
            double h = Math.max(1, card.getHeight());
            double px = e.getX() / w * 100.0;
            double py = e.getY() / h * 100.0;
            if (Math.abs(px - spot[0]) < 2.5 && Math.abs(py - spot[1]) < 2.5) {
                return;
            }
            spot[0] = px;
            spot[1] = py;
            applyGlass(card, true, 1.0, px, py);
        });
        card.setOnMouseExited(e -> {
            spot[0] = -999;
            spot[1] = -999;
            applyGlass(card, false, 1.0);
            lift(card, 1.0, 0);
        });
        card.setOnMousePressed(e -> lift(card, 0.985, -2));
        card.setOnMouseReleased(e -> lift(card, 1.02, -6));
        card.setOnMouseClicked(e -> {
            if (action != null) {
                action.run();
            }
        });

        return card;
    }

    private VBox buildInactiveTile(Tile t) {
        StackPane badge = badge(t.id(), "#33415F", "#7C8FB3", 0.55);

        Label title = new Label(t.name());
        title.setTextFill(Color.web(MUTED));
        title.setFont(Font.font("Arial", FontWeight.BOLD, 14.5));
        title.setWrapText(true);

        Label desc = new Label(t.desc());
        desc.setTextFill(Color.web(FAINT));
        desc.setFont(Font.font("Arial", 11));
        desc.setWrapText(true);
        desc.setMinHeight(46);

        Label status = new Label("Not included in this build");
        status.setTextFill(Color.web(FAINT));
        status.setFont(Font.font("Arial", FontWeight.BOLD, 9.5));
        status.setStyle("-fx-font-style: italic;");

        Region grow = new Region();
        VBox.setVgrow(grow, Priority.ALWAYS);

        VBox card = new VBox(10, badge, title, desc, grow, status);
        card.setPadding(new Insets(20));
        card.setPrefWidth(258);
        card.setMinHeight(214);
        card.setOpacity(0.62);
        applyGlass(card, false, 0.55);
        // deliberately no hover/click wiring: this tile is not interactive
        return card;
    }

    /**
     * A glossy bead: a bright rim, a domed body in the tile's tint, and a
     * highlight up in the top-left as though lit from above.
     */
    private StackPane badge(String iconId, String tint, String glyph, double opacity) {
        StackPane badge = new StackPane();
        badge.setPrefSize(58, 58);
        badge.setMaxSize(58, 58);
        badge.setMinSize(58, 58);
        badge.setOpacity(opacity);
        badge.setStyle(
                "-fx-background-color: "
                        + "linear-gradient(from 25% 0% to 75% 100%, "
                        + "rgba(255,255,255,0.60) 0%, rgba(255,255,255,0.10) 55%, rgba(255,255,255,0.28) 100%), "
                        + "radial-gradient(center 36% 26%, radius 82%, "
                        + tint + " 0%, derive(" + tint + ", -32%) 100%), "
                        + "radial-gradient(center 32% 20%, radius 44%, "
                        + "rgba(255,255,255,0.50) 0%, rgba(255,255,255,0.0) 100%);"
                        + "-fx-background-insets: 0, 1, 1;"
                        + "-fx-background-radius: 29, 28, 28;");
        badge.getChildren().add(Icons.forComponent(iconId, Color.web(glyph)));
        return badge;
    }

    /** Resting/hover glass with the highlight in its default upper-left spot. */
    private void applyGlass(Region region, boolean hover, double baseOpacityFactor) {
        applyGlass(region, hover, baseOpacityFactor, 28, 14);
    }

    /**
     * Paints a pane of liquid glass.
     *
     * <p>Three stacked fills do the work. The outermost is a light-catching rim
     * that runs bright at the top-left and fades round the edge. Inset just
     * inside it is the cool translucent body. On top sits a soft specular bloom
     * centred on {@code spotX}/{@code spotY}, which the caller moves with the
     * pointer so the sheen slides across the surface.</p>
     *
     * @param spotX horizontal centre of the highlight, 0-100 across the region
     * @param spotY vertical centre of the highlight, 0-100 down the region
     */
    private void applyGlass(Region region, boolean hover, double f, double spotX, double spotY) {
        double rimHot = clamp((hover ? 0.66 : 0.44) * f);
        double rimCool = clamp((hover ? 0.15 : 0.09) * f);
        double rimFoot = clamp((hover ? 0.34 : 0.22) * f);

        double bodyTop = clamp((hover ? 0.150 : 0.080) * f);
        double bodyBottom = clamp((hover ? 0.055 : 0.026) * f);

        double bloom = clamp((hover ? 0.30 : 0.13) * f);

        double innerRadius = GLASS_RADIUS - GLASS_RIM;

        region.setStyle(
                "-fx-background-color: "
                        // 1. the refracted rim
                        + "linear-gradient(from 0% 0% to 58% 100%, "
                        + "rgba(255,255,255," + n(rimHot) + ") 0%, "
                        + "rgba(255,255,255," + n(rimCool) + ") 40%, "
                        + "rgba(255,255,255," + n(rimCool) + ") 60%, "
                        + "rgba(255,255,255," + n(rimFoot) + ") 100%), "
                        // 2. the glass itself
                        + "linear-gradient(from 0% 0% to 0% 100%, "
                        + "rgba(233,244,255," + n(bodyTop) + ") 0%, "
                        + "rgba(138,178,246," + n(bodyBottom) + ") 100%), "
                        // 3. specular bloom, tracking the pointer
                        + "radial-gradient(center " + n(spotX) + "% " + n(spotY) + "%, radius 62%, "
                        + "rgba(255,255,255," + n(bloom) + ") 0%, "
                        + "rgba(255,255,255," + n(bloom * 0.30) + ") 40%, "
                        + "rgba(255,255,255,0.0) 100%);"
                        + "-fx-background-insets: 0, " + n(GLASS_RIM) + ", " + n(GLASS_RIM) + ";"
                        + "-fx-background-radius: " + n(GLASS_RADIUS) + ", "
                        + n(innerRadius) + ", " + n(innerRadius) + ";");

        // Contact shadow, plus a cold halo on hover so the panel reads as if it
        // has floated up off the background.
        DropShadow contact = new DropShadow();
        contact.setColor(Color.web("#020509", hover ? 0.60 : 0.42));
        contact.setRadius(hover ? 34 : 20);
        contact.setOffsetY(hover ? 14 : 7);

        DropShadow halo = new DropShadow();
        halo.setColor(Color.web(ACCENT, hover ? 0.30 : 0.0));
        halo.setRadius(hover ? 44 : 0);
        halo.setSpread(0.02);
        halo.setInput(contact);

        region.setEffect(halo);
    }

    /** Nudges a panel up and out towards the viewer. */
    private void lift(Region node, double scale, double rise) {
        ScaleTransition st = new ScaleTransition(Duration.millis(160), node);
        st.setInterpolator(Interpolator.EASE_BOTH);
        st.setToX(scale);
        st.setToY(scale);
        st.play();

        TranslateTransition tt = new TranslateTransition(Duration.millis(160), node);
        tt.setInterpolator(Interpolator.EASE_BOTH);
        tt.setToY(rise);
        tt.play();
    }

    private static double clamp(double v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    /** Fixed-point, so a comma-decimal locale can never corrupt the CSS. */
    private static String n(double v) {
        return String.format(Locale.ROOT, "%.3f", v);
    }

    private Circle glow(double diameter, String hex, double opacity, double tx, double ty) {
        Circle c = new Circle(diameter / 2.0, Color.web(hex, opacity));
        c.setEffect(new GaussianBlur(70));
        c.setTranslateX(tx);
        c.setTranslateY(ty);
        c.setMouseTransparent(true);
        return c;
    }
}
