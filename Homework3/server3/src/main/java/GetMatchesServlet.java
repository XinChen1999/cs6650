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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "GetMatchesServlet", value = "/GetMatchesServlet")
public class GetMatchesServlet extends HttpServlet {

    private static final int MIN_USER_ID = 1;
    private static final int MAX_USER_ID = 5000;

    private static final String DYNAMODB_TABLE_1 = "user-info-table";
    private static final String DYNAMODB_TABLE_2 = "likes-dislikes-table";

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

        PrintWriter out = res.getWriter();
        try {
            String swiperId = pathInfo.split("/")[1];
            res.setStatus(HttpServletResponse.SC_OK);

            GetItemResponse responseItem = getUserLikesAndDislikes(swiperId, dynamoDbClient);

            if (responseItem.hasItem()) {
                AttributeValue personAttribute = responseItem.item().get("likes");
                ArrayList<String> personList = new ArrayList<>(Arrays.asList(personAttribute.s().split(",")));
                Map<String, ArrayList<String>> matchListMap = new HashMap<>();
                matchListMap.put("matches", personList);
                Gson gson = new Gson();
                String json = gson.toJson(matchListMap);
                out.write(json);
            }
        } catch (Exception e) {
            res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to retrieve user stats");
            return;
        }
        writer.close();
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        // TODO: handle POST requests, if needed
    }


    private GetItemResponse getUserLikesAndDislikes(String swiperId, DynamoDbClient client) {
        Map<String, AttributeValue> keyToFind = new HashMap<>();
        keyToFind.put("swiper_id", AttributeValue.builder().s(swiperId).build());
        GetItemRequest request = GetItemRequest.builder()
                .tableName(DYNAMODB_TABLE_2)
                .key(keyToFind)
                .build();
        System.out.println(request.toString());
        GetItemResponse response = client.getItem(request);
        System.out.println(response.toString());
        return response;
    }
}
