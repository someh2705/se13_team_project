module org.se13 {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.se13 to javafx.fxml;
    opens org.se13.view to javafx.fxml;
    exports org.se13;
    exports org.se13.view;
}