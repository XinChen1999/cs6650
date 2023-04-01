package Model;

import Model.Record;
import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;
import io.swagger.client.model.MatchStats;
import io.swagger.client.model.Matches;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class GetRequestProcessor implements Runnable {
  private String baseUrl;
  private CountDownLatch latch;
  private List<Record> requestRecords;
  private AtomicInteger successfulRequestsCount;
  private AtomicInteger failedRequestsCount;
  private int totalRequestsCount;
  private static final int START_SWIPER_ID = 1;
  private static final int END_SWIPER_ID = 5000;

  public GetRequestProcessor(String baseUrl, CountDownLatch latch, List<Record> requestRecords,
                             AtomicInteger successfulRequestsCount, AtomicInteger failedRequestsCount,
                             int totalRequestsCount) {
    this.baseUrl = baseUrl;
    this.latch = latch;
    this.requestRecords = requestRecords;
    this.successfulRequestsCount = successfulRequestsCount;
    this.failedRequestsCount = failedRequestsCount;
    this.totalRequestsCount = totalRequestsCount;
  }

  @Override
  public void run() {
    ApiClient apiClient = new ApiClient();
    MatchesApi matchesApi = new MatchesApi(apiClient);
    StatsApi statsApi = new StatsApi(apiClient);
    matchesApi.getApiClient().setBasePath(this.baseUrl);
    int countOfSuccessfulRequests = 0;
    int countOfFailedRequests = 0;

    while (!(successfulRequestsCount.get() + failedRequestsCount.get() == totalRequestsCount)) {
      int num = successfulRequestsCount.get() + failedRequestsCount.get();
      int swiperId = ThreadLocalRandom.current().nextInt(START_SWIPER_ID, END_SWIPER_ID + 1);
      String swiperIdStr = String.valueOf(swiperId);
      Integer curTurn = ThreadLocalRandom.current().nextInt(0, 2);
      boolean value;
      if (curTurn == 0) {
        value = doMatch(matchesApi, swiperIdStr);
      } else {
        value = doStats(statsApi, swiperIdStr);
      }
      if (value) {
        countOfSuccessfulRequests++;
      } else {
        countOfFailedRequests++;
      }
      try {
        Thread.sleep(200);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }

    System.out.println("Number of successful GET requests: " + countOfSuccessfulRequests);
    System.out.println("Number of failed GET requests: " + countOfFailedRequests);
    latch.countDown();
  }

  private boolean doMatch(MatchesApi matchesApi, String swiperIdStr) {
    int retryCount = 0;
    while (retryCount < 5) {
      try {
        long startTime = System.currentTimeMillis();
        ApiResponse<Matches> response = matchesApi.matchesWithHttpInfo(swiperIdStr);
        if (response.getStatusCode() == 200) {
          long endTime = System.currentTimeMillis();
          Record requestRecord = new Record(startTime, "GET",
                  endTime - startTime, Integer.toString(response.getStatusCode()));
          this.requestRecords.add(requestRecord);
          successfulRequestsCount.incrementAndGet();
          return true;
        }
      } catch (ApiException e) {
        retryCount++;
        e.printStackTrace();
      }
    }
    failedRequestsCount.incrementAndGet();
    return false;
  }

  private boolean doStats(StatsApi statsApi, String swiperIdStr) {
    int times = 0;
    while(times < 5) {
      try {
        long start = System.currentTimeMillis();
        ApiResponse<MatchStats> res = statsApi.matchStatsWithHttpInfo(swiperIdStr);
        if(res.getStatusCode() == 200) {
          long end = System.currentTimeMillis();
          System.out.println("here");
          System.out.println(end - start);
          Record record = new Record(start,"GET", end - start,"200");
          this.requestRecords.add(record);
          return true;
        }
      } catch (ApiException e) {
        times++;
        e.printStackTrace();
      }
    }
    return false;
}
}
