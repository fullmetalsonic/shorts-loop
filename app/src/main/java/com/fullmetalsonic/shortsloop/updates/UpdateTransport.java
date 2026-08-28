package com.fullmetalsonic.shortsloop.updates;

import com.fullmetalsonic.shortsloop.core.UpdatePolicy;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import javax.net.ssl.HttpsURLConnection;

/** Test seam, not a configurable URL or a bypass exposed by the product UI. */
public interface UpdateTransport {
    InputStream open(String url, BooleanSupplier cancelled) throws IOException;

    final class DefaultHttps implements UpdateTransport {
        private static final int IO_TIMEOUT_MS = 10_000;
        private static final int MAX_REDIRECTS = 5;
        private static final long TOTAL_NANOS = TimeUnit.SECONDS.toNanos(120);

        @Override public InputStream open(String url, BooleanSupplier cancelled) throws IOException {
            if (!GitHubUpdateClient.RELEASES_URL.equals(url) && !UpdatePolicy.trustedReleaseUrl(url))
                throw new UpdateFailure(UpdateFailure.Code.UNTRUSTED_URL);
            long deadline = System.nanoTime() + TOTAL_NANOS;
            HttpsURLConnection connection = null;
            try {
                String current = url;
                for (int redirects = 0; ; redirects++) {
                    check(cancelled, deadline);
                    connection = (HttpsURLConnection) new URL(current).openConnection();
                    connection.setInstanceFollowRedirects(false);
                    connection.setConnectTimeout(timeout(deadline));
                    connection.setReadTimeout(timeout(deadline));
                    connection.setUseCaches(false);
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "ShortsLoop-Updater");
                    connection.setRequestProperty("Accept", "application/vnd.github+json, application/json, application/octet-stream");
                    connection.setRequestProperty("Accept-Encoding", "identity");
                    int status = connection.getResponseCode();
                    check(cancelled, deadline);
                    if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                        if (redirects >= MAX_REDIRECTS) throw new UpdateFailure(UpdateFailure.Code.TOO_MANY_REDIRECTS);
                        String location = connection.getHeaderField("Location");
                        if (location == null || location.length() > 8192) throw new UpdateFailure(UpdateFailure.Code.INVALID_REDIRECT);
                        String next = new URL(new URL(current), location).toExternalForm();
                        if (!UpdatePolicy.trustedRedirectUrl(next)) throw new UpdateFailure(UpdateFailure.Code.UNTRUSTED_REDIRECT);
                        connection.disconnect(); connection = null;
                        current = next;
                        continue;
                    }
                    if (status != HttpsURLConnection.HTTP_OK) throw new UpdateFailure(UpdateFailure.Code.SERVER_RESPONSE);
                    String encoding = connection.getContentEncoding();
                    if (encoding != null && !"identity".equalsIgnoreCase(encoding))
                        throw new UpdateFailure(UpdateFailure.Code.TRANSFER_FORMAT);
                    InputStream body = connection.getInputStream();
                    HttpsURLConnection owned = connection;
                    connection = null; // Returned stream now owns and releases the connection.
                    return new FilterInputStream(body) {
                        @Override public int read() throws IOException {
                            check(cancelled, deadline); owned.setReadTimeout(timeout(deadline));
                            try {
                                int value = in.read(); check(cancelled, deadline); return value;
                            } catch (IOException error) { throw safe(error); }
                        }
                        @Override public int read(byte[] bytes, int offset, int length) throws IOException {
                            check(cancelled, deadline); owned.setReadTimeout(timeout(deadline));
                            try {
                                int count = in.read(bytes, offset, length); check(cancelled, deadline); return count;
                            } catch (IOException error) { throw safe(error); }
                        }
                        @Override public void close() throws IOException {
                            try { in.close(); }
                            catch (IOException ignored) { throw new UpdateFailure(UpdateFailure.Code.CONNECTION_CLOSE); }
                            finally { owned.disconnect(); }
                        }
                    };
                }
            } catch (IOException error) {
                throw safe(error);
            } catch (RuntimeException error) {
                throw new UpdateFailure(UpdateFailure.Code.CONNECTION);
            } finally {
                if (connection != null) connection.disconnect();
            }
        }

        private static int timeout(long deadline) throws IOException {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) throw new SocketTimeoutException("UPDATE_TIMEOUT");
            return (int) Math.max(1, Math.min(IO_TIMEOUT_MS, TimeUnit.NANOSECONDS.toMillis(remaining)));
        }
        private static void check(BooleanSupplier cancelled, long deadline) throws IOException {
            if (Thread.currentThread().isInterrupted() || (cancelled != null && cancelled.getAsBoolean()))
                throw new InterruptedIOException("UPDATE_CANCELLED");
            timeout(deadline);
        }
        private static IOException safe(IOException error) {
            if (error instanceof SocketTimeoutException) return new SocketTimeoutException("UPDATE_TIMEOUT");
            if (error instanceof InterruptedIOException) return new InterruptedIOException("UPDATE_CANCELLED");
            if (error instanceof UpdateFailure) return error;
            // Connection exceptions may contain signed redirect URLs. Never propagate those details.
            return new UpdateFailure(UpdateFailure.Code.CONNECTION);
        }
    }
}
