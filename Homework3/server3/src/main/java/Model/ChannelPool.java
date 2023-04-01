package Model;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;

public class ChannelPool {
    private Connection connection;
    private BlockingQueue<Channel> pool;
    private final static int capacity = 100;

    public ChannelPool() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
//    factory.setHost("ec2-35-85-44-8.us-west-2.compute.amazonaws.com");
        factory.setHost("35.85.60.57");

//    factory.setHost("localhost");
        factory.setPassword("guest");
        factory.setUsername("guest");

        try {
            this.connection = factory.newConnection();
        } catch (IOException | TimeoutException e) {
            System.err.println("Connection Failed");
            e.printStackTrace();
        }

        this.pool = new LinkedBlockingQueue<>();

        for (int i = 0; i < this.capacity; i++) {
            try {
                Channel channel = this.connection.createChannel();
                this.pool.add(channel);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Channel takeChannel() throws InterruptedException {
        return this.pool.take();
    }

    public void add(Channel channel) {
        this.pool.offer(channel);
    }
}