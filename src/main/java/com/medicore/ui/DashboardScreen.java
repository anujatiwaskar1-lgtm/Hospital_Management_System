package com.medicore.ui;

import com.medicore.model.Bed;
import com.medicore.model.Patient;
import com.medicore.service.BedService;
import com.medicore.service.PatientService;
import javafx.application.Platform;
import javafx.collections.*;
import javafx.concurrent.Task;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * ✅ DashboardScreen — Main JavaFX UI connected to Spring Boot + MySQL
 *
 * ALL database calls go through:
 *   PatientService / BedService  →  Repository  →  Hibernate SQL  →  MySQL
 *
 * JavaFX threading rules applied:
 *   - DB calls run on a background Thread (via Task<>)
 *   - UI updates always happen on Platform.runLater()
 */
public class DashboardScreen {

    private final Stage          stage;
    private final PatientService patientService;
    private final BedService     bedService;

    // ── Live data lists for JavaFX TableView ─────────────────────────────────
    private final ObservableList<Patient> tableData = FXCollections.observableArrayList();

    // ── Stat card labels (updated from DB) ───────────────────────────────────
    private Label lblTotalBeds, lblAvailable, lblOccupied, lblDischarged;

    // ── Bed map state ─────────────────────────────────────────────────────────
    private GridPane        bedMapGrid;
    private Bed             selectedBed  = null;
    private TextField       selectedBedField;
    private List<Bed>       allBeds      = new ArrayList<>();

    // ── Ward load & summary containers ───────────────────────────────────────
    private VBox wardLoadContent, summaryContent;

    // ── Chart ─────────────────────────────────────────────────────────────────
    private BarChart<String, Number> admissionsChart;

    // ── Filter state ──────────────────────────────────────────────────────────
    private String currentFilter = "All";
    private String searchQuery   = "";

    // ─────────────────────────────────────────────────────────────────────────
    public DashboardScreen(Stage stage, PatientService patientService, BedService bedService) {
        this.stage          = stage;
        this.patientService = patientService;
        this.bedService     = bedService;
    }

    // ─── Build and show the dashboard scene ──────────────────────────────────
    public void show() {
        BorderPane root = new BorderPane();
        root.setBackground(new Background(new BackgroundFill(Color.web("#f1f5f9"), null, null)));

        root.setTop(buildHeader());

        // ── Scrollable main content ───────────────────────────────────────────
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background:#f1f5f9; -fx-background-color:#f1f5f9;");

        VBox content = new VBox(14);
        content.setPadding(new Insets(16, 20, 24, 20));

        // Stat cards
        content.getChildren().add(buildStatsRow());

        // Middle row: Admit form | Bed Map
        HBox midRow = new HBox(14);
        VBox admitPanel = buildAdmitPanel();
        admitPanel.setMinWidth(310);
        admitPanel.setMaxWidth(340);
        VBox bedMapPanel = buildBedMapPanel();
        HBox.setHgrow(bedMapPanel, Priority.ALWAYS);
        midRow.getChildren().addAll(admitPanel, bedMapPanel);
        content.getChildren().add(midRow);

        // Bottom row: Ward Load | Chart | Summary
        HBox bottomRow = new HBox(14);
        VBox wardLoad = buildWardLoadPanel();  wardLoad.setMinWidth(200); wardLoad.setPrefWidth(230);
        VBox chart    = buildAdmissionsPanel(); HBox.setHgrow(chart, Priority.ALWAYS);
        VBox summary  = buildSummaryPanel();   summary.setMinWidth(200); summary.setPrefWidth(230);
        bottomRow.getChildren().addAll(wardLoad, chart, summary);
        content.getChildren().add(bottomRow);

        // Patient records table
        content.getChildren().add(buildPatientRecordsPanel());

        scroll.setContent(content);
        root.setCenter(scroll);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();

        // ✅ Load all data from MySQL on background thread
        refreshAllData();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HEADER
    // ═════════════════════════════════════════════════════════════════════════
    private HBox buildHeader() {
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 20, 0, 20));
        header.setMinHeight(62);
        header.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
        header.setEffect(shadow(0.08, 8, 2));

