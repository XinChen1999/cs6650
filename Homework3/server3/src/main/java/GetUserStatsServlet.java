import Model.ChannelPool;
import com.google.gson.Gson;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "GetUserStatsServlet", value = "/users/*")
public class GetUserStatsServlet extends HttpServlet {
    private final static String TABLE_NAME_LIKES = "UserLikes";
    private final static String TABLE_NAME_DISLIKES = "UserDislikes";
    private final static int MIN_USER_ID = 1;
    private final static int MAX_USER_ID = 1000000;

    private ChannelPool channelPool;

    private DynamoDbClient dynamoDbClient;

    @Override
    public void init() throws ServletException {
      super.init();

      AwsCredentialsProvider credentialsProvider = SystemPropertyCredentialsProvider.create();
      System.setProperty("aws.accessKeyId", "ASIA32MRRSJ365G45N76");
      System.setProperty("aws.secretAccessKey", "mM0sWi9auKVcqgA+LtYfOolgW6UrQ8DH8h+P1mgX");
      System.setProperty("aws.sessionToken", "FwoGZXIvYXdzEBMaDO0C9wHGbz2hnyuD/SLJAZDgvKwVgQpsKNJmRYB/XDXZuQUFiEhcS4zR7rbvwQeoEVNWqBfW5tBfEvauDdIb7nfXvvaHBiZ0fwBAVMIys3fZoVDhEROoiuQ8MAkmmwRqzw/jq+Ba46joQPgrDV2N+KoQcMRQj17uHompUdZgodwytHDaSRSioPooOS1xivCuXnVKohe835nzDctCVtKSs4qmcTIr6XzsW+1cm+3//xjb36kxkC5LNAAqePMoauBOZZr49uSUGhhcGck4E8wqrYYG39tjNIcpgSj7hfSgBjItHCokdFE7NOjOtNN4Z3B9uysjaUvp7ICC0Xhf4VZcd0ZyIW0G3NJSuqsDE6aH");

      dynamoDbClient = DynamoDbClient.builder()
              .credentialsProvider(credentialsProvider)
              .region(Region.US_WEST_2)
              .build();

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String[] pathParts = pathInfo.split("/");
        if (pathParts.length != 2 || !pathParts[1].matches("\\d+")) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID");
            return;
        }

        int userId = Integer.parseInt(pathParts[1]);
        if (userId < MIN_USER_ID || userId > MAX_USER_ID) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "User ID out of range");
            return;
        }

        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        PrintWriter writer = res.getWriter();

        try {
            int likes = getUserLikes(userId);
            int dislikes = getUserDislikes(userId);
            Map<String, Integer> result = new HashMap<>();
            result.put("likes", likes);
            result.put("dislikes", dislikes);
            Gson gson = new Gson();
            String json = gson.toJson(result);
            writer.write(json);
        } catch (Exception e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve user stats");
            return;
        }

        writer.close();
    }

    private int getUserLikes(int userId) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME_LIKES)
                .key(Map.of("UserId", AttributeValue.builder().n(String.valueOf(userId)).build()))
                .projectionExpression("Likes")
                .build();
        GetItemResponse response = dynamoDbClient.getItem(request);
        if (response.hasItem()) {
            return Integer.parseInt(response.item().get("Likes").n());
        } else {
            return 0;
        }
    }

    private int getUserDislikes(int userId) {
        GetItemRequest request = GetItemRequest.builder()
                .tableName(TABLE_NAME_DISLIKES)
                .key(Map.of("UserId", AttributeValue.builder().n(String.valueOf(userId)).build()))
                .projectionExpression("Dislikes")
                .build();
        GetItemResponse response = dynamoDbClient.getItem(request);
        if (response.hasItem()) {
            return Integer.parseInt(response.item().get("Dislikes").n());
        } else {
            return 0;
        }
    }
}
