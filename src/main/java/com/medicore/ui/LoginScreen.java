package com.medicore.ui;

import com.medicore.service.BedService;
import com.medicore.service.PatientService;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * ✅ LoginScreen — JavaFX Login Page (Light Theme)
 *
 * Hardcoded credentials: admin / admin123
 * On success → opens DashboardScreen (passes patientService & bedService)
 */
public class LoginScreen {

    private final Stage          stage;
    private final PatientService patientService;
    private final BedService     bedService;

    public LoginScreen(Stage stage, PatientService patientService, BedService bedService) {
        this.stage          = stage;
        this.patientService = patientService;
        this.bedService     = bedService;
    }

    // ─── Build and show the login scene ──────────────────────────────────────
    public void show() {
        StackPane root = new StackPane();

        // ── Background: soft blue-white gradient ─────────────────────────────
        root.setBackground(new Background(new BackgroundFill(
            new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.web("#dbeafe")),
                new Stop(0.5, Color.web("#f0f9ff")),
                new Stop(1.0, Color.web("#e0f2fe"))),
            null, null)));

        // ── Decorative background circles ────────────────────────────────────
        root.getChildren().addAll(
            decorCircle(220, "#93c5fd", 0.35, -400, -260),
            decorCircle(180, "#60a5fa", 0.25,  450,  320),
            decorCircle(120, "#3b82f6", 0.18,  360, -340),
            decorCircle(90,  "#bfdbfe", 0.40, -320,  280)
        );

        // ── Login card ───────────────────────────────────────────────────────
        VBox card = buildCard();
        root.getChildren().add(card);

        // ── Entrance animation ───────────────────────────────────────────────
        card.setOpacity(0);
        card.setTranslateY(24);
        ParallelTransition entrance = new ParallelTransition(
            fadeTransition(card, 0, 1, 700),
            translateTransition(card, 24, 0, 700)
        );
        entrance.setDelay(Duration.millis(100));

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        entrance.play();
    }

    // ─── Card ────────────────────────────────────────────────────────────────
    private VBox buildCard() {
        VBox card = new VBox(0);
        card.setAlignment(Pos.TOP_CENTER);
        card.setMaxWidth(440);
        card.setPadding(new Insets(48, 52, 44, 52));
        card.setBackground(new Background(new BackgroundFill(
            Color.WHITE, new CornerRadii(24), null)));
        card.setEffect(dropShadow(0.10, 36, 10));

        // ── Icon ─────────────────────────────────────────────────────────────
        StackPane icon = new StackPane();
        Circle iconBg = new Circle(40, Color.web("#dbeafe"));
        Label iconLbl = new Label("🏥");
        iconLbl.setStyle("-fx-font-size:30px;");
        icon.getChildren().addAll(iconBg, iconLbl);
        card.getChildren().add(icon);
        VBox.setMargin(icon, new Insets(0, 0, 16, 0));

        // ── Title ─────────────────────────────────────────────────────────────
        Label title = new Label("MediCore HMS");
        title.setStyle("-fx-font-size:26px; -fx-font-weight:bold; -fx-text-fill:#1e3a5f;");
        card.getChildren().add(title);
        VBox.setMargin(title, new Insets(0, 0, 4, 0));

        // ── Subtitle ──────────────────────────────────────────────────────────
        Label sub = new Label("Hospital Management System");
        sub.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b;");
        card.getChildren().add(sub);
        VBox.setMargin(sub, new Insets(0, 0, 24, 0));

        // ── Divider ───────────────────────────────────────────────────────────
        Separator sep = new Separator();
        card.getChildren().add(sep);
        VBox.setMargin(sep, new Insets(0, 0, 24, 0));

        // ── Username ──────────────────────────────────────────────────────────
        Label userLbl = fieldLabel("Username");
        TextField userField = styledField("Enter your username", false);
        card.getChildren().addAll(userLbl, userField);
        VBox.setMargin(userLbl,  new Insets(0, 0, 6, 0));
        VBox.setMargin(userField, new Insets(0, 0, 16, 0));

        // ── Password ──────────────────────────────────────────────────────────
        Label passLbl = fieldLabel("Password");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Enter your password");
        applyFieldStyle(passField);
        card.getChildren().addAll(passLbl, passField);
        VBox.setMargin(passLbl,  new Insets(0, 0, 6, 0));
        VBox.setMargin(passField, new Insets(0, 0, 8, 0));

        // ── Error label ───────────────────────────────────────────────────────
        Label errorLbl = new Label("");
        errorLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#ef4444;");
        errorLbl.setVisible(false);
        errorLbl.setManaged(false);
        card.getChildren().add(errorLbl);
        VBox.setMargin(errorLbl, new Insets(0, 0, 10, 0));

        // ── Login button ──────────────────────────────────────────────────────
        Button loginBtn = new Button("🔐  Login to Dashboard");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle(
            "-fx-background-color:#2563eb; -fx-text-fill:white;" +
            "-fx-font-size:14px; -fx-font-weight:bold;" +
            "-fx-padding:13 20; -fx-background-radius:10; -fx-cursor:hand;");
        hoverBlue(loginBtn);
        card.getChildren().add(loginBtn);
        VBox.setMargin(loginBtn, new Insets(0, 0, 12, 0));

        // ── Hint ──────────────────────────────────────────────────────────────
        Label hint = new Label("Default credentials:  admin  /  admin123");
        hint.setStyle("-fx-font-size:11px; -fx-text-fill:#94a3b8;");
        card.getChildren().add(hint);
        VBox.setMargin(hint, new Insets(0, 0, 20, 0));

        // ── Footer ────────────────────────────────────────────────────────────
        Separator sep2 = new Separator();
        card.getChildren().add(sep2);
        VBox.setMargin(sep2, new Insets(0, 0, 16, 0));

        Label footer = new Label("© 2026 MediCore HMS  •  JavaFX + Spring Boot + MySQL");
        footer.setStyle("-fx-font-size:10px; -fx-text-fill:#cbd5e1;");
        card.getChildren().add(footer);

        // ── Login action ──────────────────────────────────────────────────────
        Runnable doLogin = () -> {
            String user = userField.getText().trim();
            String pass = passField.getText().trim();

            if (user.isEmpty() || pass.isEmpty()) {
                showError(errorLbl, "Please enter both username and password.");
                return;
            }
            if ("admin".equals(user) && "admin123".equals(pass)) {
                // ✅ Success → open dashboard (services already connected to MySQL)
                DashboardScreen dashboard = new DashboardScreen(stage, patientService, bedService);
                dashboard.show();
            } else {
                showError(errorLbl, "❌ Invalid credentials. Use  admin / admin123");
                shakeCard(card);
            }
        };

        loginBtn.setOnAction(e -> doLogin.run());
        passField.setOnAction(e -> doLogin.run());
        userField.setOnAction(e -> passField.requestFocus());

        return card;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────
    private void showError(Label lbl, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
    }

    private void shakeCard(VBox card) {
        TranslateTransition shake = new TranslateTransition(Duration.millis(55), card);
        shake.setFromX(-8); shake.setToX(8);
        shake.setCycleCount(6); shake.setAutoReverse(true);
        shake.play();
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#374151;");
        return l;
    }

    private TextField styledField(String prompt, boolean readOnly) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setEditable(!readOnly);
        applyFieldStyle(f);
        return f;
    }

    private void applyFieldStyle(TextInputControl f) {
        String base = "-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0;" +
                      "-fx-border-radius:8; -fx-background-radius:8;" +
                      "-fx-padding:10 14; -fx-font-size:13px;";
        f.setStyle(base);
        f.setMinHeight(42);
        f.focusedProperty().addListener((ob, o, n) ->
            f.setStyle(n
                ? base.replace("#e2e8f0", "#2563eb")
                : base));
    }

    private Circle decorCircle(double r, String hex, double opacity, double tx, double ty) {
        Circle c = new Circle(r, Color.web(hex, opacity));
        c.setTranslateX(tx); c.setTranslateY(ty);
        return c;
    }

    private DropShadow dropShadow(double opacity, double radius, double offsetY) {
        DropShadow ds = new DropShadow();
        ds.setColor(Color.web(String.format("#000000%02X", (int)(opacity * 255))));
        ds.setRadius(radius); ds.setOffsetY(offsetY);
        return ds;
    }

    private FadeTransition fadeTransition(Node n, double from, double to, double ms) {
        FadeTransition ft = new FadeTransition(Duration.millis(ms), n);
        ft.setFromValue(from); ft.setToValue(to);
        return ft;
    }

    private TranslateTransition translateTransition(Node n, double fromY, double toY, double ms) {
        TranslateTransition tt = new TranslateTransition(Duration.millis(ms), n);
        tt.setFromY(fromY); tt.setToY(toY);
        return tt;
    }

    private void hoverBlue(Button btn) {
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace("#2563eb", "#1d4ed8")));
        btn.setOnMouseExited (e -> btn.setStyle(btn.getStyle().replace("#1d4ed8", "#2563eb")));
    }
}