        // Logo
        StackPane iconBox = new StackPane();
        Rectangle iconBg = new Rectangle(38, 38, Color.web("#2563eb"));
        iconBg.setArcWidth(10); iconBg.setArcHeight(10);
        Label iconLbl = new Label("🏥");
        iconLbl.setStyle("-fx-font-size:20px;");
        iconBox.getChildren().addAll(iconBg, iconLbl);

        VBox titleBox = new VBox(2);
        Label appName = new Label("MediCore HMS");
        appName.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#1e3a5f;");
        Label appSub = new Label("Hospital Management System");
        appSub.setStyle("-fx-font-size:10px; -fx-text-fill:#94a3b8;");
        titleBox.getChildren().addAll(appName, appSub);

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        // Date badge
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        Label dateLbl = new Label("📅  " + today);
        dateLbl.setStyle("-fx-font-size:12px; -fx-text-fill:#374151;" +
            "-fx-background-color:#f1f5f9; -fx-padding:7 14;" +
            "-fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;");

        // Logout button
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle("-fx-background-color:transparent; -fx-border-color:#e2e8f0;" +
            "-fx-border-radius:8; -fx-text-fill:#64748b; -fx-padding:7 16; -fx-cursor:hand; -fx-font-size:12px;");
        logoutBtn.setOnAction(e -> new LoginScreen(stage, patientService, bedService).show());

        header.getChildren().addAll(iconBox, titleBox, spacer, dateLbl, logoutBtn);
        return header;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  STAT CARDS ROW
    // ═════════════════════════════════════════════════════════════════════════
    private HBox buildStatsRow() {
        HBox row = new HBox(14);
        lblTotalBeds  = statValue("20",  "#2563eb");
        lblAvailable  = statValue("20",  "#16a34a");
        lblOccupied   = statValue("0",   "#ea580c");
        lblDischarged = statValue("0",   "#7c3aed");

        String[] titles = {"🛏  Total Beds", "✅  Available", "🔴  Occupied", "📤  Discharged Today"};
        Label[]  values = {lblTotalBeds, lblAvailable, lblOccupied, lblDischarged};

        for (int i = 0; i < 4; i++) {
            VBox card = buildStatCard(titles[i], values[i]);
            HBox.setHgrow(card, Priority.ALWAYS);
            row.getChildren().add(card);
        }
        return row;
    }

