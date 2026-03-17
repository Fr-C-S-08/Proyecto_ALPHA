package Jugadores;

import jakarta.jms.Connection;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;

import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

public class TopicoJugador implements MessageListener {

    private static final String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    private static final String topico = "MONSTRUO_TOPICO";

    private Connection connection;
    private Session session;
    private MessageConsumer consumidor_de_posicion;
    private VisualJugador   ventana; //Esta se declara para la ventana (GUI) de cada jugador
    private int filaAnterior = -1;
    private int colAnterior  = -1;

    public TopicoJugador(VisualJugador ventana) {
        this.ventana = ventana;
    }

    public void iniciar() {
        try {
            ActiveMQConnectionFactory fabrica = new ActiveMQConnectionFactory(url);
            connection = fabrica.createConnection();
            connection.start();

            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            Destination destination = session.createTopic(topico);

            consumidor_de_posicion = session.createConsumer(destination);
            consumidor_de_posicion.setMessageListener(this); // asíncrono

            System.out.println("TopicoJugador suscrito a " + topico);

        } catch (JMSException e) {
            System.out.println("Error al suscribirse al tópico: " + e.getMessage());
        }
    }

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();

            System.out.println("Mensaje recibido: " + text);

            //Formato esperado: "MONSTRUO:fila:col"
            String[] partes = text.split(":");

            if (partes[0].equals("MONSTRUO")) {
                int fila = Integer.parseInt(partes[1]);
                int col  = Integer.parseInt(partes[2]);

                if (filaAnterior != -1) {
                    ventana.OcultarMonstruo(filaAnterior, colAnterior);
                    //Oculta el monstruo anterior al dar el timeout del servidor
                }
                //Muestra el nuevo monstruo
                ventana.MostrarMonstruo(fila, col);

                //Guarda la possición actual
                filaAnterior = fila;
                colAnterior  = col;
            } else if (partes[0].equals("GANADOR")) {
                // Oculta el monstruo actual antes de mostrar al ganador
                if (filaAnterior != -1) {
                    ventana.OcultarMonstruo(filaAnterior, colAnterior);
                }
                ventana.MostrarGanador(partes[1]);

                // Resetea posición anterior para nueva ronda
                filaAnterior = -1;
                colAnterior  = -1;
            }

        } catch (JMSException e) {
            System.out.println("Error al procesar mensaje: " + e.getMessage());
        }
    }

    public void detener() {
        try {
            if (consumidor_de_posicion != null) consumidor_de_posicion.close();
            if (session != null) session.close();
            if (connection != null) connection.close();
            System.out.println("TopicoJugador desconectado.");
        } catch (JMSException e) {
            System.out.println("Error al cerrar tópico: " + e.getMessage());
        }
    }
}
