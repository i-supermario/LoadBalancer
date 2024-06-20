import com.sun.net.httpserver.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

public class BackendServer {
    public static void main(String[] args) throws Exception {

        if(args.length == 0){
            System.out.print("No port number provided");
            System.exit(0);
        }

        int port = Integer.parseInt(args[0]); 
        
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/", new RootHandler() );

        server.setExecutor(null);
        server.start();
        
    }

    static class RootHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String response = "Backend Server is liveee!!";
            exchange.sendResponseHeaders(200, response.getBytes().length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

        }
    }

    
}
