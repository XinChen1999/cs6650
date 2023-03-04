package Model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class LikesInfo {

    private final ConcurrentHashMap<String, Integer> likesCollection;
    private final ConcurrentHashMap<String, Integer> dislikesCollection;
    private final ConcurrentHashMap<String, List<String>> likedUsers;

    public LikesInfo() {
        likesCollection = new ConcurrentHashMap<>();
        dislikesCollection = new ConcurrentHashMap<>();
        likedUsers = new ConcurrentHashMap<>();
    }

    public void swipe(String action, String swiper, String swipee) {
        if (action.equals("right")) {
            likesCollection.put(swiper, likesCollection.getOrDefault(swiper, 0) + 1);
            if (!likedUsers.containsKey(swiper)) {
                likedUsers.put(swiper, Collections.synchronizedList(new ArrayList<>()));
                likedUsers.get(swiper).add(swipee);
            }
        } else {
            dislikesCollection.put(swiper, dislikesCollection.getOrDefault(swiper, 0) + 1);
        }
    }

    public Integer getLikes(String swiper) {
        return likesCollection.get(swiper);
    }

    public Integer getDislikes(String swiper) {
        return dislikesCollection.get(swiper);
    }

    public List<String> getTop100LikedUsers(String swiper) {
        return new ArrayList<>();
    }
}
