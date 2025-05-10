/*
package ServerWork;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    protected int serverPort = 9006;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;

    public Server(int port){
        this.serverPort = port;
    }

    @Override
    public void run(){
        openServerSocket();
        while(! isStopped()){
            Socket clientSocket = null;
            try {
                clientSocket = this.serverSocket.accept();
            } catch (IOException e) {
                if(isStopped()) {
                    System.out.println("Server Stopped.") ;
                    return;
                }
                throw new RuntimeException("Error accepting client connection", e);
            }
            new Thread(new Worker(clientSocket)).start();
            System.out.println("Клиент подключен.");
        }
        System.out.println("Server Stopped.") ;
    }


    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        System.out.println("Stopping Server");
        this.isStopped = true;
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException("Error closing server", e);
        }
    }

    private void openServerSocket() {
        System.out.println("Opening server socket...");
        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException e) {
            throw new RuntimeException("Cannot open port " + this.serverPort, e);
        }
    }
}
*/
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ServerWork;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server implements Runnable {
    protected int serverPort = 9006;
    protected ServerSocket serverSocket = null;
    protected boolean isStopped = false;

    public Server(int port) {
        this.serverPort = port;
    }

    public void run() {
        this.openServerSocket();

        while(!this.isStopped()) {
            Socket clientSocket = null;

            try {
                clientSocket = this.serverSocket.accept();
                System.out.println("Клиент подключен.");
                (new Thread(new Worker(clientSocket))).start();
            } catch (IOException var3) {
                if (this.isStopped()) {
                    System.out.println("Сервер остановлен.");
                    return;
                }

                throw new RuntimeException("Ошибка принятия соединения с клиентом", var3);
            }
        }

        System.out.println("Сервер остановлен.");
    }

    private synchronized boolean isStopped() {
        return this.isStopped;
    }

    public synchronized void stop() {
        System.out.println("Остановка сервера");
        this.isStopped = true;

        try {
            this.serverSocket.close();
        } catch (IOException var2) {
            throw new RuntimeException("Ошибка при закрытии сервера", var2);
        }
    }

    private void openServerSocket() {
        System.out.println("Открытие серверного сокета...");

        try {
            this.serverSocket = new ServerSocket(this.serverPort);
        } catch (IOException var2) {
            throw new RuntimeException("Невозможно открыть порт " + this.serverPort, var2);
        }
    }
}
