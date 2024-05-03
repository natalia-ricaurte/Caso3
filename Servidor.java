import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor extends Thread {
    private int port;

    public Servidor(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor iniciado en el puerto " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado.");

                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                String clientInput;
                while ((clientInput = input.readLine()) != null) {
                    System.out.println("Recibido del cliente: " + clientInput);
                    String [] message = clientInput.split(",");
                    if (message[0].equals("1")){
                        output.println("2,Cifrar: "+ message[1]);
                    }
                    else if (message[0].equals("3")){
                        output.println("4,Validar: "+ message[1]);
                    }
                    else if (message[0].equals("10")){
                        output.println("11,Descifrar: "+ message[1]);
                    }
                    else {
                        output.println("Respuesta del servidor: " + clientInput);
                    }

                    
                }

                socket.close();
                System.out.println("Cliente desconectado.");
            }
        } catch (IOException e) {
            System.err.println("Servidor Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
