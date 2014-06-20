import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration
import java.util.UUID
import me.biomunky.movement.amazon.{BasicAWSCredentialsProvider, KinesisConsumer}
import scala.concurrent._
import ExecutionContext.Implicits.global

object Consumer {
  def main(args: Array[String]): Unit = {
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
  }

}