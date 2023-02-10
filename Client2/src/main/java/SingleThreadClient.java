import com.opencsv.CSVWriter;
import model.*;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class SingleThreadClient {
  private static final int TOTAL_REQ = 3000;

  private static List<String[]> records;

  public static void main(String[] args) throws InterruptedException {
    String urlIP = "http://54.218.150.0:8080/Server_war/";


    BlockingQueue<SwipeEvent> swipeEvents = new LinkedBlockingQueue<>();
    AtomicInteger successReq = new AtomicInteger(0);
    AtomicInteger failReq = new AtomicInteger(0);

    records = new ArrayList<>();
    records.add(new String[]{"Start Time", "Request Type", "Latency", "Response Code"});

    System.out.println("---------------------------------------------------------");
    System.out.println("--------------------Process Begins-----------------------");
    System.out.println("---------------------------------------------------------");


    long start = System.currentTimeMillis();
    EventGenerator eventGenerator = new EventGenerator(swipeEvents,TOTAL_REQ);
    Thread producerThread = new Thread(eventGenerator);
    producerThread.start();

    CountDownLatch latch = new CountDownLatch(1);
    Processor processor = new Processor(urlIP, TOTAL_REQ, successReq, failReq, swipeEvents, latch, records);
    Thread consumerThread = new Thread(processor);
    consumerThread.start();
    latch.await();

    long end = System.currentTimeMillis();
    long wallTime = end - start;

    try {
      FileWriter fileWriter = new FileWriter("/Users/xinchen/Desktop/6650/Homework1/Client2/src/main/java/output/SingleThreadTest.CSV");
      CSVWriter writer = new CSVWriter(fileWriter);
      writer.writeAll(records);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.out.println("---------------------------------------------------------");
    System.out.println("---------------------Process Ends------------------------");
    System.out.println("---------------------------------------------------------");
    System.out.println("Number of successful requests :" + successReq.get());
    System.out.println("Number of failed requests :" + failReq.get());
    System.out.println("Total wall time: " + wallTime);
    System.out.println( "Throughput: " + (int)((successReq.get() + failReq.get()) / (double)(wallTime / 1000)) + " requests/second");
  }
}