    private VBox buildStatCard(String title, Label valueLbl) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18, 20, 16, 20));
        card.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(12), null)));
        card.setEffect(shadow(0.08, 12, 2));

        Label t = new Label(title);
        t.setStyle("-fx-font-size:12px; -fx-text-fill:#64748b; -fx-font-weight:bold;");

        // Color strip
        String color = extractColor(valueLbl);
        Rectangle strip = new Rectangle(36, 4, Color.web(color));
        strip.setArcWidth(4); strip.setArcHeight(4);

        card.getChildren().addAll(t, valueLbl, strip);
        return card;
    }

    private Label statValue(String text, String color) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:32px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
        return l;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ADMIT PATIENT PANEL
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildAdmitPanel() {
        VBox panel = card("👤  Admit Patient");

        // Auto-generated Patient ID
        TextField idField   = inputField("e.g. P-0001", false);
        TextField nameField = inputField("e.g. Anjali Sharma", false);

        // Age + Ward row
        HBox ageWardRow = new HBox(10);
        VBox ageBox = new VBox(5);
        TextField ageField = inputField("35", false);
        ageField.setMaxWidth(80);
        ageBox.getChildren().addAll(lbl("Age"), ageField);

        VBox wardBox = new VBox(5);
        ComboBox<String> wardCb = new ComboBox<>();
        wardCb.getItems().addAll("General", "ICU", "Pediatric", "Orthopedic");
        wardCb.setPromptText("Select ward");
        wardCb.setMaxWidth(Double.MAX_VALUE);
        wardCb.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0;" +
            "-fx-border-radius:8; -fx-background-radius:8; -fx-font-size:12px;");
        wardCb.setMinHeight(38);
        HBox.setHgrow(wardBox, Priority.ALWAYS);
        wardBox.getChildren().addAll(lbl("Ward"), wardCb);
        ageWardRow.getChildren().addAll(ageBox, wardBox);

        TextField diagField = inputField("e.g. Fever, Fracture...", false);
        selectedBedField    = inputField("No bed selected", true);

        // Status feedback label
        Label statusLbl = new Label("");
        statusLbl.setStyle("-fx-font-size:12px;");
        statusLbl.setWrapText(true);

        // Admit button
        Button admitBtn = new Button("➕  Admit Patient");
        admitBtn.setMaxWidth(Double.MAX_VALUE);
        admitBtn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white;" +
            "-fx-font-size:13px; -fx-font-weight:bold; -fx-padding:12;" +
            "-fx-background-radius:10; -fx-cursor:hand;");
        hoverBtn(admitBtn, "#2563eb", "#1d4ed8");

        admitBtn.setOnAction(e -> {
            // ── Build patient ID ────────────────────────────────────────────
            String pid  = idField.getText().trim();
            String name = nameField.getText().trim();
            String ageS = ageField.getText().trim();
            String ward = wardCb.getValue();
            String diag = diagField.getText().trim();

            if (name.isEmpty() || ageS.isEmpty() || ward == null || diag.isEmpty() || selectedBed == null) {
                setStatus(statusLbl, "⚠  Fill all fields and select a green bed.", "#ef4444");
                return;
            }
            int age;
            try { age = Integer.parseInt(ageS); } catch (Exception ex) {
                setStatus(statusLbl, "⚠  Age must be a number.", "#ef4444");
                return;
            }
            if (pid.isEmpty()) pid = patientService.generateNextPatientId();

            final String finalPid  = pid;
            final String finalName = name;
            final int    finalAge  = age;
            final String finalBed  = selectedBed.getBedId();
            final String finalWard = ward;
            final String finalDiag = diag;

            admitBtn.setDisable(true);

            // ✅ DB write on background thread
            Task<Patient> task = new Task<>() {
                @Override protected Patient call() {
                    return patientService.admitPatient(finalPid, finalName, finalAge,
                                                       finalWard, finalBed, finalDiag);
                }
            };
            task.setOnSucceeded(ev -> Platform.runLater(() -> {
                admitBtn.setDisable(false);
                setStatus(statusLbl, "✓ " + finalName + " admitted to bed " + finalBed + ".", "#16a34a");
                idField.clear(); nameField.clear(); ageField.clear();
                wardCb.setValue(null); diagField.clear();
                selectedBedField.setText("No bed selected");
                selectedBed = null;
                refreshAllData();
            }));
            task.setOnFailed(ev -> Platform.runLater(() -> {
                admitBtn.setDisable(false);
                setStatus(statusLbl, "❌ " + task.getException().getMessage(), "#ef4444");
            }));
            new Thread(task).start();
        });

        panel.getChildren().addAll(
            lbl("Patient ID"),      idField,
            lbl("Full Name"),       nameField,
            ageWardRow,
            lbl("Diagnosis"),       diagField,
            lbl("Bed — click a green bed below"), selectedBedField,
            admitBtn, statusLbl
        );

        // Add spacing
        for (int i = 0; i < panel.getChildren().size(); i++) {
            VBox.setMargin(panel.getChildren().get(i), new Insets(0, 0, 8, 0));
        }

        return panel;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  BED MAP
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildBedMapPanel() {
        VBox panel = card("🛏  Bed Map");

        // Legend
        HBox legend = new HBox(16);
        legend.getChildren().addAll(
            legendItem("Available", "#4ade80"),
            legendItem("Occupied",  "#f97316"),
            legendItem("Selected",  "#3b82f6")
        );
        panel.getChildren().add(legend);

        bedMapGrid = new GridPane();
        bedMapGrid.setHgap(8);
        bedMapGrid.setVgap(8);

        ScrollPane bedScroll = new ScrollPane(bedMapGrid);
        bedScroll.setFitToWidth(true);
        bedScroll.setStyle("-fx-background:transparent; -fx-background-color:transparent;");
        bedScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        panel.getChildren().add(bedScroll);
        VBox.setVgrow(bedScroll, Priority.ALWAYS);
        return panel;
    }

    private void renderBedMap() {
        bedMapGrid.getChildren().clear();

        // Group by ward maintaining insertion order
        Map<String, List<Bed>> byWard = new LinkedHashMap<>();
        for (Bed b : allBeds) byWard.computeIfAbsent(b.getWard(), k -> new ArrayList<>()).add(b);

        int row = 0;
        for (Map.Entry<String, List<Bed>> e : byWard.entrySet()) {
            Label wardLbl = new Label(e.getKey());
            wardLbl.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:#94a3b8;");
            bedMapGrid.add(wardLbl, 0, row, 5, 1);
            row++;

            int col = 0;
            for (Bed bed : e.getValue()) {
                bedMapGrid.add(bedCell(bed), col, row);
                if (++col >= 5) { col = 0; row++; }
            }
            row++;
        }
    }

    private VBox bedCell(Bed bed) {
        boolean occupied = "OCCUPIED".equals(bed.getStatus());
        boolean selected = selectedBed != null && selectedBed.getBedId().equals(bed.getBedId());

        String bgColor  = selected ? "#dbeafe" : occupied ? "#ffedd5" : "#dcfce7";
        String bdrColor = selected ? "#3b82f6" : occupied ? "#f97316" : "#4ade80";
        String cursor   = occupied ? "default" : "hand";

        VBox cell = new VBox(2);
        cell.setAlignment(Pos.CENTER);
        cell.setPadding(new Insets(6));
        cell.setMinWidth(72); cell.setMaxWidth(72);
        cell.setMinHeight(66); cell.setMaxHeight(66);
        cell.setStyle("-fx-background-color:" + bgColor + "; -fx-border-color:" + bdrColor + ";" +
                      "-fx-border-radius:8; -fx-background-radius:8; -fx-cursor:" + cursor + ";");

        Label icon = new Label("🛏"); icon.setStyle("-fx-font-size:18px;");
        Label id   = new Label(bed.getBedId()); id.setStyle("-fx-font-size:10px; -fx-font-weight:bold; -fx-text-fill:#374151;");
        Label ward = new Label(bed.getWard());  ward.setStyle("-fx-font-size:9px; -fx-text-fill:#6b7280;");
        cell.getChildren().addAll(icon, id, ward);

        if (!occupied) {
            cell.setOnMouseClicked(ev -> {
                // Deselect old
                if (selectedBed != null) selectedBed = null;

                if (selected) {
                    // Toggle off
                    selectedBed = null;
                    selectedBedField.setText("No bed selected");
                } else {
                    selectedBed = bed;
                    selectedBedField.setText(bed.getBedId() + "  (" + bed.getWard() + ")");
                }
                renderBedMap(); // Re-render to show new selection
            });
        }
        return cell;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  WARD LOAD
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildWardLoadPanel() {
        VBox panel = card("📊  Ward Load");
        wardLoadContent = new VBox(10);
        wardLoadContent.getChildren().add(emptyLabel("No admitted patients"));
        panel.getChildren().add(wardLoadContent);
        return panel;
    }

    private void renderWardLoad(List<Object[]> wardCounts) {
        wardLoadContent.getChildren().clear();
        if (wardCounts.isEmpty()) {
            wardLoadContent.getChildren().add(emptyLabel("No admitted patients"));
            return;
        }
        Map<String, Long> counts = new LinkedHashMap<>();
        for (Object[] row : wardCounts) counts.put((String) row[0], (Long) row[1]);

        String[] wards = {"General", "ICU", "Pediatric", "Orthopedic"};
        int bedsPerWard = 5;

        for (String ward : wards) {
            long count = counts.getOrDefault(ward, 0L);
            double pct = (double) count / bedsPerWard;
            String barColor = pct > 0.8 ? "#ef4444" : pct > 0.5 ? "#f97316" : "#4ade80";

            VBox item = new VBox(4);
            HBox hdr = new HBox();
            hdr.setAlignment(Pos.CENTER_LEFT);
            Label wn = new Label(ward); wn.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#374151;");
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            Label cn = new Label(count + "/" + bedsPerWard); cn.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
            hdr.getChildren().addAll(wn, sp, cn);

            // Progress bar
            StackPane barTrack = new StackPane();
            barTrack.setMinHeight(6); barTrack.setMaxHeight(6);
            barTrack.setStyle("-fx-background-color:#e2e8f0; -fx-background-radius:3;");
            barTrack.setMaxWidth(Double.MAX_VALUE);

            double fillPct = pct;
            barTrack.widthProperty().addListener((ob, o, n) -> {
                if (barTrack.getChildren().isEmpty()) {
                    Rectangle fill = new Rectangle(n.doubleValue() * fillPct, 6, Color.web(barColor));
                    fill.setArcWidth(6); fill.setArcHeight(6);
                    barTrack.getChildren().add(fill);
                    StackPane.setAlignment(fill, Pos.CENTER_LEFT);
                }
            });

            item.getChildren().addAll(hdr, barTrack);
            wardLoadContent.getChildren().add(item);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  ADMISSIONS 7-DAY CHART
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildAdmissionsPanel() {
        VBox panel = card("📈  Admissions (7-Day)");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis   yAxis = new NumberAxis();
        yAxis.setMinorTickVisible(false);
        yAxis.setTickUnit(1);
        xAxis.setStyle("-fx-font-size:10px;");
        yAxis.setStyle("-fx-font-size:10px;");

        admissionsChart = new BarChart<>(xAxis, yAxis);
        admissionsChart.setLegendVisible(false);
        admissionsChart.setAnimated(true);
        admissionsChart.setPrefHeight(200);
        admissionsChart.setBarGap(2);
        admissionsChart.setCategoryGap(10);
        admissionsChart.setStyle("-fx-background-color:transparent;");

        panel.getChildren().add(admissionsChart);
        VBox.setVgrow(admissionsChart, Priority.ALWAYS);
        return panel;
    }

    private void renderChart(long[] counts) {
        admissionsChart.getData().clear();
        String[] labels = {"Mon","Tue","Wed","Thu","Fri","Sat","Today"};

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < 7; i++) {
            series.getData().add(new XYChart.Data<>(labels[i], counts[i]));
        }
        admissionsChart.getData().add(series);

        // Color bars blue
        series.getData().forEach(d -> {
            if (d.getNode() != null) d.getNode().setStyle("-fx-bar-fill:#3b82f6;");
            d.nodeProperty().addListener((ob, o, n) -> {
                if (n != null) n.setStyle("-fx-bar-fill:#3b82f6;");
            });
        });
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  SUMMARY
    // ═════════════════════════════════════════════════════════════════════════
    private VBox buildSummaryPanel() {
        VBox panel = card("📋  Summary");
        summaryContent = new VBox(8);
        summaryContent.getChildren().add(emptyLabel("Admit patients to see stats"));
        panel.getChildren().add(summaryContent);
        return panel;
    }

    private void renderSummary(long total, long admitted, long discharged, double avgAge, long available) {
        summaryContent.getChildren().clear();
        if (total == 0) {
            summaryContent.getChildren().add(emptyLabel("Admit patients to see stats"));
            return;
        }
        summaryContent.getChildren().addAll(
            summaryRow("Total Patients",     String.valueOf(total),       "#2563eb"),
            summaryRow("Currently Admitted", String.valueOf(admitted),    "#16a34a"),
            summaryRow("Discharged",         String.valueOf(discharged),  "#7c3aed"),
            summaryRow("Avg Patient Age",    String.format("%.0f yrs", avgAge), "#ea580c"),
            summaryRow("Beds Available",     String.valueOf(available),   "#0891b2")
        );
    }

    private HBox summaryRow(String lbl, String val, String color) {
        HBox row = new HBox();
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(6, 10, 6, 10));
        row.setBackground(new Background(new BackgroundFill(
            Color.web(color, 0.08), new CornerRadii(8), null)));

        Label l = new Label(lbl); l.setStyle("-fx-font-size:11px; -fx-text-fill:#374151;");
        Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
        Label v = new Label(val); v.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:" + color + ";");
        row.getChildren().addAll(l, sp, v);
        return row;
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  PATIENT RECORDS TABLE
    // ═════════════════════════════════════════════════════════════════════════
    @SuppressWarnings("unchecked")
    private VBox buildPatientRecordsPanel() {
        VBox panel = card("🗂  Patient Records");

        // Search + filter bar
        HBox topBar = new HBox(10);
        topBar.setAlignment(Pos.CENTER_LEFT);

        TextField search = new TextField();
        search.setPromptText("🔍  Search by name or ID…");
        search.setMinWidth(220);
        search.setStyle("-fx-background-color:#f8fafc; -fx-border-color:#e2e8f0;" +
            "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:8 12; -fx-font-size:12px;");
        search.textProperty().addListener((ob, o, n) -> {
            searchQuery = n;
            applyTableFilter();
        });

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

        ToggleGroup tg = new ToggleGroup();
        ToggleButton btnAll        = filterBtn("All",        tg);
        ToggleButton btnAdmitted   = filterBtn("Admitted",   tg);
        ToggleButton btnDischarged = filterBtn("Discharged", tg);
        btnAll.setSelected(true);

        tg.selectedToggleProperty().addListener((ob, o, n) -> {
            if (n != null) { currentFilter = ((ToggleButton) n).getText(); applyTableFilter(); }
        });

        topBar.getChildren().addAll(search, spacer, btnAll, btnAdmitted, btnDischarged);
        panel.getChildren().add(topBar);

        // Table
        TableView<Patient> table = new TableView<>(tableData);
        table.setPlaceholder(new Label("No records to display"));
        table.setMinHeight(200);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Patient,String>  colId   = strCol("Patient ID",   "patientId",   100);
        TableColumn<Patient,String>  colName = strCol("Name",         "name",        140);
        TableColumn<Patient,Integer> colAge  = new TableColumn<>("Age");
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colAge.setPrefWidth(50);
        TableColumn<Patient,String> colWard  = strCol("Ward",      "ward",          90);
        TableColumn<Patient,String> colBed   = strCol("Bed",       "bedId",         60);
        TableColumn<Patient,String> colDiag  = strCol("Diagnosis", "diagnosis",    150);
        TableColumn<Patient,String> colEntry = strCol("Entry",     "entryDateStr",  90);
        TableColumn<Patient,String> colExit  = strCol("Exit",      "exitDateStr",   90);

        TableColumn<Patient,String> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(90);
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        colStatus.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); return; }
                Label l = new Label(item);
                boolean admitted = "Admitted".equals(item);
                l.setStyle("-fx-text-fill:" + (admitted ? "#16a34a" : "#7c3aed") + ";" +
                    "-fx-background-color:" + (admitted ? "#dcfce7" : "#ede9fe") + ";" +
                    "-fx-background-radius:12; -fx-padding:2 8; -fx-font-size:11px; -fx-font-weight:bold;");
                setGraphic(l); setText(null);
            }
        });

        TableColumn<Patient, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(100);
        colAction.setCellFactory(tc -> new TableCell<>() {
            final Button btn = new Button("Discharge");
            { btn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white;" +
                "-fx-font-size:11px; -fx-padding:4 10; -fx-background-radius:6; -fx-cursor:hand;"); }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                Patient p = getTableView().getItems().get(getIndex());
                btn.setDisable("Discharged".equals(p.getStatus()));

                btn.setOnAction(e -> {
                    btn.setDisable(true);
                    Task<Patient> task = new Task<>() {
                        @Override protected Patient call() {
                            return patientService.dischargePatient(p.getPatientId());
                        }
                    };
                    task.setOnSucceeded(ev -> Platform.runLater(DashboardScreen.this::refreshAllData));
                    task.setOnFailed(ev -> Platform.runLater(() -> {
                        btn.setDisable(false);
                        new Alert(Alert.AlertType.ERROR,
                            "Discharge failed: " + task.getException().getMessage()).show();
                    }));
                    new Thread(task).start();
                });
                setGraphic(btn);
            }
        });

        table.getColumns().addAll(colId, colName, colAge, colWard, colBed,
                                  colDiag, colEntry, colExit, colStatus, colAction);
        panel.getChildren().add(table);
        VBox.setVgrow(table, Priority.ALWAYS);

        return panel;
    }

    private void applyTableFilter() {
        Task<List<Patient>> task = new Task<>() {
            @Override protected List<Patient> call() {
                List<Patient> all = searchQuery == null || searchQuery.isBlank()
                    ? patientService.getAllPatients()
                    : patientService.searchPatients(searchQuery);
                if ("Admitted".equals(currentFilter))
                    all.removeIf(p -> !"Admitted".equals(p.getStatus()));
                else if ("Discharged".equals(currentFilter))
                    all.removeIf(p -> !"Discharged".equals(p.getStatus()));
                return all;
            }
        };
        task.setOnSucceeded(e -> Platform.runLater(() -> {
            tableData.setAll(task.getValue());
        }));
        new Thread(task).start();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  REFRESH ALL DATA FROM MYSQL (background thread)
    // ═════════════════════════════════════════════════════════════════════════
    private void refreshAllData() {
        Task<Void> task = new Task<>() {
            // Loaded on background thread
            List<Bed>      beds;
            List<Patient>  patients;
            long           total, admitted, discharged, available;
            long           dischargedToday;
            double         avgAge;
            long[]         weekly = new long[7];
            List<Object[]> wardCounts;

            @Override
            protected Void call() {
                // ✅ All DB calls here — Spring services → Hibernate → MySQL
                beds           = bedService.getAllBeds();
                patients       = patientService.getAllPatients();
                total          = patients.size();
                admitted       = patients.stream().filter(p -> "Admitted".equals(p.getStatus())).count();
                discharged     = patients.stream().filter(p -> "Discharged".equals(p.getStatus())).count();
                available      = bedService.countAvailable();
                dischargedToday = patientService.countDischargedToday();
                avgAge         = patientService.averageAgeOfAdmitted();
                wardCounts     = patientService.countAdmittedByWard();

                // 7-day admissions (Mon–Today)
                LocalDate today = LocalDate.now();
                int dayOfWeek = today.getDayOfWeek().getValue(); // Mon=1 ... Sun=7
                for (int i = 0; i < 6; i++) {
                    LocalDate day = today.minusDays(dayOfWeek - 1 - i); // Mon → Sat
                    weekly[i] = patientService.countAdmissionsOnDate(day);
                }
                weekly[6] = patientService.countAdmissionsOnDate(today); // Today

                return null;
            }

            @Override
            protected void succeeded() {
                // ✅ All UI updates here — must be on JavaFX Application Thread
                Platform.runLater(() -> {
                    allBeds = beds;

                    // Stat cards
                    lblTotalBeds .setText(String.valueOf(bedService.totalBeds()));
                    lblAvailable .setText(String.valueOf(available));
                    lblOccupied  .setText(String.valueOf(bedService.countOccupied()));
                    lblDischarged.setText(String.valueOf(dischargedToday));

                    // Bed map
                    renderBedMap();

                    // Ward load
                    renderWardLoad(wardCounts);

                    // Chart
                    renderChart(weekly);

                    // Summary
                    renderSummary(total, admitted, discharged, avgAge, available);

                    // Table
                    applyTableFilter();
                });
            }
        };

        new Thread(task).start();
    }

    // ═════════════════════════════════════════════════════════════════════════
    //  HELPERS
    // ═════════════════════════════════════════════════════════════════════════
    private VBox card(String title) {
        VBox c = new VBox(10);
        c.setPadding(new Insets(16, 18, 16, 18));
        c.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(14), null)));
        c.setEffect(shadow(0.07, 12, 2));
        Label t = new Label(title);
        t.setStyle("-fx-font-size:13px; -fx-font-weight:bold; -fx-text-fill:#1e3a5f;");
        Separator sep = new Separator();
        c.getChildren().addAll(t, sep);
        return c;
    }

    private Label lbl(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px; -fx-font-weight:bold; -fx-text-fill:#374151;");
        return l;
    }

    private TextField inputField(String prompt, boolean readOnly) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setEditable(!readOnly);
        f.setStyle("-fx-background-color:" + (readOnly ? "#f0f4f8" : "#f8fafc") + ";" +
            "-fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8;" +
            "-fx-padding:8 12; -fx-font-size:12px;");
        f.setMinHeight(36);
        return f;
    }

    private TableColumn<Patient, String> strCol(String title, String prop, int width) {
        TableColumn<Patient, String> col = new TableColumn<>(title);
        col.setCellValueFactory(new PropertyValueFactory<>(prop));
        col.setPrefWidth(width);
        return col;
    }

    private ToggleButton filterBtn(String text, ToggleGroup tg) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(tg);
        String base = "-fx-background-color:#f1f5f9; -fx-border-color:#e2e8f0;" +
            "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:6 16;" +
            "-fx-font-size:12px; -fx-cursor:hand;";
        btn.setStyle(base);
        btn.selectedProperty().addListener((ob, o, n) ->
            btn.setStyle(n
                ? "-fx-background-color:#2563eb; -fx-text-fill:white; -fx-border-color:#2563eb;" +
                  "-fx-border-radius:8; -fx-background-radius:8; -fx-padding:6 16;" +
                  "-fx-font-size:12px; -fx-cursor:hand;"
                : base));
        return btn;
    }

    private HBox legendItem(String text, String color) {
        HBox box = new HBox(6);
        box.setAlignment(Pos.CENTER_LEFT);
        Rectangle r = new Rectangle(12, 12, Color.web(color));
        r.setArcWidth(4); r.setArcHeight(4);
        Label l = new Label(text); l.setStyle("-fx-font-size:11px; -fx-text-fill:#64748b;");
        box.getChildren().addAll(r, l);
        return box;
    }

    private Label emptyLabel(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size:12px; -fx-text-fill:#94a3b8;");
        return l;
    }

    private void setStatus(Label lbl, String msg, String color) {
        lbl.setText(msg);
        lbl.setStyle("-fx-font-size:12px; -fx-text-fill:" + color + ";");
    }

    private void hoverBtn(Button btn, String normal, String hover) {
        btn.setOnMouseEntered(e -> btn.setStyle(btn.getStyle().replace(normal, hover)));
        btn.setOnMouseExited (e -> btn.setStyle(btn.getStyle().replace(hover, normal)));
    }

    private DropShadow shadow(double opacity, double radius, double offsetY) {
        DropShadow ds = new DropShadow();
        ds.setColor(Color.web(String.format("#000000%02X", (int)(opacity * 255))));
        ds.setRadius(radius); ds.setOffsetY(offsetY);
        return ds;
    }

    private String extractColor(Label lbl) {
        String style = lbl.getStyle();
        int idx = style.indexOf("-fx-text-fill:") + 14;
        return style.substring(idx, idx + 7);
    }
}
