import com.motadata.interpreter.Discovery;
import com.motadata.interpreter.MainVerticle;
import com.motadata.repo.DiscoveryRepo;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import java.sql.SQLException;

public class Bootstrap {
    public static void main(String[] args) throws Exception {

        MainVerticle mainVerticle = new MainVerticle();

        Vertx vertx = Vertx.vertx();

        vertx.deployVerticle(new MainVerticle()).onComplete(h1->{

            try {

                vertx.deployVerticle(new DiscoveryRepo() ,new DeploymentOptions().setWorker(true)).onComplete(h2->{

                    try {

                        vertx.deployVerticle(new Discovery(), new DeploymentOptions().setWorker(true)).onComplete(AsyncResult::succeeded);

                    }

                    catch (SQLException | ClassNotFoundException e) {

                        throw new RuntimeException(e);
                    }

                });

            }

            catch (SQLException | ClassNotFoundException e) {

                throw new RuntimeException(e);

            }

        });

        mainVerticle.start();
    }
}
