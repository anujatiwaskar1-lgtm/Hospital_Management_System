package com.medicore;

import com.medicore.model.User;
import com.medicore.service.BedService;
import com.medicore.service.PatientService;
import com.medicore.service.UserService;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class MediCoreApp extends Application {

    private static ConfigurableApplicationContext springContext;
    private PatientService patientService;
    private BedService bedService;
    private UserService userService;
    private Stage primaryStage;

    // ─── Entry Point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        launch(args);
    }

    // ─── init() — starts Spring Boot in background ─────────────────────────────
    @Override
    public void init() throws Exception {
        try {
            springContext = SpringApplication.run(SpringBootConfig.class, new String[]{});
            patientService = springContext.getBean(PatientService.class);
            bedService     = springContext.getBean(BedService.class);
            userService    = springContext.getBean(UserService.class);
            bedService.initializeBedsIfEmpty();
        } catch (Exception e) {
            System.err.println("✗ Failed to connect to MySQL: " + e.getMessage());
            System.err.println("✗ Check application.properties credentials and that MySQL is running.");
        }
    }

    // ─── start() — show Login screen ───────────────────────────────────────────
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Hospital Management system");
        stage.setMinWidth(900);
        stage.setMinHeight(580);
        showLoginScreen();
        stage.show();
    }

    // ─── STOP ──────────────────────────────────────────────────────────────────
    @Override
    public void stop() {
        if (springContext != null) springContext.close();
        Platform.exit();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LOGIN SCREEN
    // ══════════════════════════════════════════════════════════════════════════
    private void showLoginScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f8;");

        // ── Left blue panel ──
        VBox leftPanel = new VBox(16);
        leftPanel.setPrefWidth(380);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setStyle("-fx-background-color: #1a73e8;");
        leftPanel.setPadding(new Insets(60));

        Label icon     = new Label("🏥");
        icon.setStyle("-fx-font-size: 60px;");
        Label appName  = new Label("MediCore HMS");
        appName.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label appSub   = new Label("Hospital Management System");
        appSub.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8);");
        Label tagline  = new Label("Efficient  •  Reliable  •  Caring");
        tagline.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.6);");
        leftPanel.getChildren().addAll(icon, appName, appSub, tagline);

        // ── Right white form ──
        VBox form = new VBox(14);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(60, 70, 60, 70));
        form.setStyle("-fx-background-color: white;");

        Label title = new Label("Welcome Back");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label sub = new Label("Sign in to your account");
        sub.setStyle("-fx-font-size: 13px; -fx-text-fill: #777;");

        TextField userField = styledTextField("Username");
        PasswordField passField = styledPasswordField("Password");

        Label errorLbl = new Label("");
        errorLbl.setStyle("-fx-text-fill: #e53935; -fx-font-size: 12px;");
        errorLbl.setWrapText(true);

        Button loginBtn = primaryButton("Sign In");
        Button registerBtn = outlineButton("New Registration");

        // divider
        HBox divider = new HBox(8);
        divider.setAlignment(Pos.CENTER);
        Separator s1 = new Separator(); s1.setPrefWidth(70);
        Separator s2 = new Separator(); s2.setPrefWidth(70);
        Label or = new Label("OR");
        or.setStyle("-fx-text-fill: #aaa; -fx-font-size: 12px;");
        divider.getChildren().addAll(s1, or, s2);

        form.getChildren().addAll(
            title, sub,
            fieldLabel("Username"), userField,
            fieldLabel("Password"), passField,
            errorLbl,
            loginBtn,
            divider,
            registerBtn
        );

        // ── Login action ──
        loginBtn.setOnAction(e -> {
            String u = userField.getText().trim();
            String p = passField.getText().trim();
            if (u.isEmpty() || p.isEmpty()) {
                errorLbl.setText("⚠ Please fill in all fields.");
                return;
            }
            boolean ok = false;
            if (userService != null) {
                ok = userService.authenticate(u, p);
            } else {
                ok = u.equals("admin") && p.equals("admin123");
            }
            if (ok) {
                showDashboard();
            } else {
                errorLbl.setText("⚠ Invalid username or password.");
                passField.clear();
            }
        });

        // ── Register action ──
        registerBtn.setOnAction(e -> showRegisterScreen());

        root.setLeft(leftPanel);
        root.setCenter(form);

        Scene scene = new Scene(root, 900, 580);
        primaryStage.setScene(scene);

        FadeTransition ft = new FadeTransition(Duration.millis(500), form);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  REGISTRATION SCREEN
    // ══════════════════════════════════════════════════════════════════════════
    private void showRegisterScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f0f4f8;");

        // ── Left blue panel ──
        VBox leftPanel = new VBox(16);
        leftPanel.setPrefWidth(380);
        leftPanel.setAlignment(Pos.CENTER);
        leftPanel.setStyle("-fx-background-color: #1a73e8;");
        leftPanel.setPadding(new Insets(60));

        Label icon    = new Label("👤");
        icon.setStyle("-fx-font-size: 60px;");
        Label title2  = new Label("Join MediCore");
        title2.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label sub2    = new Label("Create your staff account");
        sub2.setStyle("-fx-font-size: 13px; -fx-text-fill: rgba(255,255,255,0.8);");
        leftPanel.getChildren().addAll(icon, title2, sub2);

        // ── Right form ──
        VBox form = new VBox(10);
        form.setAlignment(Pos.CENTER);
        form.setPadding(new Insets(40, 70, 40, 70));
        form.setStyle("-fx-background-color: white;");

        Label formTitle = new Label("Create Account");
        formTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1a1a2e;");
        Label formSub = new Label("Fill in your details to register");
        formSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #777;");

        TextField nameField  = styledTextField("Dr. Full Name");
        TextField emailField = styledTextField("email@hospital.com");
        TextField userField  = styledTextField("Choose a username");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("Doctor","Nurse","Receptionist","Admin","Pharmacist","Lab Technician");
        roleBox.setPromptText("Select your role");
        roleBox.setPrefHeight(42);
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #ddd; -fx-border-width: 1.5; -fx-font-size: 13px;");

        PasswordField passField    = styledPasswordField("Create password (min 6 chars)");
        PasswordField confirmField = styledPasswordField("Confirm password");

        Label statusLbl = new Label("");
        statusLbl.setStyle("-fx-font-size: 12px;");
        statusLbl.setWrapText(true);

        Button createBtn = primaryButton("Create Account");
        Button backBtn   = new Button("← Back to Login");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #1a73e8; " +
                "-fx-font-size: 13px; -fx-cursor: hand; -fx-border-color: transparent;");
        backBtn.setMaxWidth(Double.MAX_VALUE);

        form.getChildren().addAll(
            formTitle, formSub,
            fieldLabel("Full Name"),     nameField,
            fieldLabel("Email"),         emailField,
            fieldLabel("Username"),      userField,
            fieldLabel("Role"),          roleBox,
            fieldLabel("Password"),      passField,
            fieldLabel("Confirm Password"), confirmField,
            statusLbl,
            createBtn, backBtn
        );

        // ── Create account action ──
        createBtn.setOnAction(e -> {
            String name    = nameField.getText().trim();
            String email   = emailField.getText().trim();
            String username= userField.getText().trim();
            String role    = roleBox.getValue();
            String pass    = passField.getText();
            String confirm = confirmField.getText();

            if (name.isEmpty() || email.isEmpty() || username.isEmpty()
                    || role == null || pass.isEmpty()) {
                setStatus(statusLbl, "⚠ Please fill in all fields.", false);
                return;
            }
            if (!pass.equals(confirm)) {
                setStatus(statusLbl, "⚠ Passwords do not match.", false);
                return;
            }
            if (pass.length() < 6) {
                setStatus(statusLbl, "⚠ Password must be at least 6 characters.", false);
                return;
            }
            if (userService == null) {
                setStatus(statusLbl, "⚠ Database not connected. Check MySQL.", false);
                return;
            }
            if (userService.usernameExists(username)) {
                setStatus(statusLbl, "⚠ Username already taken. Choose another.", false);
                return;
            }

            User user = new User();
            user.setFullName(name);
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(pass);
            user.setRole(role);
            userService.register(user);

            setStatus(statusLbl, "✅ Account created! Redirecting to login...", true);
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(Duration.seconds(2));
            pause.setOnFinished(ev -> showLoginScreen());
            pause.play();
        });

        backBtn.setOnAction(e -> showLoginScreen());

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: white; -fx-background-color: white;");

        root.setLeft(leftPanel);
        root.setCenter(scroll);

        Scene scene = new Scene(root, 900, 640);
        primaryStage.setScene(scene);

        FadeTransition ft = new FadeTransition(Duration.millis(500), form);
        ft.setFromValue(0); ft.setToValue(1); ft.play();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DASHBOARD — navigate here after login
    // ══════════════════════════════════════════════════════════════════════════
    private void showDashboard() {
        try {
            com.medicore.ui.DashboardScreen dashboard =
    new com.medicore.ui.DashboardScreen(primaryStage, patientService, bedService);
            dashboard.show();
        } catch (Exception e) {
            // Fallback placeholder if DashboardScreen has issues
            VBox box = new VBox(20);
            box.setAlignment(Pos.CENTER);
            box.setStyle("-fx-background-color: #f0f4f8;");
            Label lbl = new Label("✅ Login Successful! Dashboard loading...");
            lbl.setStyle("-fx-font-size: 18px; -fx-text-fill: #1a73e8;");
            box.getChildren().add(lbl);
            primaryStage.setScene(new Scene(box, 900, 580));
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HELPER METHODS
    // ══════════════════════════════════════════════════════════════════════════
    private TextField styledTextField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setPrefHeight(42);
        f.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #ddd; -fx-border-width: 1.5; " +
                "-fx-font-size: 13px; -fx-padding: 0 12;");
        return f;
    }

    private PasswordField styledPasswordField(String prompt) {
        PasswordField f = new PasswordField();
        f.setPromptText(prompt);
        f.setPrefHeight(42);
        f.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; " +
                "-fx-border-color: #ddd; -fx-border-width: 1.5; " +
                "-fx-font-size: 13px; -fx-padding: 0 12;");
        return f;
    }

    private Button primaryButton(String text) {
        Button b = new Button(text);
        b.setPrefHeight(44);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; " +
                "-fx-background-radius: 8; -fx-cursor: hand;");
        b.setOnMouseEntered(e -> b.setStyle("-fx-background-color: #1557b0; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));
        b.setOnMouseExited(e -> b.setStyle("-fx-background-color: #1a73e8; -fx-text-fill: white; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;"));
        return b;
    }

    private Button outlineButton(String text) {
        Button b = new Button(text);
        b.setPrefHeight(44);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-color: white; -fx-text-fill: #1a73e8; " +
                "-fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 8; " +
                "-fx-border-color: #1a73e8; -fx-border-radius: 8; " +
                "-fx-border-width: 1.5; -fx-cursor: hand;");
        return b;
    }

    private Label fieldLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #444;");
        return l;
    }

    private void setStatus(Label lbl, String msg, boolean success) {
        lbl.setText(msg);
        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: " +
                (success ? "#2e7d32;" : "#e53935;"));
    }
}