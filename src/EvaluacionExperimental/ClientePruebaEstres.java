package EvaluacionExperimental;

import java.net.*;
import java.io.*;

public class ClientePruebaEstres extends Thread {

    private static final int    puerto      = 49152;
    private static final String servidor    = "localhost";
    private static final int    pausa_hit   = 100;

    private String             nombreJugador;
    private ResultadosMetricas resultados;

    public ClientePruebaEstres(String nombreJugador, ResultadosMetricas resultados) {
        this.nombreJugador = nombreJugador;
        this.resultados    = resultados;
    }

    @Override
    public void run() {
        Socket           socket          = null;
        DataInputStream  in              = null;
        DataOutputStream out             = null;
        boolean          loginRegistrado = false;

        try {
            socket = new Socket(servidor, puerto);
            in     = new DataInputStream(socket.getInputStream());
            out    = new DataOutputStream(socket.getOutputStream());

            // Mide tiempo de registro
            long inicioRegistro = System.currentTimeMillis();
            out.writeUTF("LOGIN:" + nombreJugador);
            String[] respuestaLogin = in.readUTF().split(":");
            long tiempoRegistro = System.currentTimeMillis() - inicioRegistro;

            boolean loginExitoso = respuestaLogin[0].equals("OK");
            resultados.registrarConexion(loginExitoso);
            resultados.agregarTiempoRegistro(tiempoRegistro);
            loginRegistrado = true;

            if (loginExitoso) {
                // Mide tiempo de juego — manda 5 hits automáticos
                for (int i = 0; i < 5; i++) {
                    int fila = (int) (Math.random() * 3);
                    int col  = (int) (Math.random() * 3);

                    long inicioHit = System.currentTimeMillis();
                    out.writeUTF("HIT:" + fila + ":" + col);
                    in.readUTF();
                    long tiempoHit = System.currentTimeMillis() - inicioHit;

                    resultados.agregarTiempoJuego(tiempoHit);

                    // Pausa entre hits para mantener la conexión abierta y generar carga realista
                    Thread.sleep(pausa_hit);
                }
            }

        } catch (UnknownHostException e) {
            System.out.println("Sock: " + e.getMessage());
            if (!loginRegistrado) resultados.registrarConexion(false);
        } catch (EOFException e) {
            System.out.println("EOF: " + e.getMessage());
            if (!loginRegistrado) resultados.registrarConexion(false);
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
            if (!loginRegistrado) resultados.registrarConexion(false);
        } catch (InterruptedException e) {
            System.out.println("Interrupted: " + e.getMessage());
        } finally {
            if (socket != null) try {
                socket.close();
            } catch (IOException e) {
                System.out.println("close: " + e.getMessage());
            }
        }
    }
}
