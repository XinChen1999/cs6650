import model.*;

import java.io.FileWriter;
import com.opencsv.CSVWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiThreadClient {

    private static final int totalReq = 500000;
    private static AtomicInteger successReq;
    private static AtomicInteger failReq;
    private static BlockingQueue<SwipeEvent> swipeEvents;
    private static List<String[]> records;

    private static List<String[]> plotStatistics;
    private static final int numOfThread = 100;
    private static final int singleReq = 5000;

    public static void main(String[] args) throws InterruptedException {
        String urlIP = "http://54.218.150.0:8080/Server_war";

        swipeEvents = new LinkedBlockingQueue<>();
        successReq = new AtomicInteger(0);
        failReq = new AtomicInteger(0);

        records = new ArrayList<>();
        records.add(new String[]{"Start Time", "Request Type", "Latency", "Response Code"});

        System.out.println("---------------------------------------------------------");
        System.out.println("--------------------Process Begins-----------------------");
        System.out.println("---------------------------------------------------------");

        CountDownLatch latch = new CountDownLatch(numOfThread);
        long start = System.currentTimeMillis();
        EventGenerator eventGenerator = new EventGenerator(swipeEvents,totalReq);
        Thread eventThread = new Thread(eventGenerator);
        eventThread.start();

        for (int i = 0; i < numOfThread; i++) {
            Processor processor = new Processor(urlIP, singleReq, successReq, failReq, swipeEvents, latch, records);
            Thread thread = new Thread(processor);
            thread.start();
        }
        latch.await();

        long end = System.currentTimeMillis();
        long wallTime = end - start;

        try {
            FileWriter fileWriter = new FileWriter("/Users/xinchen/Desktop/6650/Homework1/Client2/src/main/java/output/MultiThreadTest.CSV");
            CSVWriter writer = new CSVWriter(fileWriter);
            writer.writeAll(records);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        StatisticGenerator statisticsGenerator = new StatisticGenerator(records);
        double meanVal = statisticsGenerator.getMeanValue();
        double medianVal = statisticsGenerator.getMedianValue();
        double percent99Val = statisticsGenerator.get99PercentValue();
        double maxVal = statisticsGenerator.getMaxValue();
        double minVal = statisticsGenerator.getMinValue();

        plotStatistics = statisticsGenerator.getPlot();

        try {
            FileWriter fileWriter = new FileWriter("/Users/xinchen/Desktop/6650/Homework1/Client2/src/main/java/output/RequestPerSecond.CSV");
            CSVWriter writer = new CSVWriter(fileWriter);
            writer.writeAll(plotStatistics);
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
        System.out.println("The mean response time: " + meanVal);
        System.out.println("The median response time: " + medianVal);
        System.out.println("Throughput: " + (int)((successReq.get() + failReq.get()) / (double)(wallTime / 1000)) + " requests/second");
        System.out.println("The p99 (99th percentile) response time: " + percent99Val);
        System.out.println("The max response time: " + maxVal);
        System.out.println("The min response time: " + minVal);
    }
}
