package model;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static java.net.HttpURLConnection.HTTP_CREATED;

public class Processor implements Runnable{
    private String urlIP;
    private int totalReqNum;
    private AtomicInteger successReqNum;
    private AtomicInteger failReqNum;
    private BlockingQueue<SwipeEvent> swipeEvents;
    private CountDownLatch latch;
    private static final int MAX_RETRY = 5;

    private List<String[]> records;

    public Processor(String urlIP, int totalReqNum, AtomicInteger successReqNum, AtomicInteger failReqNum,
                     BlockingQueue<SwipeEvent> swipeEvents, CountDownLatch latch, List<String[]> records) {
        this.urlIP = urlIP;
        this.totalReqNum = totalReqNum;
        this.successReqNum = successReqNum;
        this.failReqNum = failReqNum;
        this.swipeEvents = swipeEvents;
        this.latch = latch;
        this.records = records;
    }

    @Override
    public void run() {
        ApiClient apiClient = new ApiClient();
        SwipeApi swipeApi = new SwipeApi(apiClient);
        swipeApi.getApiClient().setBasePath(this.urlIP);

        // record successful and failed request
        Integer successNum = 0;
        Integer failNum = 0;

        for (int i = 0; i < this.totalReqNum; i++) {
            SwipeEvent curEvent = this.swipeEvents.poll();

            if (postEvent(swipeApi, curEvent)) {
                successNum++;
            } else {
                failNum++;
            }
        }

        successReqNum.getAndAdd(successNum);
        failReqNum.getAndAdd(failNum);
        latch.countDown();
    }

    public boolean postEvent(SwipeApi swipeApi, SwipeEvent curEvent) {
        int retry = 0;

        while (retry < MAX_RETRY) {
            try {
                long start = System.currentTimeMillis();
                SwipeDetails swipeRes = new SwipeDetails();
                swipeRes.setSwiper(curEvent.getSwiper());
                swipeRes.setSwipee(curEvent.getSwipee());
                swipeRes.setComment(curEvent.getComment());
                ApiResponse<Void> res = swipeApi.swipeWithHttpInfo(swipeRes, curEvent.getSwipe());
                if (res.getStatusCode() == HTTP_CREATED) {
                    long end = System.currentTimeMillis();
                    System.out.println(end - start);

                    // generate current event record
                    String[] curRecord = new String[4];
                    curRecord[0] = String.valueOf(start);
                    curRecord[1] = "POST";
                    curRecord[2] = String.valueOf(end - start);
                    curRecord[3] = String.valueOf(res.getStatusCode());

                    // add current record to list
                    this.records.add(curRecord);
                    return true;
                }
            } catch (ApiException e) {
                retry++;
                e.printStackTrace();
            }
        }

        return false;
    }
}
