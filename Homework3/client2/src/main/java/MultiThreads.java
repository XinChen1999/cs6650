import Model.*;
import Model.Record;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreads {
  private static String urlBase;
  private static AtomicInteger successReq;
  private static AtomicInteger failReq;
  private static AtomicInteger getSuccessReq;
  private static AtomicInteger getFailReq;
  private static BlockingQueue<SwipeEvent> events;
  private static final int numOfThread = 1;
  private static final int totalReq = 1000;
  private static final int processorNumberEach = 1000;
  private static String pathName;
  private static String pathName2;
  public static void main(String[] args) throws InterruptedException {
    urlBase = "http://localhost:8080/server3_war_exploded";
//    urlBase = "http://my-alb-1984034483.us-west-2.elb.amazonaws.com/server2_war";
//    urlBase = "http://34.209.47.67:8080/server2_war";

    successReq = new AtomicInteger(0);
    failReq = new AtomicInteger(0);
    events = new LinkedBlockingQueue<>();

    List<Record> records = Collections.synchronizedList(new ArrayList<>());
    List<Record> getRecords = Collections.synchronizedList(new ArrayList<>());

    System.out.println("---------------------------------------------------------");
    System.out.println("--------------------Process Begins-----------------------");
    System.out.println("---------------------------------------------------------");

    CountDownLatch latch = new CountDownLatch(numOfThread);
    long start = System.currentTimeMillis();
    EventGenerator eventGenerator = new EventGenerator(events, totalReq);
    Thread generatorThread = new Thread(eventGenerator);
    generatorThread.start();

    for (int i = 0; i < numOfThread; i++) {
      GetRequestProcessor processor = new GetRequestProcessor(urlBase,latch, records, successReq, failReq, processorNumberEach);
      Thread thread = new Thread(processor);
      thread.start();
    }

    GetRequestProcessor getProcessor = new GetRequestProcessor(urlBase, latch, getRecords,successReq, failReq,totalReq);
    Thread getThread = new Thread(getProcessor);
    getThread.start();

    latch.await();
    long end = System.currentTimeMillis();
    long wallTime = end - start;
    PerformanceMetrics performanceMetrics = new PerformanceMetrics(records);
    long maxVal = performanceMetrics.getMaxLatency();
    long minVal = performanceMetrics.getMinLatency();
    long percent99Val = performanceMetrics.get99thPercentileLatency();
    double medianVal = performanceMetrics.getMedianLatency();
    double meanVal = performanceMetrics.getMeanLatency();


    System.out.println("---------------------------------------------------------");
    System.out.println("---------------------Process Ends------------------------");
    System.out.println("---------------------------------------------------------");
    System.out.println("Number of successful requests :" + successReq.get());
    System.out.println("Number of failed requests :" + failReq.get());
    System.out.println("The total wall time: " + wallTime);
    System.out.println("The mean response time: " + meanVal);
    System.out.println("The median response time: " + medianVal);
    System.out.println("Throughput: " + (int)((successReq.get() + failReq.get()) / (double)(wallTime / 1000)) + " requests/second");
    System.out.println("The p99 (99th percentile) response time: " + percent99Val);
    System.out.println("The max response time: " + maxVal);
    System.out.println("The min response time: " + minVal);

  }
}
