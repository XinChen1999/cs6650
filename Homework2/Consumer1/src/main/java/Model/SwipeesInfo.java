package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class SwipeesInfo {
    private final ConcurrentHashMap<String, Integer> likesReceived;
    private final ConcurrentHashMap<String, Integer> dislikesReceived;
    private final ConcurrentHashMap<String, List<String>> likedUsers;

    public SwipeesInfo() {
        likesReceived = new ConcurrentHashMap<>();
        dislikesReceived = new ConcurrentHashMap<>();
        likedUsers = new ConcurrentHashMap<>();
    }

    public void swipe(String action, String swiper, String swipee) {
        if (action.equals("right")) {
            likesReceived.put(swipee, likesReceived.getOrDefault(swipee, 0) + 1);
            if (!likedUsers.containsKey(swiper)) {
                likedUsers.put(swiper, Collections.synchronizedList(new ArrayList<>()));
                likedUsers.get(swiper).add(swipee);
            }
        }
        else {
            dislikesReceived.put(swipee, dislikesReceived.getOrDefault(swipee, 0) + 1);
        }
    }

    public List<String> getTop100LikedUsers(String swiper) {
        return new ArrayList<>();
    }
}
