import Model.EventGenerator;
import Model.Processor;
import Model.SwipeEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadClient {

    private static final int totalReq = 500000;
    private static AtomicInteger successReq;
    private static AtomicInteger failReq;
    private static BlockingQueue<SwipeEvent> swipeEvents;
    private static final int numOfThread = 100;
    private static final int firstNumWork = 5000;

    public static void main(String[] args) throws InterruptedException {
        String urlIP = "http://34.217.16.78:8080/Server_war";

        swipeEvents = new LinkedBlockingQueue<>();
        successReq = new AtomicInteger(0);
        failReq = new AtomicInteger(0);

        System.out.println("---------------------------------------------------------");
        System.out.println("--------------------Process Begins-----------------------");
        System.out.println("---------------------------------------------------------");

        CountDownLatch latch = new CountDownLatch(numOfThread);
        long start = System.currentTimeMillis();
        EventGenerator eventGenerator = new EventGenerator(swipeEvents,totalReq);
        Thread eventThread = new Thread(eventGenerator);
        eventThread.start();

        for (int i = 0; i < numOfThread; i++) {
            Processor processor = new Processor(urlIP, firstNumWork, successReq, failReq, swipeEvents, latch);
            Thread thread = new Thread(processor);
            thread.start();
        }
        latch.await();

        long end = System.currentTimeMillis();
        long wallTime = end - start;

        System.out.println("---------------------------------------------------------");
        System.out.println("---------------------Process Ends------------------------");
        System.out.println("---------------------------------------------------------");
        System.out.println("Number of successful requests :" + successReq.get());
        System.out.println("Number of failed requests :" + failReq.get());
        System.out.println("Total wall time: " + wallTime);
        System.out.println( "Throughput: " + (int)((successReq.get() + failReq.get()) / (double)(wallTime / 1000)) + " requests/second");
    }
}
