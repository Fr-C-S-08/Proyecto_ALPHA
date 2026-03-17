package Jugadores;

import java.net.*;
import java.io.*;
import javax.swing.SwingUtilities;

public class ConexionJugador {

    private static final int puerto = 49152;
    private static final String servidor = "localhost";

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private VisualJugador ventana;
    private String nombreJugador;

    public ConexionJugador(String nombreJugador, VisualJugador ventana) {
        this.nombreJugador = nombreJugador;
        this.ventana       = ventana;
    }

    // Aquí se establece la conexión y el login
    public void conectar() {
        try {
            socket = new Socket(servidor, puerto);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // Manda login al servidor
            out.writeUTF("LOGIN:" + nombreJugador);

            // Espera respuesta del servidor
            String[] partes = in.readUTF().split(":");
            if (partes[0].equals("OK")) {
                int score = Integer.parseInt(partes[1]);
                SwingUtilities.invokeLater(() -> ventana.PuntajeActual(score));
                System.out.println("Login exitoso: " + nombreJugador + " | Score: " + score);
            }

        } catch (UnknownHostException e) {
            System.out.println("Sock: " + e.getMessage());
        } catch (EOFException e) {
            System.out.println("EOF: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    // Aquí se establece el envío de un hit en el grid
    public void enviarHit(int fila, int col) {
        try {
            out.writeUTF("HIT:" + fila + ":" + col);

            // Espera respuesta del servidor
            String[] partes = in.readUTF().split(":");
            if (partes[0].equals("HIT_VALIDO")) {
                int nuevoScore = Integer.parseInt(partes[1]);
                ventana.PuntajeActual(nuevoScore);
                System.out.println("Hit válido en [" + fila + "][" + col + "] Score: " + nuevoScore);
            } else {
                System.out.println("Hit inválido en [" + fila + "][" + col + "]");
            }

        } catch (EOFException e) {
            System.out.println("EOF: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    public void desconectar() {
        if (socket != null) try {
            socket.close();
        } catch (IOException e) {
            System.out.println("close: " + e.getMessage());
        }
    }
}
