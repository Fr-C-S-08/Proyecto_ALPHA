package EvaluacionExperimental;

public class PruebaDeEstres {

    private static final int[] configuraciones  = {50, 100, 150, 200, 250, 300, 350, 400, 450, 500};
    private static final int   repeticiones     = 10;

    void main() throws InterruptedException {
        for (int numClientes : configuraciones) {
            System.out.println("\n=========================================================");
            System.out.println("CONFIGURACIÓN: " + numClientes + " clientes");
            System.out.println("=========================================================");

            ResultadosMetricas resultados = new ResultadosMetricas();

            for (int rep = 1; rep <= repeticiones; rep++) {
                System.out.println("Repetición " + rep + "/" + repeticiones);
                ejecutarRonda(numClientes, resultados);
            }

            resultados.imprimirResultados();
        }
    }

    // Lanza numClientes hilos simultáneos y espera a que todos terminen
    private void ejecutarRonda(int numClientes, ResultadosMetricas resultados)
            throws InterruptedException {

        ClientePruebaEstres[] clientes = new ClientePruebaEstres[numClientes];

        // Crea e inicia todos los clientes simultáneamente
        for (int i = 0; i < numClientes; i++) {
            clientes[i] = new ClientePruebaEstres("Jugador" + i, resultados);
            clientes[i].start();
        }

        // Espera a que todos terminen
        for (int i = 0; i < numClientes; i++) {
            clientes[i].join();
        }
    }
}
