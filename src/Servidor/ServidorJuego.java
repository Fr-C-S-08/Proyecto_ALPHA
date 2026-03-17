package Servidor;

import jakarta.jms.*;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import java.net.*;
import java.io.*;
import java.util.*;

public class ServidorJuego {

    private static final int    puerto            = 49152;
    private static final int    golpes_para_ganar = 5;
    private static final String url               = ActiveMQConnection.DEFAULT_BROKER_URL;
    private static final String topico            = "MONSTRUO_TOPICO";

    // Se utilizó un HashMap, para poder validar si los Jugadores ya existían en la partida
    // o si son Jugadores nuevos.
    private Map<String, Integer> scores = new HashMap<>();

    // Se instancia el mensajero de posiciones de los monstruos
    private TopicoPosicion mensajeroPosicion;

    // Se crea otro productor para publicar ganador a todos los clientes
    private Connection connection;
    private Session session;
    private MessageProducer productor_de_ganador;

    // Se inicia el servidor principal que controla, tanto los mensajes de posición de monstruos
    // como mensajes de ganador, puntos y conexiones de jugadores.
    public void iniciar() {
        iniciarMensajeroGanador();
        iniciarMensajeroPosicion();
        escucharConexiones();
    }

    // Establece conexión JMS para poder publicar al ganador, la implementación de otro MessaggeSender
    // para poder mandar el resultado del ganador sin interrumpir la implementación de los mensajes de posición.
    private void iniciarMensajeroGanador() {
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            connection = connectionFactory.createConnection();
            connection.start();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topico);
            productor_de_ganador = session.createProducer(destination);
            System.out.println("Mnesajero listo para publicar ganador.");
        } catch (JMSException e) {
            System.out.println("Error al iniciar: " + e.getMessage());
        }
    }

    // Arranca el mensajero de posiciones de los monstruos
    private void iniciarMensajeroPosicion() {
        mensajeroPosicion = new TopicoPosicion();
        mensajeroPosicion.start();
        System.out.println("MensajeroPosicion iniciado.");
    }

    // Espera conexiones TCP de jugadores e instancia un ManejadorHiloJugador por cada uno
    private void escucharConexiones() {
        try {
            ServerSocket listenSocket = new ServerSocket(puerto);
            while (true) {
                System.out.println("Esperando jugadores...");
                Socket clientSocket = listenSocket.accept();
                ManejadorHiloJugador manejador = new ManejadorHiloJugador(clientSocket, this);
                manejador.start();
            }
        } catch (IOException e) {
            System.out.println("Listen: " + e.getMessage());
        }
    }

    // Registra al jugador en un HashMap conservando su score si ya existía o se reconecta
    public synchronized int registrarJugadorHash(String nombre) {
        if (!scores.containsKey(nombre)) {
            scores.put(nombre, 0);
            System.out.println("Jugador nuevo registrado: " + nombre);
        } else {
            System.out.println("Jugador reconectado: " + nombre +
                    " | Score actual: " + scores.get(nombre));
        }
        return scores.get(nombre);
    }

    // Valida el hit del jugador actualiza score del jugador y verifica si hay ganador
    public synchronized boolean procesarHit(String nombreJugador, int fila, int col) {
        // Valida que el monstruo esté en la posición recibida
        int[] posicionActual = mensajeroPosicion.getPosicionActual();
        if (posicionActual[0] != fila || posicionActual[1] != col) {
            System.out.println("Hit inválido de " + nombreJugador +
                    " en [" + fila + "][" + col + "]");
            return false;
        }

        // Hit válido entonces se incrementa score del jugador
        int nuevoScore = scores.getOrDefault(nombreJugador, 0) + 1;
        scores.put(nombreJugador, nuevoScore);
        System.out.println(nombreJugador + " golpeó un monstruo, Score: " + nuevoScore);

        // Verifica si el jugador alcanzó el límite de golpes
        if (nuevoScore >= golpes_para_ganar) {
            publicarGanador(nombreJugador);
            reiniciarJuego();
        }

        return true;
    }

    // Publica el nombre del ganador a todos los clientes vía tópico
    private void publicarGanador(String nombreGanador) {
        try {
            TextMessage mensaje = session.createTextMessage();
            mensaje.setText("GANADOR:" + nombreGanador);
            productor_de_ganador.send(mensaje);
            System.out.println("Ganador publicado: " + nombreGanador);
        } catch (JMSException e) {
            System.out.println("Error al publicar ganador: " + e.getMessage());
        }
    }

    // Reinicia el score de todos los jugadores para una nueva ronda
    private void reiniciarJuego() {
        for (String jugador : scores.keySet()) {
            scores.put(jugador, 0);
        }
        System.out.println("Juego reiniciado — scores en cero.");
    }

    // Manda llamr el score actual del Jugador
    public synchronized int getScore(String nombreJugador) {
        return scores.getOrDefault(nombreJugador, 0);
    }


    void main() {
        new ServidorJuego().iniciar();
    }
}
