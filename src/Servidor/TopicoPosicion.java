package Servidor;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import java.util.Random;

public class TopicoPosicion extends Thread {

    private static final String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private static final String topico = "MONSTRUO_TOPICO";
    private static final int intervalo_ms = 2000; // cada 2 segundos sale un nuevo monstruo

    private boolean corriendo = true;
    private int filaActual = -1;
    private int colActual = -1;

    @Override
    public void run() {
        Random random = new Random();//Para generar un número aletorio para destinar a la posición del grid
        MessageProducer productor_de_posicion = null;
        Session session = null;
        Connection connection = null;
        //Se les puso valores de null para evitar conflictos que se detectarán en el finally
        try {
            ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
            connection = connectionFactory.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topico);
            productor_de_posicion = session.createProducer(destination);

            while (corriendo) {
                int fila = random.nextInt(3); // 0, 1 o 2
                int col = random.nextInt(3); // 0, 1 o 2

                // Guarda la posición actual para validación del servidor cuando se detecte un hit
                synchronized (this) {
                    filaActual = fila;
                    colActual = col;
                }

                TextMessage message = session.createTextMessage();
                message.setText("MONSTRUO:" + fila + ":" + col);

                productor_de_posicion.send(message);
                System.out.println("Monstruo publicado en [" + fila + "][" + col + "]");

                Thread.sleep(intervalo_ms);
            }

        } catch (JMSException e) {
            System.out.println("JMS error: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Produccion de posiciones interrumpida.");
        } finally {
            try {
                if (productor_de_posicion != null) productor_de_posicion.close();
                if (session != null) session.close();
                if (connection != null) connection.close();
            } catch (JMSException e) {
                System.out.println("Error al cerrar: " + e.getMessage());
            }
        }
    }

    // Para detener el hilo limpiamente desde ServidorJuego
    public void detener() {
        corriendo = false;
        this.interrupt();
    }

    // Para que ServidorJuego pueda validar la posición actual del monstruo
    public synchronized int[] getPosicionActual() {
        return new int[]{filaActual, colActual};
    }
}