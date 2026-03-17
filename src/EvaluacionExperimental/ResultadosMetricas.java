package EvaluacionExperimental;

import java.util.ArrayList;
import java.util.List;

public class ResultadosMetricas {

    private List<Long> tiemposRegistro   = new ArrayList<>();
    private List<Long> tiemposJuego      = new ArrayList<>();
    private int        conexionesExitosas = 0;
    private int        conexionesTotales  = 0;

    // Registra el tiempo de respuesta de un login
    public synchronized void agregarTiempoRegistro(long tiempoMs) {
        tiemposRegistro.add(tiempoMs);
    }

    // Registra el tiempo de respuesta de un hit
    public synchronized void agregarTiempoJuego(long tiempoMs) {
        tiemposJuego.add(tiempoMs);
    }

    // Registra si la conexión fue exitosa o fallida
    public synchronized void registrarConexion(boolean exitosa) {
        conexionesTotales++;
        if (exitosa) conexionesExitosas++;
    }

    // Calcula el promedio de una lista de tiempos
    private double calcularPromedio(List<Long> tiempos) {
        if (tiempos.isEmpty()) return 0;
        long suma = 0;
        for (long t : tiempos) suma += t;
        return (double) suma / tiempos.size();
    }

    // Calcula la desviación estándar de una lista de tiempos
    private double calcularDesviacion(List<Long> tiempos) {
        if (tiempos.isEmpty()) return 0;
        double promedio = calcularPromedio(tiempos);
        double suma     = 0;
        for (long t : tiempos) suma += Math.pow(t - promedio, 2);
        return Math.sqrt(suma / tiempos.size());
    }

    // Calcula el porcentaje de conexiones exitosas
    private double calcularPorcentajeExito() {
        if (conexionesTotales == 0) return 0;
        return (double) conexionesExitosas / conexionesTotales * 100;
    }

    // Resultados
    public void imprimirResultados() {
        System.out.println("=========================================================");
        System.out.println("RESULTADOS DE ESTRESAMIENTO");
        System.out.println("=========================================================");
        System.out.println("Registro:");
        System.out.println("  Promedio:            " + String.format("%.2f", calcularPromedio(tiemposRegistro))  + " ms");
        System.out.println("  Desviación std:      " + String.format("%.2f", calcularDesviacion(tiemposRegistro)) + " ms");
        System.out.println("  Conexiones exitosas: " + conexionesExitosas + "/" + conexionesTotales +
                " (" + String.format("%.2f", calcularPorcentajeExito()) + "%)");
        System.out.println("---------------------------------------------------------");
        System.out.println("Juego:");
        System.out.println("  Promedio:            " + String.format("%.2f", calcularPromedio(tiemposJuego))  + " ms");
        System.out.println("  Desviación std:      " + String.format("%.2f", calcularDesviacion(tiemposJuego)) + " ms");
        System.out.println("=========================================================");
    }
}
