/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.spotify;


import com.estructuras.ArbolAVL;
import java.io.File;
import javax.swing.JFileChooser;
import com.estructuras.ArbolBinario;
import com.estructuras.Cancion;
import com.estructuras.Hash;
import com.estructuras.Historial;
import com.estructuras.Playlist;
import com.frames.CrearPlaylist;

import com.frames.HistorialForm;
import com.utilidades.Reproductor;

import java.util.List;
import java.util.ArrayList;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;




public class Inicio extends javax.swing.JFrame {
    
    
    public ArrayList<Playlist> playlists = new ArrayList();
    
    public Playlist playlistActual = new Playlist();
    public ArbolBinario arbol = ArbolBinario.getInstancia();
    public DefaultListModel<Cancion> modeloListaCanciones = new DefaultListModel<>();
    public DefaultListModel<Playlist> modeloListaPlaylist = new DefaultListModel<>();
    public ArbolAVL arbolAvl = ArbolAVL.getInstancia();
    public Hash hashMaps = Hash.getInstancia();
    public int modo;
    
    public Reproductor reproductor = new Reproductor();
    
    public Historial historial = Historial.getInstancia();
    
    Playlist lista = new Playlist();

    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Inicio.class.getName());
   


    public Inicio() {
        
        initComponents();
        
        modo = modoReproduccionCombo.getSelectedIndex();
        
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filtrarCanciones(txtBuscar.getText());
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filtrarCanciones(txtBuscar.getText());
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filtrarCanciones(txtBuscar.getText());
            }
        });
        
        listadoCanciones.setModel(modeloListaCanciones);
        

        listadoCanciones.addMouseListener(new java.awt.event.MouseAdapter() /*PARA CONTROLAR EL DOBLE CLICK EN LA LISTA*/ {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    
                    Cancion seleccionada = listadoCanciones.getSelectedValue();
                    if (seleccionada != null) {
                        reproductor.detener();
                        reproductor.setModo(modoReproduccionCombo.getSelectedIndex() + 1);
                        
                        Playlist seleccionadaPl = listadoPlaylist.getSelectedValue();
                        if (seleccionadaPl != null && txtBuscar.getText().trim().isEmpty()) {
                            playlistActual = seleccionadaPl;
                        } else {
                            playlistActual = lista;
                            listadoPlaylist.clearSelection();
                        }
                        playlistEnReproduccionLbl.setText(playlistActual.nombre);
                        
                        reproductor.reproducir(seleccionada, playlistActual); 
                    }
                    else{
                        JOptionPane.showMessageDialog(null, "No has seleccionado ninguna cancion", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }); 

        // 1. Doble clic en listadoPlaylist para reproducir
        listadoPlaylist.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    Playlist seleccionada = listadoPlaylist.getSelectedValue();
                    if (seleccionada != null) {
                        if (seleccionada.inicio == null) {
                            JOptionPane.showMessageDialog(null, "La playlist no puede estar vacía", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        playlistActual = seleccionada;
                        cargarPlaylistEnModelo(seleccionada, modeloListaCanciones);
                        reproductor.setModo(modoReproduccionCombo.getSelectedIndex() + 1);
                        reproductor.reproducir(seleccionada.inicio, seleccionada);
                        playlistEnReproduccionLbl.setText(seleccionada.nombre);
                    }
                }
            }
        });

        // 2. Menu contextual (clic derecho) para listadoPlaylist (Renombrar/Eliminar)
        javax.swing.JPopupMenu popupPlaylist = new javax.swing.JPopupMenu();
        javax.swing.JMenuItem itemRenombrar = new javax.swing.JMenuItem("Renombrar...");
        javax.swing.JMenuItem itemEliminar = new javax.swing.JMenuItem("Eliminar");
        javax.swing.JMenuItem itemExportar = new javax.swing.JMenuItem("Exportar (Encriptar)...");
        
        popupPlaylist.add(itemRenombrar);
        popupPlaylist.add(itemEliminar);
        popupPlaylist.add(itemExportar);

        itemExportar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Playlist seleccionada = listadoPlaylist.getSelectedValue();
                if (seleccionada != null) {
                    try {
                        encriptador.Encriptador enc = new encriptador.Encriptador();
                        String ruta = enc.encriptarPlaylist(seleccionada);
                        if (ruta != null) {
                            JOptionPane.showMessageDialog(null, "Playlist exportada y encriptada exitosamente en:\n" + ruta, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error al exportar playlist: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        

        itemRenombrar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Playlist seleccionada = listadoPlaylist.getSelectedValue();
                if (seleccionada != null) {
                    if (seleccionada.nombre.equals("GENERAL")) {
                        JOptionPane.showMessageDialog(null, "No se puede renombrar la playlist GENERAL", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String nuevoNombre = JOptionPane.showInputDialog(null, "Nuevo nombre para la playlist:", seleccionada.nombre);
                    if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                        seleccionada.nombre = nuevoNombre.trim();
                        listadoPlaylist.repaint();
                        if (playlistEnReproduccionLbl.getText().equals(seleccionada.nombre)) {
                            playlistEnReproduccionLbl.setText(nuevoNombre.trim());
                        }
                    }
                }
            }
        });

        itemEliminar.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                Playlist seleccionada = listadoPlaylist.getSelectedValue();
                if (seleccionada != null) {
                    if (seleccionada.nombre.equals("GENERAL")) {
                        JOptionPane.showMessageDialog(null, "No se puede eliminar la playlist GENERAL", "Advertencia", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int confirm = JOptionPane.showConfirmDialog(null, "¿Seguro que desea eliminar la playlist '" + seleccionada.nombre + "'?", "Eliminar Playlist", JOptionPane.YES_NO_OPTION);
                    if (confirm == JOptionPane.YES_OPTION) {
                        playlists.remove(seleccionada);
                        modeloListaPlaylist.removeElement(seleccionada);
                        JOptionPane.showMessageDialog(null, "Playlist eliminada correctamente");
                    }
                }
            }
        });
        

        listadoPlaylist.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                mostrarPopup(e);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                mostrarPopup(e);
            }
            private void mostrarPopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = listadoPlaylist.locationToIndex(e.getPoint());
                    if (index != -1) {
                        listadoPlaylist.setSelectedIndex(index);
                        popupPlaylist.show(listadoPlaylist, e.getX(), e.getY());
                    }
                }
            }
        });

        // 3. Menu contextual (clic derecho) para listadoCanciones (Agregar a playlist)
        javax.swing.JPopupMenu popupCanciones = new javax.swing.JPopupMenu();
        javax.swing.JMenu submenuAgregar = new javax.swing.JMenu("Agregar a playlist...");
        javax.swing.JMenuItem itemAgregarCola = new javax.swing.JMenuItem("Agregar a la cola de reproduccion");
        popupCanciones.add(submenuAgregar);
        popupCanciones.add(itemAgregarCola);

        itemAgregarCola.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                java.util.List<Cancion> seleccionadas = listadoCanciones.getSelectedValuesList();
                if (seleccionadas != null && !seleccionadas.isEmpty()) {
                    for (Cancion cancion : seleccionadas) {
                        reproductor.cola.encolar(cancion);
                    }
                    JOptionPane.showMessageDialog(null, "Se agregaron " + seleccionadas.size() + " canciones a la cola de reproducción.");
                }
            }
        });

        listadoCanciones.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                mostrarPopup(e);
            }
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                mostrarPopup(e);
            }
            private void mostrarPopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int index = listadoCanciones.locationToIndex(e.getPoint());
                    if (index != -1) {
                        if (!listadoCanciones.isSelectedIndex(index)) {
                            listadoCanciones.setSelectedIndex(index);
                        }
                        
                        java.util.List<Cancion> seleccionadas = listadoCanciones.getSelectedValuesList();
                        if (seleccionadas != null && !seleccionadas.isEmpty()) {
                            submenuAgregar.removeAll();
                            
                            for (Playlist pl : playlists) {
                                if (pl.nombre.equals("GENERAL")) continue;
                                
                                javax.swing.JMenuItem itemPl = new javax.swing.JMenuItem(pl.nombre);
                                itemPl.addActionListener(new java.awt.event.ActionListener() {
                                    @Override
                                    public void actionPerformed(java.awt.event.ActionEvent evt) {
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
                                        
                                        if (agregadas > 0 && yaExisten > 0) {
                                            JOptionPane.showMessageDialog(null, "Se agregaron " + agregadas + " canciones a '" + pl.nombre + "'. (" + yaExisten + " ya existían).");
                                        } else if (agregadas > 0) {
                                            JOptionPane.showMessageDialog(null, "Se agregaron " + agregadas + " canciones a '" + pl.nombre + "' correctamente.");
                                        } else if (yaExisten > 0) {
                                            JOptionPane.showMessageDialog(null, "Todas las canciones seleccionadas ya están en '" + pl.nombre + "'.", "Información", JOptionPane.INFORMATION_MESSAGE);
                                        }
                                        
                                        listadoPlaylist.repaint();
                                    }
                                });
                                submenuAgregar.add(itemPl);
                            }
                            
                            if (submenuAgregar.getItemCount() == 0) {
                                javax.swing.JMenuItem vacio = new javax.swing.JMenuItem("(No hay playlists creadas)");
                                vacio.setEnabled(false);
                                submenuAgregar.add(vacio);
                            }
                            
                            popupCanciones.show(listadoCanciones, e.getX(), e.getY());
                        }
                    }
                }
            }
        });

        reproductor.setListener(new com.utilidades.ReproductorListener() {
            @Override
            public void onCancionCambiada(Cancion nuevaCancion) {
                SwingUtilities.invokeLater(() -> seleccionarCancionEnLista(nuevaCancion));
            }
        });

        modoReproduccionCombo.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int index = modoReproduccionCombo.getSelectedIndex();
                reproductor.setModo(index + 1);
            }
        });
        
        reproductor.setModo(modoReproduccionCombo.getSelectedIndex() + 1);
        
        // 4. Botón de Estadísticas programático
        javax.swing.JButton estadisticasBtn = new javax.swing.JButton("ESTADISTICAS");
        estadisticasBtn.setFont(new java.awt.Font("Gontserrat", 0, 12));
        estadisticasBtn.setText("ESTADISTICAS");
        estadisticasBtn.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                com.frames.EstadisticasForm f = new com.frames.EstadisticasForm(Inicio.this);
                f.setAlwaysOnTop(true);
                f.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
                f.setVisible(true);
            }
        });
        jPanel1.add(estadisticasBtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 230, 150, -1));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        spotifyLabel = new javax.swing.JLabel();
        agregarButton = new javax.swing.JButton();
        playlistButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        listadoCanciones = new javax.swing.JList<>();
        txtBuscar = new javax.swing.JTextField();
        playButton = new javax.swing.JButton();
        anteriorButton = new javax.swing.JButton();
        siguienteButton = new javax.swing.JButton();
        arbolTxt = new javax.swing.JLabel();
        misplaylistLbl = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        listadoPlaylist = new javax.swing.JList<>();
        reproduccionLbl = new javax.swing.JLabel();
        playlistEnReproduccionLbl = new javax.swing.JLabel();
        generalButton = new javax.swing.JButton();
        modoReproduccionCombo = new javax.swing.JComboBox<>();
        historialButton = new javax.swing.JButton();
        importarPlaylistButton = new javax.swing.JButton();
        graphBinario = new javax.swing.JButton();
        graphAvl = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(51, 51, 51));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        spotifyLabel.setFont(new java.awt.Font("Gontserrat SemiBold", 0, 24)); // NOI18N
        spotifyLabel.setForeground(new java.awt.Color(0, 255, 51));
        spotifyLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        spotifyLabel.setText("Sputify");
        jPanel1.add(spotifyLabel, new org.netbeans.lib.awtextra.AbsoluteConstraints(160, 20, 1020, -1));

        agregarButton.setFont(new java.awt.Font("Gontserrat", 0, 12)); // NOI18N
        agregarButton.setText("Importar Musica");
        agregarButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agregarButtonActionPerformed(evt);
            }
        });
        jPanel1.add(agregarButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 130, 30));

        playlistButton.setFont(new java.awt.Font("Gontserrat", 0, 12)); // NOI18N
        playlistButton.setForeground(new java.awt.Color(0, 204, 51));
        playlistButton.setText("Crear Playlist");
        playlistButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                playlistButtonMouseClicked(evt);
            }
        });
        playlistButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playlistButtonActionPerformed(evt);
            }
        });
        jPanel1.add(playlistButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 660, 140, 30));

        listadoCanciones.setBackground(new java.awt.Color(102, 102, 102));
        listadoCanciones.setFont(new java.awt.Font("Gontserrat", 0, 14)); // NOI18N
        listadoCanciones.setForeground(new java.awt.Color(255, 255, 255));
        listadoCanciones.setModel(modeloListaCanciones);
        listadoCanciones.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        listadoCanciones.setSelectionForeground(new java.awt.Color(51, 51, 51));
        jScrollPane1.setViewportView(listadoCanciones);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 140, 920, 510));

        txtBuscar.setFont(new java.awt.Font("Gontserrat", 0, 12)); // NOI18N
        jPanel1.add(txtBuscar, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 100, 920, -1));

        playButton.setBackground(new java.awt.Color(204, 204, 204));
        playButton.setFont(new java.awt.Font("Gontserrat", 0, 12)); // NOI18N
        playButton.setForeground(new java.awt.Color(0, 0, 0));
        playButton.setText("Play/Pause");
        playButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                playButtonActionPerformed(evt);
            }
        });
        jPanel1.add(playButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(500, 660, -1, -1));

        anteriorButton.setBackground(new java.awt.Color(204, 204, 204));
        anteriorButton.setFont(new java.awt.Font("Gontserrat", 0, 12)); // NOI18N
        anteriorButton.setForeground(new java.awt.Color(0, 0, 0));
        anteriorButton.setText("Anterior");
        anteriorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anteriorButtonActionPerformed(evt);
            }
        });
        jPanel1.add(anteriorButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(360, 660, -1, -1));

        siguienteButton.setBackground(new java.awt.Color(204, 204, 204));
        siguienteButton.setFont(new java.awt.Font("Gontserrat", 0, 12)); // NOI18N
        siguienteButton.setForeground(new java.awt.Color(0, 0, 0));
        siguienteButton.setText("Siguiente");
        siguienteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                siguienteButtonActionPerformed(evt);
            }
        });
        jPanel1.add(siguienteButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 660, -1, -1));

        arbolTxt.setFont(new java.awt.Font("Gontserrat", 0, 12)); // NOI18N
        arbolTxt.setText("Tiempos: ");
        jPanel1.add(arbolTxt, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 820, -1, -1));

        misplaylistLbl.setFont(new java.awt.Font("Gontserrat", 0, 12)); // NOI18N
        misplaylistLbl.setText("Mis Playlists:");
        jPanel1.add(misplaylistLbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 110, -1, -1));

        listadoPlaylist.setBackground(new java.awt.Color(102, 102, 102));
        listadoPlaylist.setModel(modeloListaPlaylist);
        listadoPlaylist.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPane2.setViewportView(listadoPlaylist);

        jPanel1.add(jScrollPane2, new org.netbeans.lib.awtextra.AbsoluteConstraints(1140, 140, 160, 510));

        reproduccionLbl.setFont(new java.awt.Font("Gontserrat SemiBold", 0, 18)); // NOI18N
        reproduccionLbl.setForeground(new java.awt.Color(0, 204, 51));
        reproduccionLbl.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        reproduccionLbl.setText("Reproducción: ");
        jPanel1.add(reproduccionLbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 60, 150, 30));

        playlistEnReproduccionLbl.setFont(new java.awt.Font("Gontserrat", 2, 18)); // NOI18N
        playlistEnReproduccionLbl.setForeground(new java.awt.Color(255, 255, 255));
        playlistEnReproduccionLbl.setText(reproductor.cancionActual.nombre);
        jPanel1.add(playlistEnReproduccionLbl, new org.netbeans.lib.awtextra.AbsoluteConstraints(190, 66, 170, 20));

        generalButton.setText("BIBLIOTECA GENERAL");
        generalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generalButtonActionPerformed(evt);
            }
        });
        jPanel1.add(generalButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 150, 150, -1));

        modoReproduccionCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Normal", "Aleatoria", "Infinita" }));
        modoReproduccionCombo.setToolTipText("");
        modoReproduccionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modoReproduccionComboActionPerformed(evt);
            }
        });
        jPanel1.add(modoReproduccionCombo, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 660, -1, -1));

        historialButton.setText("HISTORIAL");
        historialButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                historialButtonActionPerformed(evt);
            }
        });
        jPanel1.add(historialButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(960, 190, 150, -1));

        importarPlaylistButton.setFont(new java.awt.Font("Gontserrat", 0, 12)); // NOI18N
        importarPlaylistButton.setText("Importar Playlist");
        importarPlaylistButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importarPlaylistButtonActionPerformed(evt);
            }
        });
        jPanel1.add(importarPlaylistButton, new org.netbeans.lib.awtextra.AbsoluteConstraints(1150, 700, 140, 30));

        graphBinario.setText("Binario Graph");
        graphBinario.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphBinarioActionPerformed(evt);
            }
        });
        jPanel1.add(graphBinario, new org.netbeans.lib.awtextra.AbsoluteConstraints(1190, 830, -1, -1));

        graphAvl.setText("AVL Graph");
        graphAvl.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graphAvlActionPerformed(evt);
            }
        });
        jPanel1.add(graphAvl, new org.netbeans.lib.awtextra.AbsoluteConstraints(1090, 830, -1, -1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1342, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 862, Short.MAX_VALUE)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 862, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void playlistButtonMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_playlistButtonMouseClicked
        
    }//GEN-LAST:event_playlistButtonMouseClicked

    private void agregarButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agregarButtonActionPerformed
            cargarCanciones();
    }//GEN-LAST:event_agregarButtonActionPerformed

    private void filtrarCanciones(String textoBusqueda) {
        textoBusqueda = textoBusqueda.trim();
        
        if (textoBusqueda.isEmpty()) {  
            Playlist seleccionada = listadoPlaylist.getSelectedValue();
            if (seleccionada == null && !playlists.isEmpty()) {
                seleccionada = playlists.get(0);
            }
            if (seleccionada != null) {
                cargarPlaylistEnModelo(seleccionada, modeloListaCanciones);
            }
            arbolTxt.setText("Búsqueda limpiada. Total canciones: " + modeloListaCanciones.getSize());
            return;
        }
        
        long itiempo = System.nanoTime();
        
        // mapa para eliminar duplicados manteniendo orden
        java.util.LinkedHashMap<String, Cancion> mapaResultados = new java.util.LinkedHashMap<>();
        
        
        DefaultListModel<Cancion> resultadosAVL = new DefaultListModel<>();
        arbolAvl.buscarPorNombre(arbolAvl.raiz, textoBusqueda, resultadosAVL);
        for (int i = 0; i < resultadosAVL.getSize(); i++) {
            Cancion c = resultadosAVL.getElementAt(i);
            mapaResultados.put(c.ruta.toLowerCase(), c);
        }
        
        
        DefaultListModel<Cancion> resultadosArtista = hashMaps.buscarPorArtistaParcial(textoBusqueda);
        for (int i = 0; i < resultadosArtista.getSize(); i++) {
            Cancion c = resultadosArtista.getElementAt(i);
            mapaResultados.put(c.ruta.toLowerCase(), c);
        }
        
        
        DefaultListModel<Cancion> resultadosGenero = hashMaps.buscarPorGeneroParcial(textoBusqueda);
        for (int i = 0; i < resultadosGenero.getSize(); i++) {
            Cancion c = resultadosGenero.getElementAt(i);
            mapaResultados.put(c.ruta.toLowerCase(), c);
        }
        
        
        modeloListaCanciones.clear();
        for (Cancion c : mapaResultados.values()) {
            modeloListaCanciones.addElement(c);
        }
        
        long ftiempo = System.nanoTime();
        long tiempoNano = ftiempo - itiempo;
        double tiempoFinal = tiempoNano / 1_000_000.0;
        
        arbolTxt.setText("Búsqueda: '" + textoBusqueda + "' | Encontradas: " + modeloListaCanciones.getSize() + " canciones en " + tiempoFinal + " ms");
    }

    private void playButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playButtonActionPerformed
        
        if(reproductor.estaReproduciendo() == true){
            reproductor.setModo(modoReproduccionCombo.getSelectedIndex() + 1);
            reproductor.pausar();
        }    
        else{
            reproductor.setModo(modoReproduccionCombo.getSelectedIndex() + 1);
            reproductor.reanudar();
        }
    }//GEN-LAST:event_playButtonActionPerformed

    private void siguienteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_siguienteButtonActionPerformed
        if (playlistActual == null || playlistActual.inicio == null) return;
        
        reproductor.setModo(modoReproduccionCombo.getSelectedIndex() + 1);
        reproductor.avanzarSiguiente(playlistActual);
        
        seleccionarCancionEnLista(reproductor.getCancionActual());
    }//GEN-LAST:event_siguienteButtonActionPerformed

    private void anteriorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anteriorButtonActionPerformed
        if (playlistActual == null || playlistActual.inicio == null) return;
        
        reproductor.setModo(modoReproduccionCombo.getSelectedIndex() + 1);
        reproductor.retrocederAnterior(playlistActual);
        
        seleccionarCancionEnLista(reproductor.getCancionActual());
    }//GEN-LAST:event_anteriorButtonActionPerformed

    private void playlistButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_playlistButtonActionPerformed
        CrearPlaylist c = new CrearPlaylist(this);
        c.setAlwaysOnTop(true);
        c.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        c.setVisible(true);
    }//GEN-LAST:event_playlistButtonActionPerformed

    private void importarPlaylistButtonActionPerformed(java.awt.event.ActionEvent evt) {
        try {
            encriptador.Encriptador enc = new encriptador.Encriptador();
            Playlist importada = enc.desencriptarPlaylist();
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
                modeloListaPlaylist.addElement(importada);
                JOptionPane.showMessageDialog(this, "Playlist '" + importada.nombre + "' importada con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al importar la playlist: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generalButtonActionPerformed
        ArbolBinario arbol = ArbolBinario.getInstancia();
        
        listadoPlaylist.clearSelection();
        txtBuscar.setText("");
        modeloListaCanciones.clear();
        
        List<Cancion> temp = new ArrayList<>();
        arbol.inOrderALista(arbol.raiz, temp); //CARGA AL JLIST
        for(Cancion c : temp) {
            modeloListaCanciones.addElement(c);
        }
        playlistActual = lista;
    }//GEN-LAST:event_generalButtonActionPerformed

    private void modoReproduccionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modoReproduccionComboActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_modoReproduccionComboActionPerformed

    private void historialButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_historialButtonActionPerformed
        DefaultListModel<Cancion> listaHistorial = historial.recorrerLista();
        HistorialForm e = new HistorialForm(this, listaHistorial);
        e.setAlwaysOnTop(true);
        e.setVisible(true);
        e.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }//GEN-LAST:event_historialButtonActionPerformed

    private void graphAvlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphAvlActionPerformed
        
        try {
            renderizar(arbolAvl.toDOT(), "arbol_avl");
        } catch (Exception ex) {
            System.getLogger(Inicio.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
    }//GEN-LAST:event_graphAvlActionPerformed

    private void graphBinarioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_graphBinarioActionPerformed
        try {
            renderizar(arbol.toDOT(), "arbol_binario");
        } catch (Exception ex) {
            System.getLogger(Inicio.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }//GEN-LAST:event_graphBinarioActionPerformed


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new Inicio().setVisible(true));
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton agregarButton;
    private javax.swing.JButton anteriorButton;
    private javax.swing.JLabel arbolTxt;
    private javax.swing.JButton generalButton;
    private javax.swing.JButton graphAvl;
    private javax.swing.JButton graphBinario;
    private javax.swing.JButton historialButton;
    private javax.swing.JButton importarPlaylistButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JList<Cancion> listadoCanciones;
    public javax.swing.JList<Playlist> listadoPlaylist;
    private javax.swing.JLabel misplaylistLbl;
    public javax.swing.JComboBox<String> modoReproduccionCombo;
    private javax.swing.JButton playButton;
    private javax.swing.JButton playlistButton;
    public javax.swing.JLabel playlistEnReproduccionLbl;
    private javax.swing.JLabel reproduccionLbl;
    private javax.swing.JButton siguienteButton;
    private javax.swing.JLabel spotifyLabel;
    private javax.swing.JTextField txtBuscar;
    // End of variables declaration//GEN-END:variables

    private void buscarCancion(String textoBusqueda) {
        if (textoBusqueda.isEmpty()) {
            return;
        }

        for (int i = 0; i < modeloListaCanciones.getSize(); i++) {
            
            Cancion c = modeloListaCanciones.getElementAt(i);

            if (c.nombre.toLowerCase().contains(textoBusqueda.toLowerCase()) || c.artista.toLowerCase().contains(textoBusqueda.toLowerCase())) {

                listadoCanciones.setSelectedIndex(i);     
                listadoCanciones.ensureIndexIsVisible(i);   
                return;
            }
        }

        javax.swing.JOptionPane.showMessageDialog(this, "Canción no encontrada", "Búsqueda", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    private void seleccionarCancionEnLista(Cancion cancion) {
        if (cancion == null || cancion.ruta == null) return;
        for (int i = 0; i < modeloListaCanciones.getSize(); i++) {
            Cancion c = modeloListaCanciones.getElementAt(i);
            if (c.ruta.equalsIgnoreCase(cancion.ruta)) {
                listadoCanciones.setSelectedIndex(i);
                listadoCanciones.ensureIndexIsVisible(i);
                return;
            }
        }
    }

    
    private void cargarCanciones(){
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int resultado = chooser.showOpenDialog(null);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                File carpeta = chooser.getSelectedFile();

                arbol = ArbolBinario.getInstancia();
                arbol.raiz = null;      //LIMPIAMOS EL ARBOL ANTES DE VOLVER A CARGAR
                lista.limpiarLista();
                lista.contador = 0;
                arbol.cantidad = 0;     // Y LA CANTIDAD TAMBIEN
                arbolAvl.limpiar();     // LIMPIAMOS EL AVL TAMBIEN
                
                long iArbolAVL = System.nanoTime();
                arbolAvl.insertarDesdeCarpeta(carpeta.getAbsolutePath());
                long fArbolAVL = System.nanoTime();
                
                long tiempoNano = fArbolAVL - iArbolAVL;
                
                double tiempoArbolAVL = tiempoNano / 1_000_0000.0;
                

                long iArbolBB = System.nanoTime();
                arbol.cargarConSub(carpeta.getAbsolutePath()); //CARGA ARBOL
                long fArbolBB = System.nanoTime();
                
                tiempoNano = fArbolBB - iArbolBB;
                
                double tiempoArbolBB =  tiempoNano / 1_000_0000.0;
                
                arbol.inOrderAListaDoble(arbol.raiz, lista); //CARGA PLAYLIST GENERAL
                
                lista.nombre = "GENERAL";
                
                
                
                playlistActual = lista;
                
                playlists.add(lista);

                modeloListaPlaylist.clear();
                modeloListaCanciones.clear();    // LIMPIAMOS EL MODELO DEL HJSLIT
                modeloListaPlaylist.addElement(lista);
                List<Cancion> temp = new ArrayList<>();
                arbol.inOrderALista(arbol.raiz, temp); //CARGA AL JLIST
                for(Cancion c : temp) {
                    modeloListaCanciones.addElement(c);
                }
                
                hashMaps.clasificarCanciones(modeloListaCanciones);

                arbolTxt.setText("ArbolBB: " + arbol.cantidad + " canciones en " + tiempoArbolBB + " ms  |  "
                        + "ArbolAVL: " + arbolAvl.getContador() + " canciones en " + tiempoArbolAVL + " ms");
                
                JOptionPane.showMessageDialog(this, "Canciones agregadas: " + arbol.cantidad);
            }
    }
    
    
    public void crearPlayList(String nombre, List<Cancion> canciones){
        Playlist nueva = new Playlist(nombre);
        
        for(Cancion c : canciones){
            nueva.Insertar(c.nombre, c.artista, c.album, c.genero, c.duracionSeg, c.tamaño, c.ruta, c.año);
        }
        
        JOptionPane.showMessageDialog(null, "Playlist " + nueva.nombre + " creada correctamente con " + nueva.contador + " canciones", "PlayList", JOptionPane.INFORMATION_MESSAGE);
        playlists.add(nueva);
        modeloListaPlaylist.addElement(nueva);
       
    }
    
    public void crearPlayList(String nombre){
        Playlist nueva = new Playlist(nombre);
        
        
        JOptionPane.showMessageDialog(null, "Playlist " + nueva.nombre + " creada correctamente con " + nueva.contador + " canciones", "PlayList", JOptionPane.INFORMATION_MESSAGE);
        playlists.add(nueva);
        modeloListaPlaylist.addElement(nueva);
       
    }
    
    public void cargarPlaylistEnModelo(Playlist playlist, DefaultListModel<Cancion> modelo){
        if(modelo != null){
            modelo.clear();
        }
        
        if(playlist == null || playlist.inicio == null){
            JOptionPane.showMessageDialog(null, "La playlist no puede estar vacia", "ADVERTENCIA", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        Cancion actual = playlist.inicio;
        
        do{
            modelo.addElement(actual);
            actual = actual.siguiente;
        }
        while(actual != playlist.inicio);
    }
    
    public static void renderizar(String dot, String nombreArchivo) throws Exception {
        String dotFile = nombreArchivo + ".dot";
        String pngFile = nombreArchivo + ".png";
        java.nio.file.Files.writeString(java.nio.file.Path.of(dotFile), dot);

        try {
            // Llama a Graphviz (debe estar instalado en el sistema)
            Process p = Runtime.getRuntime().exec(new String[]{"dot", "-Tpng", dotFile, "-o", pngFile});
            int exitCode = p.waitFor();
            
            if (exitCode != 0) {
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "Graphviz terminó con código de error: " + exitCode + "\nPor favor, verifica el formato del árbol.", 
                    "Error de Graphviz", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            java.io.File imgFile = new java.io.File(pngFile);
            if (!imgFile.exists() || imgFile.length() == 0) {
                javax.swing.JOptionPane.showMessageDialog(null, 
                    "No se pudo generar la imagen del grafo (el archivo está vacío o no existe).", 
                    "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Carga la imagen
            java.awt.image.BufferedImage imagen = javax.imageio.ImageIO.read(imgFile);
            
            // Crea el visor interactivo (Ajuste automático / Tamaño real al hacer clic)
            ImagePanel imagePanel = new ImagePanel(imagen);
            javax.swing.JScrollPane scroll = new javax.swing.JScrollPane(imagePanel);
            scroll.setBorder(null);

            javax.swing.JFrame frame = new javax.swing.JFrame(nombreArchivo + " (Haz clic para alternar tamaño)");
            frame.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            frame.add(scroll);
            frame.setSize(900, 650);  // tamaño inicial de la ventana
            frame.setLocationRelativeTo(null);  // centrado en pantalla
            frame.setVisible(true);
            
        } catch (java.io.IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null, 
                "No se pudo ejecutar Graphviz ('dot').\nAsegúrate de tener instalado Graphviz y de agregar la ruta de su binario al PATH del sistema.\nDetalle: " + e.getMessage(), 
                "Error de Ejecución de Graphviz", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class ImagePanel extends javax.swing.JPanel {
        private final java.awt.Image originalImage;
        private boolean fitToScreen = true;

        public ImagePanel(java.awt.Image image) {
            this.originalImage = image;
            this.setBackground(new java.awt.Color(25, 20, 20)); // Negro Spotify
            
            // Alternar vista entre ajustar a pantalla y tamaño real con clic
            this.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    fitToScreen = !fitToScreen;
                    revalidate();
                    repaint();
                }
            });
            
            // Cursor de mano para indicar que es interactivo
            this.setCursor(java.awt.Cursor.getPredefinedCursor(java.awt.Cursor.HAND_CURSOR));
            this.setToolTipText("Haz clic para alternar entre Ajustar a Pantalla y Tamaño Real");
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            if (originalImage == null) {
                return new java.awt.Dimension(100, 100);
            }
            
            if (fitToScreen) {
                java.awt.Container parent = getParent();
                if (parent instanceof javax.swing.JViewport) {
                    return parent.getSize();
                }
                return new java.awt.Dimension(800, 600);
            } else {
                return new java.awt.Dimension(originalImage.getWidth(null), originalImage.getHeight(null));
            }
        }

        @Override
        protected void paintComponent(java.awt.Graphics g) {
            super.paintComponent(g);
            if (originalImage == null) return;

            java.awt.Graphics2D g2d = (java.awt.Graphics2D) g;
            // Configurar antialiasing e interpolación bilineal para mejor calidad
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION, java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);

            int imgW = originalImage.getWidth(null);
            int imgH = originalImage.getHeight(null);

            if (fitToScreen) {
                int panelW = getWidth();
                int panelH = getHeight();

                double scaleX = (double) panelW / imgW;
                double scaleY = (double) panelH / imgH;
                double scale = Math.min(scaleX, scaleY);
                
                // Evitamos estirar la imagen si es pequeña
                if (scale > 1.0) {
                    scale = 1.0;
                }

                int newW = (int) (imgW * scale);
                int newH = (int) (imgH * scale);

                int x = (panelW - newW) / 2;
                int y = (panelH - newH) / 2;

                g2d.drawImage(originalImage, x, y, newW, newH, null);
            } else {
                int panelW = getWidth();
                int panelH = getHeight();
                int x = Math.max(0, (panelW - imgW) / 2);
                int y = Math.max(0, (panelH - imgH) / 2);
                g2d.drawImage(originalImage, x, y, null);
            }
        }
    }
}
