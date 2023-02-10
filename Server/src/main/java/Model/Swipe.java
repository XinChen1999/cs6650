package Model;

public class Swipe {
    String swiper;
    String action;
    String swipee;
    String comment;

    protected Swipe(String swiper, String action, String swipee, String comment){
        this.action = action;
        this.swipee = swipee;
        this.swiper = swiper;
        this.comment = comment;
    }

    public String getSwiper() {
        return swiper;
    }

    public String getAction() {
        return action;
    }

    public String getSwipee() {
        return swipee;
    }

    public String getComment() {
        return comment;
    }

    public void setSwiper(String swiper) {
        this.swiper = swiper;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setSwipee(String swipee) {
        this.swipee = swipee;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
