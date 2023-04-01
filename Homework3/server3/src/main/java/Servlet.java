import Model.ChannelPool;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@WebServlet(name = "Servlet", value = "/Servlet")
public class Servlet extends HttpServlet {
    private final static String QUEUE_NAME1 = "POOL1";
    private final static String QUEUE_NAME2 = "POOL2";
    private ChannelPool channelPool;

    @Override
    public void init() throws ServletException {
        try {
            super.init();
            this.channelPool = new ChannelPool();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("invalid url");
            return;
        }

        String[] urlParts = urlPath.split("/");

        if (!validateAction(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            System.out.println('1');
        } else {
            // process the request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();

            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    sb.append(line).append('\n');
                }
            } finally {
                reader.close();
            }

            try {

                String inputQueueString = String.join("/", Arrays.copyOfRange(urlParts, 1, 4));
                Channel channel = channelPool.takeChannel();

                channel.queueDeclare(QUEUE_NAME1, false, false, false, null);
                channel.basicPublish("", QUEUE_NAME1, (AMQP.BasicProperties) null, inputQueueString.getBytes(StandardCharsets.UTF_8));

                channel.queueDeclare(QUEUE_NAME2, false, false, false, null);
                channel.basicPublish("", QUEUE_NAME2, (AMQP.BasicProperties) null, inputQueueString.getBytes(StandardCharsets.UTF_8));

                this.channelPool.add(channel);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }


            res.setStatus(HttpServletResponse.SC_CREATED);
        }
    }

    private boolean validateAction(String[] urlPath) {
        // urlPath  = "/swipe/left"
        // urlParts = [, left/right]
        if (urlPath[0].length() != 0) {
            return false;
        }
        return Objects.equals(urlPath[1], "left") || Objects.equals(urlPath[1], "right");
    }

}
