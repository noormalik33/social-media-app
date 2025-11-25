package com.example.utils;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

import java.io.IOException;

public class CountingRequestBody  extends RequestBody {
    protected RequestBody delegate;
    protected Listener listener;

    public CountingRequestBody(RequestBody delegate, Listener listener) {
        this.delegate = delegate;
        this.listener = listener;
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return delegate.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink bufferedSink = Okio.buffer(sink(sink));
        delegate.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    protected Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = -1L;

            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == -1) {
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
                listener.onProgress(bytesWritten, contentLength);
            }
        };
    }

    public interface Listener {
        void onProgress(long bytesWritten, long contentLength);
    }
}
