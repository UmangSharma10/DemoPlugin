import com.motadata.interpreter.Discovery;
import com.motadata.interpreter.MainVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class Bootstrap {
    public static void main(String[] args) throws Exception {
        MainVerticle mainVerticle = new MainVerticle();
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new Discovery(),new DeploymentOptions().setWorker(true));
        vertx.deployVerticle(new MainVerticle());

        mainVerticle.start();
    }
}
