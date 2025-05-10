package ServerWork;

public class ServerStart {
    public static final int PORT_WORK = 9006;

    public ServerStart() {
    }

    public static void main(String[] args) {
        Server server = new Server(9006);
        (new Thread(server)).start();//созд нового потока
    }
}
