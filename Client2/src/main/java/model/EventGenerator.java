package model;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadLocalRandom;

public class EventGenerator implements Runnable {
    private static final int MIN_SWIPEE_ID = 1;
    private static final int MAX_SWIPEE_ID = 1000000;

    private static final int MIN_SWIPER_ID = 1;
    private static final int MAX_SWIPER_ID = 5000;

    private static final int MIN_COMMENT_LENGTH = 1;
    private static final int MAX_COMMENT_LENGTH = 256;

    private final BlockingQueue<SwipeEvent> swipeEvents;
    private final int totalPostNum;

    public EventGenerator(BlockingQueue<SwipeEvent> swipeEvents, int totalPostNum) {
        this.swipeEvents = swipeEvents;
        this.totalPostNum = totalPostNum;
    }

    @Override
    public void run() {
        for (int i = 0; i < totalPostNum; i++) {
            String swipe = ThreadLocalRandom.current().nextInt(1, 3) == 1 ? "left" : "right";
            String swiper = Integer.toString(ThreadLocalRandom.current().nextInt(MIN_SWIPER_ID, MAX_SWIPER_ID + 1));
            String swipee = Integer.toString(ThreadLocalRandom.current().nextInt(MIN_SWIPEE_ID, MAX_SWIPEE_ID + 1));
            String comment = RandomStringUtils.randomAlphanumeric(ThreadLocalRandom.current().nextInt(MIN_COMMENT_LENGTH, MAX_COMMENT_LENGTH + 1));

            swipeEvents.offer(new SwipeEvent(swipe, swiper, swipee, comment));
        }
    }
}
