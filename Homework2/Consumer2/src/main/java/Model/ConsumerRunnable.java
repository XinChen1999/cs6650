package Model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ConsumerRunnable implements Runnable {
    private final static String QUEUE_NAME = "HEYPOOL2";
    private Connection connection;
    private LikesInfo likesInfo;
    private SwipeesInfo swipeesInfo;

    public ConsumerRunnable(Connection connection, LikesInfo likesInfo, SwipeesInfo swipeesInfo) {
        this.connection = connection;
        this.likesInfo = likesInfo;
        this.swipeesInfo = swipeesInfo;
    }

    @Override
    public void run() {
        Channel channel;
        try {
            channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                String[] messageParts = message.split("/");

                String swiper = messageParts[1];
                String swipee = messageParts[2];
                String action = messageParts[0];

                swipeesInfo.swipe(action, swiper, swipee);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

            };
            channel.basicConsume(QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
