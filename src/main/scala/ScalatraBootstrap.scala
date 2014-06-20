import akka.actor.ActorSystem
import me.biomunky.movement._
import org.scalatra._
import javax.servlet.ServletContext

class ScalatraBootstrap() extends LifeCycle {
  implicit val actorSystem = ActorSystem()
  override def init(context: ServletContext) {
    context.mount(new MovementServlet, "/")
  }
}
