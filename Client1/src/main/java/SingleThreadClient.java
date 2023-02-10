import Model.EventGenerator;
import Model.Processor;
import Model.SwipeEvent;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadClient {
  private static final int TOTAL_REQ = 3000;

  private static AtomicInteger successReq;
  private static AtomicInteger failReq;

  public static void main(String[] args) throws InterruptedException {
    String urlIP = "http://54.245.152.254:8080/Server_war/";


    BlockingQueue<SwipeEvent> swipeEvents = new LinkedBlockingQueue<>();
    successReq = new AtomicInteger(0);
    failReq = new AtomicInteger(0);

    System.out.println("---------------------------------------------------------");
    System.out.println("--------------------Process Begins-----------------------");
    System.out.println("---------------------------------------------------------");


    long start = System.currentTimeMillis();
    EventGenerator eventGenerator = new EventGenerator(swipeEvents,TOTAL_REQ);
    Thread producerThread = new Thread(eventGenerator);
    producerThread.start();

    CountDownLatch latch = new CountDownLatch(1);
    Processor processor = new Processor(urlIP, TOTAL_REQ, successReq, failReq, swipeEvents, latch);
    Thread consumerThread = new Thread(processor);
    consumerThread.start();
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
