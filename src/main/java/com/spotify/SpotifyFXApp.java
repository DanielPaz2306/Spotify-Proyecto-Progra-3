package com.spotify;

import com.estructuras.ArbolAVL;
import com.estructuras.ArbolBinario;
import com.estructuras.Cancion;
import com.estructuras.Hash;
import com.estructuras.Historial;
import com.estructuras.Playlist;
import com.utilidades.Reproductor;
import com.utilidades.Estadisticas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.media.MediaPlayer;

public class SpotifyFXApp extends Application {

    private Stage mainStage;
    
    // Core logic instances
    public ArrayList<Playlist> playlists = new ArrayList<>();
    public Playlist playlistActual = new Playlist();
    public ArbolBinario arbol = ArbolBinario.getInstancia();
    public ArbolAVL arbolAvl = ArbolAVL.getInstancia();
    public Hash hashMaps = Hash.getInstancia();
    public Reproductor reproductor = new Reproductor();
    public Historial historial = Historial.getInstancia();
    
    public Playlist lista = new Playlist("GENERAL"); // Playlist general
    
    // UI Nodes
    private StackPane mainContentArea;
    private ListView<Playlist> sidebarPlaylistsListView;
    private TableView<Cancion> tblSongs;
    private TextField txtSearch;
    private Label lblStatus;
    
    // Playback Bar UI Nodes
    private Label lblTrackName;
    private Label lblTrackArtist;
    private Label lblPlaylistPlaying;
    private Button btnPlayPause;
    private Slider seekSlider;
    private Label lblCurrentTime;
    private Label lblTotalTime;
    private Slider volumeSlider;
    private ComboBox<String> cbModoReproduccion;

    // Navigation Buttons to track active states
    private Button btnBiblioteca;
    private Button btnHistorial;
    private Button btnEstadisticas;

    @Override
    public void start(Stage stage) {
        this.mainStage = stage;
        stage.setTitle("Sputify - Tu reproductor de música premium");
        
        // Add default GENERAL library to playlists
        lista.nombre = "GENERAL";
        playlists.add(lista);
        playlistActual = lista;

        // Initialize Player Listener
        reproductor.setListener(nuevaCancion -> {
            Platform.runLater(() -> actualizarReproductorUI());
        });

        // Main Layout Structure
        BorderPane mainLayout = new BorderPane();
        
        // 1. Sidebar (Left)
        VBox sidebar = crearSidebar();
        mainLayout.setLeft(sidebar);
        
        // 2. Central Content Pane (Center)
        mainContentArea = new StackPane();
        crearContentAreaCanciones();
        mainLayout.setCenter(mainContentArea);
        
        // 3. Playback Controls Bar (Bottom)
        HBox playbackBar = crearPlaybackBar();
        mainLayout.setBottom(playbackBar);
        
        // Scene setup
        Scene scene = new Scene(mainLayout, 1280, 780);
        scene.getStylesheets().add(getStylesheetUrl());
        
        stage.setScene(scene);
        stage.show();
        
        // Auto-refresh playlists in sidebar
        refreshPlaylistsUI();
        actualizarReproductorUI();
    }

