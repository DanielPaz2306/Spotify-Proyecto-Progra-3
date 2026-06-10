/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.frames;

import com.estructuras.Playlist;
import com.spotify.Inicio;
import com.utilidades.Estadisticas;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *
 * @author pchin
 */
public class EstadisticasForm extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(EstadisticasForm.class.getName());
    private Inicio inicio;
    
    // Etiquetas para mostrar los valores de las estadísticas
    private JLabel lblCancionVal;
    private JLabel lblArtistaVal;
    private JLabel lblPlGrandeVal;
    private JLabel lblPlLargaVal;
    private JLabel lblGenCantVal;
    private JLabel lblGenRepVal;
    private JLabel lblPromedioVal;

    /**
     * Creates new form EstadisticasForm
     */
    public EstadisticasForm() {
        initComponents();
    }

    public EstadisticasForm(Inicio inicio) {
        this.inicio = inicio;
        initComponents();
        personalizarDiseño();
        actualizarEstadisticas();
    }

    private void personalizarDiseño() {
        setTitle("Estadísticas de Reproducción");
        getContentPane().removeAll();
        getContentPane().setBackground(new Color(25, 20, 20)); // Negro Spotify
        
        // Layout principal
        setLayout(new BorderLayout(15, 15));
        
        // Panel Superior: Título
        JPanel pnlHeader = new JPanel();
        pnlHeader.setBackground(new Color(25, 20, 20));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        JLabel lblTitulo = new JLabel("ESTADÍSTICAS DE REPRODUCCIÓN");
        lblTitulo.setFont(new Font("Montserrat", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(30, 215, 96)); // Verde Spotify
        pnlHeader.add(lblTitulo);
        add(pnlHeader, BorderLayout.NORTH);
        
        // Panel Central: Grid de Estadísticas
        JPanel pnlGrid = new JPanel();
        pnlGrid.setBackground(new Color(40, 40, 40)); // Gris oscuro
        pnlGrid.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(10, 20, 10, 20),
            BorderFactory.createLineBorder(new Color(60, 60, 60), 1)
        ));
        
        pnlGrid.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        int row = 0;
        row = agregarFilaEstadistica(pnlGrid, gbc, row, "Canción más reproducida:", lblCancionVal = crearLabelValor());
        row = agregarFilaEstadistica(pnlGrid, gbc, row, "Artista más escuchado:", lblArtistaVal = crearLabelValor());
        row = agregarFilaEstadistica(pnlGrid, gbc, row, "Playlist más grande:", lblPlGrandeVal = crearLabelValor());
        row = agregarFilaEstadistica(pnlGrid, gbc, row, "Playlist más larga:", lblPlLargaVal = crearLabelValor());
        row = agregarFilaEstadistica(pnlGrid, gbc, row, "Género frecuente (Cant):", lblGenCantVal = crearLabelValor());
        row = agregarFilaEstadistica(pnlGrid, gbc, row, "Género frecuente (Rep):", lblGenRepVal = crearLabelValor());
        row = agregarFilaEstadistica(pnlGrid, gbc, row, "Promedio de duración:", lblPromedioVal = crearLabelValor());
        
        JScrollPane scrollPane = new JScrollPane(pnlGrid);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(25, 20, 20));
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel Inferior: Botones
        JPanel pnlButtons = new JPanel();
        pnlButtons.setBackground(new Color(25, 20, 20));
        pnlButtons.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        pnlButtons.setLayout(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        
        JButton btnActualizar = new JButton("Actualizar");
        btnActualizar.setFont(new Font("Montserrat", Font.BOLD, 13));
        btnActualizar.setBackground(new Color(30, 215, 96)); // Verde Spotify
        btnActualizar.setForeground(Color.WHITE);
        btnActualizar.setFocusPainted(false);
        btnActualizar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnActualizar.addActionListener(e -> actualizarEstadisticas());
        
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Montserrat", Font.BOLD, 13));
        btnCerrar.setBackground(new Color(80, 80, 80));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btnCerrar.addActionListener(e -> dispose());
        
        pnlButtons.add(btnActualizar);
        pnlButtons.add(btnCerrar);
        add(pnlButtons, BorderLayout.SOUTH);
        
        setSize(620, 480);
        setLocationRelativeTo(inicio);
    }

    private int agregarFilaEstadistica(JPanel pnl, GridBagConstraints gbc, int row, String titulo, JLabel valLabel) {
        JLabel lblTitle = new JLabel(titulo);
        lblTitle.setFont(new Font("Montserrat", Font.BOLD, 13));
        lblTitle.setForeground(new Color(30, 215, 96)); // Verde Spotify
        
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.35;
        pnl.add(lblTitle, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.65;
        pnl.add(valLabel, gbc);
        
        return row + 1;
    }
    
    private JLabel crearLabelValor() {
        JLabel lbl = new JLabel("N/A");
        lbl.setFont(new Font("Montserrat", Font.PLAIN, 13));
        lbl.setForeground(Color.WHITE);
        return lbl;
    }

    public void actualizarEstadisticas() {
        if (inicio == null) return;
        
        // Recargar las estadísticas desde el archivo plano
        Estadisticas.cargarEstadisticas();
        
        // Buscar la playlist GENERAL
        Playlist generalPlaylist = null;
        for (Playlist pl : inicio.playlists) {
            if (pl.nombre != null && pl.nombre.equalsIgnoreCase("GENERAL")) {
                generalPlaylist = pl;
                break;
            }
        }
        
        lblCancionVal.setText(Estadisticas.getCancionMasReproducida(generalPlaylist));
        lblArtistaVal.setText(Estadisticas.getArtistaMasReproducido(generalPlaylist));
        lblPlGrandeVal.setText(Estadisticas.getPlaylistMasGrande(inicio.playlists));
        lblPlLargaVal.setText(Estadisticas.getPlaylistMasLarga(inicio.playlists));
        lblGenCantVal.setText(Estadisticas.getGeneroMasFrecuentePorCantidad(generalPlaylist));
        lblGenRepVal.setText(Estadisticas.getGeneroMasFrecuentePorReproducciones(generalPlaylist));
        lblPromedioVal.setText(Estadisticas.getPromedioDuracion(generalPlaylist));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        java.awt.EventQueue.invokeLater(() -> new EstadisticasForm().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

