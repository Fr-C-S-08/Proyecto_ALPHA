import Jugadores.ConexionJugador;
import Jugadores.TopicoJugador;
import Jugadores.VisualJugador;

void main() {
    String nombreJugador = "Jugador2";
    VisualJugador ventana = new VisualJugador(nombreJugador);

    TopicoJugador topico = new TopicoJugador(ventana);
    topico.iniciar();

    // Primero mostrar la ventana
    javax.swing.SwingUtilities.invokeLater(() -> {
        ventana.setVisible(true);

        // Conectar y vincular DESPUÉS de que la ventana esté visible
        ConexionJugador conexion = new ConexionJugador(nombreJugador, ventana);
        conexion.conectar();
        ventana.setConexionJugador(conexion);
    });
}
