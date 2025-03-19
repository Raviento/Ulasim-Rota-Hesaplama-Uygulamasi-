module com.example.prolab4 {
    requires javafx.controls;
    requires javafx.fxml;
    // Harita gösterecekseniz WebView için
    requires javafx.web;

    // Jackson
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;

    // Eğer @JsonProperty vb. Jackson anotasyonları kullanıyorsanız:
    // requires com.fasterxml.jackson.annotation;

    // Jackson veya FXML'in (reflection) erişmesi gereken paketleri açın:
    // Projede model sınıfları com.example.prolab4 içinde ise
    opens com.example.prolab4 to javafx.fxml, com.fasterxml.jackson.databind;

    exports com.example.prolab4;
}
