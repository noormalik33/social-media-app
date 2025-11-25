package blogtalk.com.interfaces;

import blogtalk.com.items.ItemComments;

import java.util.ArrayList;

public interface EditCommentListener {
    void onEdit(int pos);

    void onDelete();
}
