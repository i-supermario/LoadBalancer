import com.sun.net.httpserver.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LoadBalancer {

    public static ArrayList<String> servers;
    public static int currServerIndex;
    public static Map<String, Boolean> serverHealthCheck;

    public static void main(String[] args) throws Exception {

        if(args.length == 0){
            System.out.println("No servers provided");
            System.exit(0);
        }
        System.out.println(args);

        servers = new ArrayList<>(Arrays.asList(args));
        currServerIndex = 0;

        serverHealthCheck = new HashMap<>();

        ScheduledExecutorService healthCheckScheduler = Executors.newScheduledThreadPool(1);
        
        Runnable HealthCheckService = new Runnable() {

            @Override
            public void run() {
                for(String server: servers){
                    boolean serverHealth = checkServerHealth(server);
                    System.out.printf("%s : %b \n",server, serverHealth);

                    if(serverHealth){
                        serverHealthCheck.putIfAbsent(server, true);
                    }
                    else serverHealthCheck.putIfAbsent(server, false);
                }
            }

        };

        healthCheckScheduler.scheduleAtFixedRate(HealthCheckService, 0, 10, TimeUnit.SECONDS);

        

        System.out.println(servers);
    
        HttpServer server = HttpServer.create(new InetSocketAddress(7000), 0);

        server.createContext("/", new RootHandler() );

        server.setExecutor(Executors.newFixedThreadPool(5));
        server.start();
        
    }

    private static boolean checkServerHealth(String url){
        try {
            URL temp = URI.create(url).toURL();
            HttpURLConnection connection = (HttpURLConnection) temp.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();

            if(responseCode != 200) return false;

            return true;
            
        } catch (Exception e) {
            // System.out.println(e);
            return false;
        }
    
    }

    static class RootHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            System.out.println(servers);

            String currServer = servers.get(currServerIndex);
            System.out.println(currServerIndex);
            System.out.println(currServer);
            currServerIndex = (currServerIndex + 1) % servers.size();
            if(checkServerHealth(currServer)){
                String response = "No Response";
                response = makeRequest(currServer);
                exchange.sendResponseHeaders(200, response.getBytes().length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
            else{
                exchange.sendResponseHeaders(500, 0);
                System.out.println("server not healthy");
            }

        }
    }

    private static String makeRequest(String targetURL){

        try {

            URL url = URI.create(targetURL).toURL();
            System.out.println(url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            return response.toString();

        } catch (Exception e) {
            System.out.println(e);
            return e.toString();
        }

        

        

    }
}
