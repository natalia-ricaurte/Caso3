public class App {
    public static void main(String[] args) {
        int port = 1234;
        Servidor servidor = new Servidor(port);
        Cliente cliente = new Cliente("localhost", port);

        servidor.start(); // Inicia el servidor en su propio hilo
        try {
            Thread.sleep(1000); // Esperamos un poco antes de iniciar el cliente
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrumpido " + ie.getMessage());
        }
        cliente.start(); // Inicia el cliente en su propio hilo
    }
}