public class ClientApp {

    public static String ipAddr = "localhost";
    public static int port = 8080;



    public static void main(String[] args) {
        new Client(ipAddr, port);
    }
}
