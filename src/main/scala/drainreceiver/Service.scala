package drainreceiver

import com.github.vonnagy.service.container.ContainerBuilder
import drainreceiver.routes.DrainEndpoints

/**
 * Created by ivannagy on 4/8/15.
 */
object Service extends App {

  // Here we establish the container and build it while
  // applying extras.
  val service = new ContainerBuilder()
    .withRoutes(classOf[DrainEndpoints]).build

  service.start

}
