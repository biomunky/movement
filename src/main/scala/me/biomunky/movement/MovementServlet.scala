package me.biomunky.movement

import _root_.akka.actor.ActorSystem
import me.biomunky.movement.amazon._
import java.util.UUID
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import scala.concurrent.ExecutionContext

class MovementServlet()(implicit system: ActorSystem) extends MovementStack {
  protected implicit def executor: ExecutionContext = system.dispatcher

  get("/") {
    <html>
      <body>
        <h1>Hello, world!</h1>
        Say <a href="hello-scalate">hello to Scalate</a>.
      </body>
    </html>
  }

  val appName = "data_proxy_hack_local" + UUID.randomUUID.toString
  val workerId = UUID.randomUUID.toString
  val streamName = "serverside_stream_live"
  val credentials = new BasicAWSCredentialsProvider("accessKey", "secretKey")

  val kinesisClientLibConfiguration = new KinesisClientLibConfiguration(
    appName,
    streamName,
    credentials,
    workerId
  )

  def printThings(s: String): Unit = println(s)

  val kc = KinesisConsumer.withFunction(kinesisClientLibConfiguration, printThings)
  kc.start()

  override def destroy(): Unit = kc.stop()
}