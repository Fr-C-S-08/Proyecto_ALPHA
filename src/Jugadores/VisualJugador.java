package Jugadores;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class VisualJugador extends JFrame{
    private final JCheckBox[] monstruo = new JCheckBox[9];
    private final JLabel Puntaje;
    public String NombreJugador;
    private ConexionJugador conexionJugador;

    public VisualJugador(String NombreJugador){
        this.NombreJugador = NombreJugador;

        setTitle("Juego Alpha" + NombreJugador);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10,10));
        setResizable(true);

        //Aquí se declara el grid del juego 3x3
        JPanel grid = new JPanel(new GridLayout(3,3,5,5));
        grid.setBorder(BorderFactory.createTitledBorder("EspacioJuego"));

        for(int i = 0; i < 9; i++){
            final int index = i;
            monstruo[i] = new JCheckBox("Hoyo " + (i + 1));
            monstruo[i].setEnabled(false);
            monstruo[i].setHorizontalAlignment(SwingConstants.CENTER);
            monstruo[i].addActionListener((ActionEvent e) -> {
                monstruo[index].setSelected(false); // revertir visualmente
                int row = index / 3;
                int col = index % 3;
                MonstruoGolpeado(row, col);
            });
            grid.add(monstruo[i]);
        }

        // Panel inferior con puntaje
        JPanel panel_puntaje = new JPanel(new FlowLayout(FlowLayout.CENTER));
        Puntaje = new JLabel("Puntaje:");
        Puntaje.setFont(new Font("Arial", Font.BOLD, 16));
        panel_puntaje.add(Puntaje);

        add(grid, BorderLayout.CENTER);
        add(panel_puntaje, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
    }

    //La activación se manda desde el servidor
    public void MostrarMonstruo(int row, int col) {
        SwingUtilities.invokeLater(() -> {
            monstruo[row * 3 + col].setEnabled(true);
            monstruo[row * 3 + col].setSelected(false);
        });
    }

    //El monstruo desaparece (TIMEOUT) iogual se manda desde el servidor
    public void OcultarMonstruo(int row, int col) {
        SwingUtilities.invokeLater(() -> {
            monstruo[row * 3 + col].setSelected(false);
            monstruo[row * 3 + col].setEnabled(false);
        });
    }

    public void PuntajeActual(int n) {
        SwingUtilities.invokeLater(() -> Puntaje.setText("Puntaje: " + n));
    }

    public void MostrarGanador(String nombreGanador) {
        SwingUtilities.invokeLater(() -> {
            String msg = nombreGanador.equals(NombreJugador)
                    ? "¡Ganaste!"
                    : nombreGanador + " ha ganado.";
            JOptionPane.showMessageDialog(this, msg, "Fin de ronda",
                    JOptionPane.INFORMATION_MESSAGE);
            resetBoard();
        });
    }

    public void setConexionJugador(ConexionJugador conexionJugador) {
        this.conexionJugador = conexionJugador;
    }

    private void MonstruoGolpeado(int row, int col) {
        if (conexionJugador != null) {
            conexionJugador.enviarHit(row, col);
        }
    }

    private void resetBoard() {
        for (JCheckBox hoyo : monstruo) {
            hoyo.setSelected(false);
            hoyo.setEnabled(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VisualJugador("Jugador1").setVisible(true));
    }


}
