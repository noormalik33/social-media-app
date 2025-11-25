package com.example.apiservices;

import com.example.items.ItemUser;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;

public class RespUserList implements Serializable {


    @JsonAdapter(MyListAdapterFactory.class)
    @SerializedName("VIDEO_STATUS_APP")
    Object itemUser;

    @SerializedName("msg")
    String message;

    @SerializedName("success")
    String success;

    public ItemUser getUserDetail() {
        return itemUser instanceof ItemUser ? (ItemUser) itemUser : null;
    }

//    public ArrayList<ItemUser> getUserArrayDetail() {
//        return itemUser instanceof ArrayList ? (ArrayList<ItemUser>) itemUser : null;
//    }

    public String getMessage() {
        return message;
    }

    public String getSuccess() {
        return success;
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
                Type listType = new TypeToken<ItemUser>() {}.getType();
                return gson.fromJson(in, listType);
            }
            return null;
        }
    }
}