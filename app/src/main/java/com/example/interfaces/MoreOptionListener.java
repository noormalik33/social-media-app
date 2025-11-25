package blogtalk.com.interfaces;

public interface MoreOptionListener {
    void onFavDone(String success, boolean isFav, int total);
    void onUserPostDelete();
}