    private VBox crearSidebar() {
        VBox sidebar = new VBox();
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(240);
        
        // Sputify Brand
        Label logo = new Label("Sputify");
        logo.getStyleClass().add("sidebar-title");
        
        // Section 1: Navigation
        Label lblNavHeader = new Label("Navegación");
        lblNavHeader.getStyleClass().add("sidebar-header");
        
        btnBiblioteca = new Button("Biblioteca General");
        btnBiblioteca.getStyleClass().addAll("sidebar-button", "sidebar-button-active");
        btnBiblioteca.setMaxWidth(Double.MAX_VALUE);
        btnBiblioteca.setOnAction(e -> {
            marcarBotonActivo(btnBiblioteca);
            sidebarPlaylistsListView.getSelectionModel().clearSelection();
            txtSearch.setText("");
            cargarPlaylistEnTabla(lista);
            mostrarPanelCanciones();
        });
        
        btnHistorial = new Button("Historial");
        btnHistorial.getStyleClass().add("sidebar-button");
        btnHistorial.setMaxWidth(Double.MAX_VALUE);
        btnHistorial.setOnAction(e -> {
            marcarBotonActivo(btnHistorial);
            sidebarPlaylistsListView.getSelectionModel().clearSelection();
            showHistorialView();
        });
        
        btnEstadisticas = new Button("Estadísticas");
        btnEstadisticas.getStyleClass().add("sidebar-button");
        btnEstadisticas.setMaxWidth(Double.MAX_VALUE);
        btnEstadisticas.setOnAction(e -> {
            marcarBotonActivo(btnEstadisticas);
            sidebarPlaylistsListView.getSelectionModel().clearSelection();
            showEstadisticasView();
        });
        
        // Section 2: Playlists
        Label lblPlaylistsHeader = new Label("Mis Playlists");
        lblPlaylistsHeader.getStyleClass().add("sidebar-header");
        
        sidebarPlaylistsListView = new ListView<>();
        sidebarPlaylistsListView.getStyleClass().add("sidebar-list");
        VBox.setVgrow(sidebarPlaylistsListView, Priority.ALWAYS);
        
        // Load selected playlist on click
        sidebarPlaylistsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                desmarcarTodosBotones();
                txtSearch.setText("");
                cargarPlaylistEnTabla(newVal);
                mostrarPanelCanciones();
            }
        });
        setupSidebarContextMenu();
        
        // Section 3: Playlists actions
        Button btnCrearPl = new Button("+ Crear Playlist");
        btnCrearPl.getStyleClass().add("sidebar-button");
        btnCrearPl.setMaxWidth(Double.MAX_VALUE);
        btnCrearPl.setOnAction(e -> showCrearPlaylistDialog());
        
        Button btnImportarPl = new Button("↓ Importar Playlist");
        btnImportarPl.getStyleClass().add("sidebar-button");
        btnImportarPl.setMaxWidth(Double.MAX_VALUE);
        btnImportarPl.setOnAction(e -> showImportarPlaylistDialog());
        
        // Section 4: Dev / Graph Tools
        Label lblGraphsHeader = new Label("Herramientas de Grafo");
        lblGraphsHeader.getStyleClass().add("sidebar-header");
        
        Button btnGraphAvl = new Button("AVL Graph");
        btnGraphAvl.getStyleClass().add("sidebar-button");
        btnGraphAvl.setMaxWidth(Double.MAX_VALUE);
        btnGraphAvl.setOnAction(e -> {
            try {
                renderizarGrafo(arbolAvl.toDOT(), "arbol_avl");
            } catch (Exception ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al compilar árbol AVL", ex.getMessage());
            }
        });
        
        Button btnGraphBinario = new Button("Binario Graph");
        btnGraphBinario.getStyleClass().add("sidebar-button");
        btnGraphBinario.setMaxWidth(Double.MAX_VALUE);
        btnGraphBinario.setOnAction(e -> {
            try {
                renderizarGrafo(arbol.toDOT(), "arbol_binario");
            } catch (Exception ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al compilar árbol Binario", ex.getMessage());
            }
        });
        
        sidebar.getChildren().addAll(
            logo, 
            lblNavHeader, btnBiblioteca, btnHistorial, btnEstadisticas, 
            lblPlaylistsHeader, sidebarPlaylistsListView, btnCrearPl, btnImportarPl,
            lblGraphsHeader, btnGraphAvl, btnGraphBinario
        );
        return sidebar;
    }

    private void crearContentAreaCanciones() {
        VBox pnl = new VBox();
        pnl.setSpacing(15);
        pnl.getStyleClass().add("main-content");
        
        // Topbar: Search Bar & Import Music
        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(20);
        
        // Styled Search Field
        StackPane searchContainer = new StackPane();
        txtSearch = new TextField();
        txtSearch.setPromptText("Buscar canción, artista o género...");
        txtSearch.getStyleClass().add("search-field");
        txtSearch.setPrefWidth(500);
        
        // In-place Search Icon using Label
        Label searchIcon = new Label("🔍");
        searchIcon.setStyle("-fx-text-fill: #727272; -fx-font-size: 14px; -fx-padding: 0 0 0 12;");
        searchContainer.getChildren().addAll(txtSearch, searchIcon);
        StackPane.setAlignment(searchIcon, Pos.CENTER_LEFT);
        
        // Add dynamic search listener
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filtrarCanciones(newVal));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button btnCargarMusica = new Button("Importar Música");
        btnCargarMusica.getStyleClass().add("action-button");
        btnCargarMusica.setOnAction(e -> cargarCanciones());
        
        topBar.getChildren().addAll(searchContainer, spacer, btnCargarMusica);
        
        // Search Stats Label
        lblStatus = new Label("Busca o carga canciones para iniciar.");
        lblStatus.getStyleClass().add("status-label");
        
        // Songs Table
        tblSongs = new TableView<>();
        tblSongs.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblSongs.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        VBox.setVgrow(tblSongs, Priority.ALWAYS);
        
        // Double-click row handler
        tblSongs.setRowFactory(tv -> {
            TableRow<Cancion> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && (!row.isEmpty())) {
                    Cancion seleccionada = row.getItem();
                    reproducirCancion(seleccionada);
                }
            });
            return row;
        });
        
        // Columns definitions
        TableColumn<Cancion, Number> indexCol = new TableColumn<>("#");
        indexCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        indexCol.setPrefWidth(40);
        indexCol.getStyleClass().add("index-cell");
        
        TableColumn<Cancion, String> tituloCol = new TableColumn<>("Título");
        tituloCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().nombre));
        tituloCol.setPrefWidth(220);

        TableColumn<Cancion, String> artistaCol = new TableColumn<>("Artista");
        artistaCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().artista));
        artistaCol.setPrefWidth(180);

        TableColumn<Cancion, String> albumCol = new TableColumn<>("Álbum");
        albumCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().album));
        albumCol.setPrefWidth(180);

        TableColumn<Cancion, String> generoCol = new TableColumn<>("Género");
        generoCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().genero));
        generoCol.setPrefWidth(120);

        TableColumn<Cancion, String> anoCol = new TableColumn<>("Año");
        anoCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().año));
        anoCol.setPrefWidth(80);

        TableColumn<Cancion, String> duracionCol = new TableColumn<>("Duración");
        duracionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().duracionReal));
        duracionCol.setPrefWidth(100);
        duracionCol.getStyleClass().add("duracion-cell");

        TableColumn<Cancion, Number> playsCol = new TableColumn<>("Reproducciones");
        playsCol.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().reproducciones));
        playsCol.setPrefWidth(120);
        
        tblSongs.getColumns().addAll(indexCol, tituloCol, artistaCol, albumCol, generoCol, anoCol, duracionCol, playsCol);
        setupSongTableContextMenu();
        
        pnl.getChildren().addAll(topBar, lblStatus, tblSongs);
        mainContentArea.getChildren().add(pnl);
    }

    private void mostrarPanelCanciones() {
        mainContentArea.getChildren().clear();
        crearContentAreaCanciones();
        cargarPlaylistEnTabla(playlistActual);
    }

    private HBox crearPlaybackBar() {
        HBox bar = new HBox();
        bar.getStyleClass().add("playback-bar");
        bar.setAlignment(Pos.CENTER);
        bar.setPrefHeight(90);
        
        // Left section: Song metadata
        VBox songMetadata = new VBox();
        songMetadata.setAlignment(Pos.CENTER_LEFT);
        songMetadata.setPrefWidth(300);
        
        lblTrackName = new Label("No hay reproducción activa");
        lblTrackName.getStyleClass().add("track-title");
        
        lblTrackArtist = new Label("");
        lblTrackArtist.getStyleClass().add("track-artist");
        
        HBox playlistPlayingLayout = new HBox();
        Label lblPlayingIcon = new Label("💽 ");
        lblPlayingIcon.setStyle("-fx-text-fill: #1DB954; -fx-font-size: 11px;");
        lblPlaylistPlaying = new Label("GENERAL");
        lblPlaylistPlaying.getStyleClass().add("track-artist");
        playlistPlayingLayout.getChildren().addAll(lblPlayingIcon, lblPlaylistPlaying);
        
        songMetadata.getChildren().addAll(lblTrackName, lblTrackArtist, playlistPlayingLayout);
        
        // Center section: Playback Controls & Progress bar
        VBox controlsLayout = new VBox();
        controlsLayout.setAlignment(Pos.CENTER);
        controlsLayout.setSpacing(8);
        HBox.setHgrow(controlsLayout, Priority.ALWAYS);
        
        HBox buttonsLayout = new HBox();
        buttonsLayout.setAlignment(Pos.CENTER);
        buttonsLayout.setSpacing(25);
        
        Button btnPrev = new Button("⏮");
        btnPrev.getStyleClass().add("playback-icon-btn");
        btnPrev.setOnAction(e -> {
            if (playlistActual == null || playlistActual.inicio == null) return;
            reproductor.setModo(cbModoReproduccion.getSelectionModel().getSelectedIndex() + 1);
            reproductor.retrocederAnterior(playlistActual);
            actualizarReproductorUI();
        });
        
        btnPlayPause = new Button("▶");
        btnPlayPause.getStyleClass().add("play-pause-btn");
        btnPlayPause.setOnAction(e -> {
            if (reproductor.getCancionActual() == null || reproductor.getCancionActual().ruta.isEmpty()) {
                // Play first song in list
                if (!tblSongs.getItems().isEmpty()) {
                    reproducirCancion(tblSongs.getItems().get(0));
                }
                return;
            }
            reproductor.setModo(cbModoReproduccion.getSelectionModel().getSelectedIndex() + 1);
            if (reproductor.estaReproduciendo()) {
                reproductor.pausar();
                btnPlayPause.setText("▶");
            } else {
                reproductor.reanudar();
                btnPlayPause.setText("⏸");
            }
        });
        
        Button btnNext = new Button("⏭");
        btnNext.getStyleClass().add("playback-icon-btn");
        btnNext.setOnAction(e -> {
            if (playlistActual == null || playlistActual.inicio == null) return;
            reproductor.setModo(cbModoReproduccion.getSelectionModel().getSelectedIndex() + 1);
            reproductor.avanzarSiguiente(playlistActual);
            actualizarReproductorUI();
        });
        
        cbModoReproduccion = new ComboBox<>();
        cbModoReproduccion.setItems(FXCollections.observableArrayList("Normal", "Aleatoria", "Infinita"));
        cbModoReproduccion.getSelectionModel().select(0);
        cbModoReproduccion.setStyle("-fx-background-color: #282828; -fx-text-fill: #ffffff; -fx-background-radius: 10; -fx-padding: 2 10;");
        cbModoReproduccion.setOnAction(e -> {
            int idx = cbModoReproduccion.getSelectionModel().getSelectedIndex();
            reproductor.setModo(idx + 1);
        });
        
        buttonsLayout.getChildren().addAll(cbModoReproduccion, btnPrev, btnPlayPause, btnNext);
        
        // Progress Slider
        HBox progressLayout = new HBox();
        progressLayout.setAlignment(Pos.CENTER);
        progressLayout.setSpacing(10);
        progressLayout.setMaxWidth(600);
        
        lblCurrentTime = new Label("0:00");
        lblCurrentTime.getStyleClass().add("playback-bar-time");
        
        seekSlider = new Slider(0, 100, 0);
        seekSlider.setPrefWidth(500);
        HBox.setHgrow(seekSlider, Priority.ALWAYS);
        
        // Bind slider adjustments to seek positions
        seekSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (seekSlider.isValueChanging()) {
                MediaPlayer player = reproductor.getPlayer();
                if (player != null) {
                    double total = player.getTotalDuration().toSeconds();
                    player.seek(Duration.seconds(newVal.doubleValue() / 100.0 * total));
                }
            }
        });
        seekSlider.setOnMouseReleased(e -> {
            MediaPlayer player = reproductor.getPlayer();
            if (player != null) {
                double total = player.getTotalDuration().toSeconds();
                player.seek(Duration.seconds(seekSlider.getValue() / 100.0 * total));
            }
        });
        
        lblTotalTime = new Label("0:00");
        lblTotalTime.getStyleClass().add("playback-bar-time");
        
        progressLayout.getChildren().addAll(lblCurrentTime, seekSlider, lblTotalTime);
        controlsLayout.getChildren().addAll(buttonsLayout, progressLayout);
        
        // Right section: Volume control
        HBox volumeLayout = new HBox();
        volumeLayout.setAlignment(Pos.CENTER_RIGHT);
        volumeLayout.setSpacing(10);
        volumeLayout.setPrefWidth(300);
        
        Label lblVolumeIcon = new Label("🔊");
        lblVolumeIcon.setStyle("-fx-text-fill: #b3b3b3; -fx-font-size: 14px;");
        
        volumeSlider = new Slider(0, 100, 80);
        volumeSlider.setPrefWidth(120);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            MediaPlayer player = reproductor.getPlayer();
            if (player != null) {
                player.setVolume(newVal.doubleValue() / 100.0);
            }
        });
        
        volumeLayout.getChildren().addAll(lblVolumeIcon, volumeSlider);
        
        bar.getChildren().addAll(songMetadata, controlsLayout, volumeLayout);
        return bar;
    }

    private void cargarPlaylistEnTabla(Playlist playlist) {
        playlistActual = playlist;
        if (playlist == null || playlist.EstaVacia()) {
            tblSongs.setItems(FXCollections.observableArrayList());
            return;
        }
        ArrayList<Cancion> array = playlist.getPlaylistEnArray(playlist);
        tblSongs.setItems(FXCollections.observableArrayList(array));
        
        // Highlight active track if currently playing from this playlist
        if (reproductor.playlistActual == playlist) {
            seleccionarCancionEnTabla(reproductor.getCancionActual());
        }
    }

    private void reproducirCancion(Cancion seleccionada) {
        if (seleccionada != null) {
            reproductor.detener();
            reproductor.setModo(cbModoReproduccion.getSelectionModel().getSelectedIndex() + 1);
            
            // If search bar is active and search yields a smaller subset of songs,
            // we configure a temporary list of search result items so the navigation
            // controls (Next/Prev) cycle within the search subset.
            if (!txtSearch.getText().trim().isEmpty()) {
                Playlist tempPlaylist = new Playlist("RESULTADOS_BUSQUEDA");
                for (Cancion c : tblSongs.getItems()) {
                    tempPlaylist.Insertar(c);
                }
                playlistActual = tempPlaylist;
            }
            
            reproductor.reproducir(seleccionada, playlistActual);
            actualizarReproductorUI();
        }
    }

    private void actualizarReproductorUI() {
        Cancion c = reproductor.getCancionActual();
        if (c != null && c.nombre != null && !c.nombre.isEmpty()) {
            lblTrackName.setText(c.nombre);
            lblTrackArtist.setText(c.artista + " - " + c.album);
            lblPlaylistPlaying.setText(playlistActual != null ? playlistActual.nombre : "GENERAL");
            btnPlayPause.setText("⏸");
            
            // Sync slider and times with the new Player
            javafx.scene.media.MediaPlayer player = reproductor.getPlayer();
            if (player != null) {
                player.setVolume(volumeSlider.getValue() / 100.0);
                
                player.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                    if (!seekSlider.isValueChanging()) {
                        double total = player.getTotalDuration().toSeconds();
                        if (total > 0) {
                            seekSlider.setValue(newTime.toSeconds() / total * 100);
                        }
                        lblCurrentTime.setText(formatDuration(newTime));
                    }
                });
                
                player.totalDurationProperty().addListener((obs, oldDur, newDur) -> {
                    lblTotalTime.setText(formatDuration(newDur));
                });
            }
        } else {
            lblTrackName.setText("No hay reproducción activa");
            lblTrackArtist.setText("");
            btnPlayPause.setText("▶");
            seekSlider.setValue(0);
            lblCurrentTime.setText("0:00");
            lblTotalTime.setText("0:00");
        }
        
        seleccionarCancionEnTabla(c);
    }

    private void seleccionarCancionEnTabla(Cancion cancion) {
        if (cancion == null || cancion.ruta == null) return;
        for (int i = 0; i < tblSongs.getItems().size(); i++) {
            Cancion c = tblSongs.getItems().get(i);
            if (c.ruta.equalsIgnoreCase(cancion.ruta)) {
                tblSongs.getSelectionModel().select(i);
                tblSongs.scrollTo(i);
                return;
            }
        }
    }

    private void filtrarCanciones(String textoBusqueda) {
        textoBusqueda = textoBusqueda.trim();
        if (textoBusqueda.isEmpty()) {
            cargarPlaylistEnTabla(playlistActual);
            lblStatus.setText("Búsqueda limpiada. Total canciones: " + tblSongs.getItems().size());
            return;
        }
        
        long itiempo = System.nanoTime();
        
        java.util.LinkedHashMap<String, Cancion> mapaResultados = new java.util.LinkedHashMap<>();
        
        // 1. AVL Name matches
        List<Cancion> resultadosAVL = new ArrayList<>();
        arbolAvl.buscarPorNombre(arbolAvl.raiz, textoBusqueda, resultadosAVL);
        for (Cancion c : resultadosAVL) {
            mapaResultados.put(c.ruta.toLowerCase(), c);
        }
        
        // 2. Hash Artista matches
        List<Cancion> resultadosArtista = hashMaps.buscarPorArtistaParcial(textoBusqueda);
        for (Cancion c : resultadosArtista) {
            mapaResultados.put(c.ruta.toLowerCase(), c);
        }
        
        // 3. Hash Genero matches
        List<Cancion> resultadosGenero = hashMaps.buscarPorGeneroParcial(textoBusqueda);
        for (Cancion c : resultadosGenero) {
            mapaResultados.put(c.ruta.toLowerCase(), c);
        }
        
        ObservableList<Cancion> itemsFiltrados = FXCollections.observableArrayList(mapaResultados.values());
        tblSongs.setItems(itemsFiltrados);
        
        long ftiempo = System.nanoTime();
        double tiempoFinal = (ftiempo - itiempo) / 1000000.0;
        
        lblStatus.setText("Búsqueda: '" + textoBusqueda + "' | Encontradas: " + itemsFiltrados.size() + " canciones en " + String.format("%.2f", tiempoFinal) + " ms");
    }

    private void cargarCanciones() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta de música");
        File carpeta = chooser.showDialog(mainStage);
        
        if (carpeta != null) {
            arbol = ArbolBinario.getInstancia();
            arbol.raiz = null;
            lista.limpiarLista();
            lista.contador = 0;
            arbol.cantidad = 0;
            arbolAvl.limpiar();
            
            long iArbolAVL = System.nanoTime();
            arbolAvl.insertarDesdeCarpeta(carpeta.getAbsolutePath());
            long fArbolAVL = System.nanoTime();
            double tiempoArbolAVL = (fArbolAVL - iArbolAVL) / 10000000.0;
            
            long iArbolBB = System.nanoTime();
            arbol.cargarConSub(carpeta.getAbsolutePath());
            long fArbolBB = System.nanoTime();
            double tiempoArbolBB = (fArbolBB - iArbolBB) / 10000000.0;
            
            arbol.inOrderAListaDoble(arbol.raiz, lista);
            lista.nombre = "GENERAL";
            playlistActual = lista;
            
            // Add default GENERAL library to playlists list if not present
            if (!playlists.contains(lista)) {
                playlists.add(lista);
            }
            
            refreshPlaylistsUI();
            
            List<Cancion> allSongs = lista.getPlaylistEnArray(lista);
            tblSongs.setItems(FXCollections.observableArrayList(allSongs));
            
            hashMaps.clasificarCanciones(allSongs);
            
            String status = "ArbolBB: " + arbol.cantidad + " canciones en " + String.format("%.2f", tiempoArbolBB) + " ms  |  "
                    + "ArbolAVL: " + arbolAvl.getContador() + " canciones en " + String.format("%.2f", tiempoArbolAVL) + " ms";
            lblStatus.setText(status);
            
            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Música cargada", "Canciones agregadas: " + arbol.cantidad);
        }
    }

    private void showHistorialView() {
        VBox pnl = new VBox();
        pnl.setSpacing(15);
        pnl.getStyleClass().add("main-content");
        
        Label title = new Label("Historial de Reproducción");
        title.getStyleClass().add("content-title");
        
        List<Cancion> listHistorial = historial.recorrerLista();
        
        TableView<Cancion> tblHistorial = new TableView<>();
        tblHistorial.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblHistorial.setItems(FXCollections.observableArrayList(listHistorial));
        
        TableColumn<Cancion, Number> indexCol = new TableColumn<>("#");
        indexCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(String.valueOf(getIndex() + 1));
                }
            }
        });
        indexCol.setPrefWidth(40);
        indexCol.getStyleClass().add("index-cell");
        
        TableColumn<Cancion, String> tituloCol = new TableColumn<>("Título");
        tituloCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().nombre));
        tituloCol.setPrefWidth(300);
        
        TableColumn<Cancion, String> artistaCol = new TableColumn<>("Artista");
        artistaCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().artista));
        artistaCol.setPrefWidth(200);

        TableColumn<Cancion, String> duracionCol = new TableColumn<>("Duración");
        duracionCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().duracionReal));
        duracionCol.setPrefWidth(120);
        duracionCol.getStyleClass().add("duracion-cell");
        
        tblHistorial.getColumns().addAll(indexCol, tituloCol, artistaCol, duracionCol);
        VBox.setVgrow(tblHistorial, Priority.ALWAYS);
        
        tblHistorial.setRowFactory(tv -> {
            TableRow<Cancion> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2 && (!row.isEmpty())) {
                    Cancion seleccionada = row.getItem();
                    reproductor.setModo(cbModoReproduccion.getSelectionModel().getSelectedIndex() + 1);
                    Playlist firstPl = playlists.isEmpty() ? lista : playlists.get(0);
                    reproductor.reproducir(seleccionada, firstPl);
                    playlistActual = firstPl;
                    actualizarReproductorUI();
                }
            });
            return row;
        });
        
        pnl.getChildren().addAll(title, tblHistorial);
        
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(pnl);
    }

    private void showEstadisticasView() {
        VBox pnl = new VBox();
        pnl.setSpacing(15);
        pnl.getStyleClass().add("main-content");
        
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);
        
        Label title = new Label("Estadísticas de Reproducción");
        title.getStyleClass().add("content-title");
        
        Button btnRefresh = new Button("Actualizar");
        btnRefresh.getStyleClass().add("action-button");
        btnRefresh.setOnAction(e -> {
            Estadisticas.cargarEstadisticas();
            showEstadisticasView();
        });
        
        header.getChildren().addAll(title, btnRefresh);
        
        // Load stats
        Estadisticas.cargarEstadisticas();
        Playlist generalPlaylist = null;
        for (Playlist pl : playlists) {
            if (pl.nombre != null && pl.nombre.equalsIgnoreCase("GENERAL")) {
                generalPlaylist = pl;
                break;
            }
        }
        
        GridPane grid = new GridPane();
        grid.getStyleClass().add("stats-grid");
        grid.setHgap(20);
        grid.setVgap(20);
        
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col1, col2);
        
        VBox cardCancion = crearTarjetaEstadistica("Canción más reproducida", Estadisticas.getCancionMasReproducida(generalPlaylist));
        VBox cardArtista = crearTarjetaEstadistica("Artista más escuchado", Estadisticas.getArtistaMasReproducido(generalPlaylist));
        VBox cardPlGrande = crearTarjetaEstadistica("Playlist más grande", Estadisticas.getPlaylistMasGrande(playlists));
        VBox cardPlLarga = crearTarjetaEstadistica("Playlist más larga", Estadisticas.getPlaylistMasLarga(playlists));
        VBox cardGenCant = crearTarjetaEstadistica("Género frecuente (Cant)", Estadisticas.getGeneroMasFrecuentePorCantidad(generalPlaylist));
        VBox cardGenRep = crearTarjetaEstadistica("Género frecuente (Rep)", Estadisticas.getGeneroMasFrecuentePorReproducciones(generalPlaylist));
        VBox cardPromedio = crearTarjetaEstadistica("Promedio de duración", Estadisticas.getPromedioDuracion(generalPlaylist));
        
        grid.add(cardCancion, 0, 0);
        grid.add(cardArtista, 1, 0);
        grid.add(cardPlGrande, 0, 1);
        grid.add(cardPlLarga, 1, 1);
        grid.add(cardGenCant, 0, 2);
        grid.add(cardGenRep, 1, 2);
        grid.add(cardPromedio, 0, 3, 2, 1);
        
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        
        pnl.getChildren().addAll(header, scroll);
        
        mainContentArea.getChildren().clear();
        mainContentArea.getChildren().add(pnl);
    }

    private VBox crearTarjetaEstadistica(String titulo, String valor) {
        VBox card = new VBox();
        card.getStyleClass().add("stat-card");
        
        Label lblTitulo = new Label(titulo);
        lblTitulo.getStyleClass().add("stat-title");
        
        Label lblValor = new Label(valor);
        lblValor.getStyleClass().add("stat-value");
        lblValor.setWrapText(true);
        
        card.getChildren().addAll(lblTitulo, lblValor);
        return card;
    }

    private void showCrearPlaylistDialog() {
        String nombre = mostrarInputDialog("Crear Playlist", "Nueva Playlist", "Ingrese el nombre de la playlist:", "");
        if (nombre != null && !nombre.trim().isEmpty()) {
            crearPlayList(nombre.trim());
        } else if (nombre != null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Nombre inválido", "El nombre no puede estar en blanco.");
        }
    }

    public void crearPlayList(String nombre) {
        Playlist nueva = new Playlist(nombre);
        playlists.add(nueva);
        refreshPlaylistsUI();
        mostrarAlerta(Alert.AlertType.INFORMATION, "PlayList", "Playlist creada", "Playlist '" + nueva.nombre + "' creada correctamente.");
    }

    private void showImportarPlaylistDialog() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccione el archivo de playlist encriptado");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos de texto (*.txt)", "*.txt"));
            File archivo = fileChooser.showOpenDialog(mainStage);
            
            if (archivo != null) {
                encriptador.Encriptador enc = new encriptador.Encriptador();
                Playlist importada = enc.desencriptarPlaylist(archivo);
                if (importada != null) {
                    boolean existe = false;
                    for (Playlist pl : playlists) {
                        if (pl.nombre.equalsIgnoreCase(importada.nombre)) {
                            existe = true;
                            break;
                        }
                    }
                    if (existe) {
                        importada.nombre = importada.nombre + "_importada";
                    }
                    
                    playlists.add(importada);
                    refreshPlaylistsUI();
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Playlist importada", "Playlist '" + importada.nombre + "' importada con éxito.");
                }
            }
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al importar la playlist", ex.getMessage());
        }
    }

    private void setupSidebarContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemRenombrar = new MenuItem("Renombrar...");
        MenuItem itemEliminar = new MenuItem("Eliminar");
        MenuItem itemExportar = new MenuItem("Exportar (Encriptar)...");
        
        contextMenu.getItems().addAll(itemRenombrar, itemEliminar, new SeparatorMenuItem(), itemExportar);
        
        itemRenombrar.setOnAction(e -> {
            Playlist seleccionada = sidebarPlaylistsListView.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                if (seleccionada.nombre.equals("GENERAL")) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Acción no permitida", "No se puede renombrar la playlist GENERAL.");
                    return;
                }
                String nuevoNombre = mostrarInputDialog("Renombrar Playlist", "Renombrar", "Nuevo nombre para la playlist:", seleccionada.nombre);
                if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                    seleccionada.nombre = nuevoNombre.trim();
                    sidebarPlaylistsListView.refresh();
                    if (lblPlaylistPlaying.getText().equals(seleccionada.nombre)) {
                        lblPlaylistPlaying.setText(nuevoNombre.trim());
                    }
                }
            }
        });
        
        itemEliminar.setOnAction(e -> {
            Playlist seleccionada = sidebarPlaylistsListView.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                if (seleccionada.nombre.equals("GENERAL")) {
                    mostrarAlerta(Alert.AlertType.WARNING, "Advertencia", "Acción no permitida", "No se puede eliminar la playlist GENERAL.");
                    return;
                }
                boolean confirm = mostrarConfirmDialog("Eliminar Playlist", "¿Está seguro?", "¿Desea eliminar la playlist '" + seleccionada.nombre + "'?");
                if (confirm) {
                    playlists.remove(seleccionada);
                    refreshPlaylistsUI();
                    if (playlistActual == seleccionada) {
                        cargarPlaylistEnTabla(lista);
                    }
                    mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Playlist eliminada", "Playlist eliminada correctamente.");
                }
            }
        });
        
        itemExportar.setOnAction(e -> {
            Playlist seleccionada = sidebarPlaylistsListView.getSelectionModel().getSelectedItem();
            if (seleccionada != null) {
                try {
                    DirectoryChooser dirChooser = new DirectoryChooser();
                    dirChooser.setTitle("Seleccione la carpeta donde guardar su archivo");
                    File carpeta = dirChooser.showDialog(mainStage);
                    if (carpeta != null) {
                        encriptador.Encriptador enc = new encriptador.Encriptador();
                        String ruta = enc.encriptarPlaylist(seleccionada, carpeta);
                        if (ruta != null) {
                            mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Playlist exportada", "Playlist exportada y encriptada exitosamente en:\n" + ruta);
                        }
                    }
                } catch (Exception ex) {
                    mostrarAlerta(Alert.AlertType.ERROR, "Error", "Error al exportar playlist", ex.getMessage());
                }
            }
        });
        
        sidebarPlaylistsListView.setContextMenu(contextMenu);
    }

    private void setupSongTableContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem itemCola = new MenuItem("Agregar a la cola de reproducción");
        Menu menuAgregarPl = new Menu("Agregar a playlist...");
        
        contextMenu.getItems().addAll(itemCola, menuAgregarPl);
        
        contextMenu.setOnShowing(e -> {
            menuAgregarPl.getItems().clear();
            List<Playlist> customPlaylists = playlists.stream()
                    .filter(p -> !p.nombre.equalsIgnoreCase("GENERAL"))
                    .collect(java.util.stream.Collectors.toList());
            
            if (customPlaylists.isEmpty()) {
                MenuItem itemVacio = new MenuItem("(No hay playlists creadas)");
                itemVacio.setDisable(true);
                menuAgregarPl.getItems().add(itemVacio);
            } else {
                for (Playlist pl : customPlaylists) {
                    MenuItem itemPl = new MenuItem(pl.nombre);
                    itemPl.setOnAction(evt -> {
                        ObservableList<Cancion> seleccionadas = tblSongs.getSelectionModel().getSelectedItems();
                        if (seleccionadas != null && !seleccionadas.isEmpty()) {
                            int agregadas = 0;
                            int yaExisten = 0;
                            for (Cancion cancion : seleccionadas) {
                                if (pl.existe(cancion)) {
                                    yaExisten++;
                                } else {
                                    pl.Insertar(cancion.nombre, cancion.artista, cancion.album, cancion.genero, cancion.duracionSeg, cancion.tamaño, cancion.ruta, cancion.año);
                                    agregadas++;
                                }
                            }
                            
                            contextMenu.hide();
                            
                            if (agregadas > 0 && yaExisten > 0) {
                                mostrarAlerta(Alert.AlertType.INFORMATION, "Información", "Canciones agregadas", "Se agregaron " + agregadas + " canciones a '" + pl.nombre + "'. (" + yaExisten + " ya existían).");
                            } else if (agregadas > 0) {
                                mostrarAlerta(Alert.AlertType.INFORMATION, "Éxito", "Canciones agregadas", "Se agregaron " + agregadas + " canciones a '" + pl.nombre + "' correctamente.");
                            } else if (yaExisten > 0) {
                                mostrarAlerta(Alert.AlertType.INFORMATION, "Información", "Ya existen", "Todas las canciones seleccionadas ya están en '" + pl.nombre + "'.");
                            }
                            sidebarPlaylistsListView.refresh();
                        }
                    });
                    menuAgregarPl.getItems().add(itemPl);
                }
            }
        });
        
        itemCola.setOnAction(e -> {
            ObservableList<Cancion> seleccionadas = tblSongs.getSelectionModel().getSelectedItems();
            if (seleccionadas != null && !seleccionadas.isEmpty()) {
                for (Cancion cancion : seleccionadas) {
                    reproductor.cola.encolar(cancion);
                }
                contextMenu.hide();
                mostrarAlerta(Alert.AlertType.INFORMATION, "Cola de Reproducción", "Canciones agregadas", "Se agregaron " + seleccionadas.size() + " canciones a la cola.");
            }
        });
        
        tblSongs.setContextMenu(contextMenu);
    }

    private void refreshPlaylistsUI() {
        List<Playlist> customPlaylists = playlists.stream()
                .filter(p -> !p.nombre.equalsIgnoreCase("GENERAL"))
                .collect(java.util.stream.Collectors.toList());
        sidebarPlaylistsListView.setItems(FXCollections.observableArrayList(customPlaylists));
    }

    private void marcarBotonActivo(Button activeBtn) {
        desmarcarTodosBotones();
        activeBtn.getStyleClass().add("sidebar-button-active");
    }

    private void desmarcarTodosBotones() {
        btnBiblioteca.getStyleClass().remove("sidebar-button-active");
        btnHistorial.getStyleClass().remove("sidebar-button-active");
        btnEstadisticas.getStyleClass().remove("sidebar-button-active");
    }

    public void renderizarGrafo(String dot, String nombreArchivo) {
        try {
            String dotFile = nombreArchivo + ".dot";
            String pngFile = nombreArchivo + ".png";
            java.nio.file.Files.writeString(java.nio.file.Path.of(dotFile), dot);
            
            Process p = Runtime.getRuntime().exec(new String[]{"dot", "-Tpng", dotFile, "-o", pngFile});
            int exitCode = p.waitFor();
            
            if (exitCode != 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error de Graphviz", "Graphviz terminó con código: " + exitCode, "Por favor, verifica el formato del árbol.");
                return;
            }
            
            File imgFile = new File(pngFile);
            if (!imgFile.exists() || imgFile.length() == 0) {
                mostrarAlerta(Alert.AlertType.ERROR, "Error", "No se pudo generar la imagen del grafo", "El archivo está vacío o no existe.");
                return;
            }
            
            Stage stage = new Stage();
            stage.setTitle(nombreArchivo + " (Arrastra con el scroll, click para tamaño)");
            
            ImageView imageView = new ImageView(new Image(imgFile.toURI().toString()));
            imageView.setPreserveRatio(true);
            
            ScrollPane scroll = new ScrollPane(imageView);
            scroll.setPannable(true);
            scroll.setStyle("-fx-background: #191414; -fx-background-color: #191414;");
            
            // Toggle Scale/Fit to screen on click
            imageView.setOnMouseClicked(e -> {
                if (imageView.getFitWidth() > 0) {
                    imageView.setFitWidth(0);
                    imageView.setFitHeight(0);
                } else {
                    imageView.setFitWidth(stage.getWidth() - 30);
                    imageView.setFitHeight(stage.getHeight() - 50);
                }
            });
            
            Scene scene = new Scene(scroll, 900, 650);
            stage.setScene(scene);
            stage.show();
            
        } catch (Exception ex) {
            mostrarAlerta(Alert.AlertType.ERROR, "Error de Ejecución de Graphviz", "No se pudo ejecutar Graphviz ('dot')", 
                "Asegúrate de tener instalado Graphviz y agregar su binario al PATH del sistema.\nDetalle: " + ex.getMessage());
        }
    }

    // --- Dialog Helper Methods ---
    
    private String getStylesheetUrl() {
        try {
            java.net.URL url = getClass().getResource("style.css");
            if (url != null) {
                return url.toExternalForm();
            }
        } catch (Exception e) {
            // ignore
        }
        return new File("src/main/java/com/spotify/style.css").toURI().toString();
    }
    
    private String mostrarInputDialog(String titulo, String cabecera, String contenido, String valorInicial) {
        TextInputDialog dialog = new TextInputDialog(valorInicial);
        dialog.setTitle(titulo);
        dialog.setHeaderText(cabecera);
        dialog.setContentText(contenido);
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getStylesheetUrl());
        dialogPane.getStyleClass().add("dialog-pane");
        
        java.util.Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private boolean mostrarConfirmDialog(String titulo, String cabecera, String contenido) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);
        alert.setContentText(contenido);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getStylesheetUrl());
        dialogPane.getStyleClass().add("dialog-pane");
        
        java.util.Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String cabecera, String contenido) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(cabecera);
        alert.setContentText(contenido);
        
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.getStylesheets().add(getStylesheetUrl());
        dialogPane.getStyleClass().add("dialog-pane");
        
        alert.show();
    }

    private String formatDuration(Duration duration) {
        if (duration == null || duration.isUnknown()) return "0:00";
        int seconds = (int) Math.floor(duration.toSeconds());
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%d:%02d", minutes, remainingSeconds);
    }
}
