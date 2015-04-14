package drainreceiver.routes

import akka.actor.{ActorRefFactory, ActorSystem}
import com.github.vonnagy.service.container.http.routing.RoutedEndpoints
import com.github.vonnagy.service.container.log.LoggingAdapter
import com.github.vonnagy.service.container.metrics.{Counter, Meter}
import spray.http.StatusCodes
import scala.util.Try
import drainreceiver._

/**
 * Created by ivannagy on 4/13/15.
 */
class DrainEndpoints(implicit system: ActorSystem,
                     actorRefFactory: ActorRefFactory) extends RoutedEndpoints with LoggingAdapter {

  val logCount = Counter("http.log.receive")
  val logMeter = Meter("http.log.receive.meter")

  val route = {
    post {
      path("logs") {
        logRequest("log-received", akka.event.Logging.DebugLevel) {
          acceptableMediaTypes(drainreceiver.`application/logplex-1`) {
            requestEntityPresent {
              logplexMsgCount { msgCount =>
                logplexToken { token =>
                  logplexFrameId { frameId =>
                    entity(as[String]) { payload =>
                      noop { ctx =>
                        Try({
                          // Publish the batch to the waiting processor(s)
                          log.info(s"$token : $frameId : $msgCount")
                          // Increment the counter
                          logCount.incr
                          logMeter.mark
                          // Mark the request as complete
                          ctx.complete(StatusCodes.NoContent)
                        }) recover {
                          case e =>
                            log.error(s"Unable to handle the log: $logplexFrameId", e)
                            ctx.complete(StatusCodes.InternalServerError)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}