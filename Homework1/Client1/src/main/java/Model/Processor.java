package Model;

import static java.net.HttpURLConnection.HTTP_CREATED;
import Model.SwipeEvent;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Processor implements Runnable{
    private String urlIP;
    private int totalReqNum;
    private AtomicInteger successReqNum;
    private AtomicInteger failReqNum;
    private BlockingQueue<SwipeEvent> swipeEvents;
    private CountDownLatch latch;
    private static final int MAX_RETRY = 5;

    public Processor(String urlIP, int totalReqNum, AtomicInteger successReqNum, AtomicInteger failReqNum,
                    BlockingQueue<SwipeEvent> swipeEvents, CountDownLatch latch) {
        this.urlIP = urlIP;
        this.totalReqNum = totalReqNum;
        this.successReqNum = successReqNum;
        this.failReqNum = failReqNum;
        this.swipeEvents = swipeEvents;
        this.latch = latch;
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
