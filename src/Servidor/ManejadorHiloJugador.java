package Servidor;

import java.net.*;
import java.io.*;

public class ManejadorHiloJugador extends Thread {

    private DataInputStream in;
    private DataOutputStream out;
    private Socket socket;
    private ServidorJuego servidorJuego;
    private String nombreJugador;

    public ManejadorHiloJugador(Socket socket, ServidorJuego servidorJuego) {
        this.socket = socket;
        this.servidorJuego = servidorJuego;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out  = new DataOutputStream(socket.getOutputStream());

            String mensaje;
            while (!(mensaje = in.readUTF()).isEmpty()) {
                System.out.println("Mensaje recibido: " + mensaje);
                procesarMensaje(mensaje);
            }
        } catch (EOFException e) {
            System.out.println("EOF: " + nombreJugador);
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    // Identifica el tipo de mensaje y lo procesa, ya sea un mensaje de conexión o de Hit ya estando en el juego
    private void procesarMensaje(String mensaje) throws IOException {
        String[] partes = mensaje.split(":");

        switch (partes[0]) {
            case "LOGIN":
                procesarLogin(partes[1]);
                break;
            case "HIT":
                procesarHit(partes);
                break;
            default:
                System.out.println("Mensaje desconocido: " + mensaje);
        }
    }

    // Registra al jugador en ServidorJuego y responde con un "Login" al jugador con su score
    private void procesarLogin(String nombre) throws IOException {
        this.nombreJugador = nombre;
        int score = servidorJuego.registrarJugadorHash(nombre);
        out.writeUTF("OK:" + score);
        System.out.println("Login exitoso: " + nombre + " | Score: " + score);
    }

    // Manda el hit a ServidorJuego para validarlo y responde HIT_OK o HIT_FAIL
    private void procesarHit(String[] partes) throws IOException {
        int fila = Integer.parseInt(partes[1]);
        int col  = Integer.parseInt(partes[2]);

        boolean hitValido = servidorJuego.procesarHit(nombreJugador, fila, col);
        if (hitValido) {
            int score = servidorJuego.getScore(nombreJugador);
            out.writeUTF("HIT_VALIDO:" + score);
        } else {
            out.writeUTF("HIT_INVALIDO");
        }
    }
}
