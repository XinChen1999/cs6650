import Model.ConsumerRunnable;
import Model.LikesInfo;
import Model.SwipeesInfo;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class ConsumerRun {
    private static final int numOfThread = 100;
    private static Connection connection;

    public static void main(String[] args) throws InterruptedException, IOException, TimeoutException {


        System.out.println("---------------------------------------------------------");
        System.out.println("--------------------Process Begins-----------------------");
        System.out.println("---------------------------------------------------------");

        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost("35.85.60.57");

        connection = factory.newConnection();
        LikesInfo processLikes = new LikesInfo();
        SwipeesInfo swipeesInfo = new SwipeesInfo();

        for (int i = 0; i < numOfThread; i++) {
            ConsumerRunnable consumerRunnable = new ConsumerRunnable(connection, processLikes, swipeesInfo);
            Thread thread = new Thread(consumerRunnable);
            thread.start();
            System.out.println("Thread Start" + i);
        }

    }
}