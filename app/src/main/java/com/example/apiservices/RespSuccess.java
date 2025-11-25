package blogtalk.com.apiservices;

import blogtalk.com.items.ItemSuccess;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class RespSuccess {

    @JsonAdapter(MyListAdapterFactory.class)
    @SerializedName("VIDEO_STATUS_APP")
    Object itemSuccess;

    @SerializedName("success")
    String success;

    @SerializedName("msg")
    String message;

    @SerializedName("type")
    String type;

    public String getSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public ItemSuccess getItemSuccess() {
        return itemSuccess instanceof ItemSuccess ? (ItemSuccess) itemSuccess : null;
    }

    public static class MyListAdapterFactory implements com.google.gson.TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() == Object.class) {
                return (TypeAdapter<T>) new MyListAdapter(gson);
            }
            return null;
        }
    }

    public static class MyListAdapter extends TypeAdapter<Object> {

        private final Gson gson;

        public MyListAdapter(Gson gson) {
            this.gson = gson;
        }

        @Override
        public void write(JsonWriter out, Object value) {
            // Not needed for serialization
        }

        @Override
        public Object read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            if (token == JsonToken.BEGIN_ARRAY) {
                Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                return gson.fromJson(in, listType);
            } else if (token == JsonToken.BEGIN_OBJECT) {
                Type listType = new TypeToken<ItemSuccess>() {}.getType();
                return gson.fromJson(in, listType);
            }
            return null;
        }
    }
}