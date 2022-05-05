import com.mindarray.APIServer;
import com.mindarray.DatabaseEngine;
import com.mindarray.DiscoveryEngine;
import com.mindarray.PollerEngine;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;

public class Bootstrap {
    public static final Vertx vertx = Vertx.vertx();

    public static void main(String[] args) {

        start(APIServer.class.getName())
                .compose(future -> start(DatabaseEngine.class.getName()))

                .compose(future -> start(DiscoveryEngine.class.getName()))

                .compose(future -> start(PollerEngine.class.getName()))

                .onComplete(handler -> {

                    if (handler.succeeded()) {

                        System.out.println("deployed successfully");

                    } else {
                        System.out.println("Not deployed");
                    }

                });
    }

    public static Future<Void> start(String verticle) {

        Promise<Void> promise = Promise.promise();

        vertx.deployVerticle(verticle, handler -> {

            if (handler.succeeded()) {

                promise.complete();

            } else {

                System.out.println("Failed");

                promise.fail(handler.cause());
            }
        });
        return promise.future();
    }


}
