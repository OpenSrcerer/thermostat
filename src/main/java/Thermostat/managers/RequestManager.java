package thermostat.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import thermostat.thermoFunctions.commands.requestFactories.Request;
import thermostat.thermoFunctions.commands.requestListeners.RequestListener;

import javax.annotation.Nonnull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class RequestManager {

    private static final int nThreads = 5;
    private static final Logger lgr = LoggerFactory.getLogger(RequestManager.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(nThreads);
    private static final LinkedBlockingQueue<Request> requests = new LinkedBlockingQueue<>(nThreads);
    private static final RequestListener[] listeners = new RequestListener[12];

    static {
        Runnable runRequests = () -> {
            while (true) {
                Request request = null;

                try {
                    request = requests.take();
                } catch (InterruptedException ex) {
                    lgr.error("Thread interrupted while waiting for request.", ex);
                }

                if (request == null) {
                    return;
                }

                request.run();

                for (RequestListener listener : listeners) {
                    if (listener.getRequestType().equals(request.type)) {
                        if (request.isSuccessful()) {
                            listener.requestComplete();
                        } else {
                            listener.requestFailed();
                        }
                    }
                }
            }
        };

        for (int thread = 1; thread <= nThreads; ++thread) {
            executor.submit(runRequests);
        }
    }

    /**
     * Adds the listeners to the listeners array of active listeners.
     * @param listener Listener to be added.
     */
    public static void addListener(@Nonnull RequestListener... listener) {
        System.arraycopy(listener, 0, RequestManager.listeners, 0, 12);
    }

    /**
     * Adds the request to the requests array of active requests.
     * @param request Request to be added.
     */
    public static void queueRequest(@Nonnull Request request) throws InterruptedException {
        requests.put(request);
    }
}