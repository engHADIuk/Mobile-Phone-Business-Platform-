package uk.ac.brunel.delivery;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FxmlResourceTest {
    private static final List<String> VIEWS = List.of(
            "LoginView.fxml",
            "AdminDashboardView.fxml",
            "DeliveriesView.fxml",
            "AssignDeliveryView.fxml",
            "DeliveryDetailView.fxml",
            "DriverDashboardView.fxml",
            "LiveMapView.fxml"
    );

    @Test
    void everyScreenHasAWellFormedFxmlResource() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

        for (String view : VIEWS) {
            String resource = "/uk/ac/brunel/delivery/view/" + view;
            try (InputStream input = getClass().getResourceAsStream(resource)) {
                assertNotNull(input, "Missing FXML resource: " + view);
                Document document = factory.newDocumentBuilder().parse(input);
                assertEquals("uk.ac.brunel.delivery.controller." + view.replace("View.fxml", "Controller"),
                        document.getDocumentElement().getAttribute("fx:controller"));
            }
        }
    }
}
