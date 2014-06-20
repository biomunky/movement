package me.biomunky.movement.amazon

import akka.actor.ActorRef
import com.amazonaws.services.kinesis.clientlibrary.interfaces.{IRecordProcessorCheckpointer, IRecordProcessor, IRecordProcessorFactory}
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason
import scala.collection.JavaConversions._
import com.amazonaws.services.kinesis.model.Record
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.{Worker, KinesisClientLibConfiguration}
import com.amazonaws.services.kinesis.metrics.impl.NullMetricsFactory
import com.amazonaws.services.kinesis.metrics.interfaces.IMetricsFactory
import scala.concurrent.{ExecutionContext, Future}

class DefaultProcessorFactory(fn: String => Unit) extends IRecordProcessorFactory {
  def createProcessor = new DefaultProcessor(fn)
}

class DefaultProcessor(fn: String => Unit) extends IRecordProcessor {
  lazy val log = LoggerFactory getLogger this.getClass
  private val decoder = Charset.forName("UTF-8").newDecoder()

  def initialize(shardId: String) = {}

  def shutdown(checkpointer: IRecordProcessorCheckpointer, reason: ShutdownReason) = reason match {
    case ShutdownReason.ZOMBIE =>
      log.error("Zombie process - shutdown called without checkpoint")
    case ShutdownReason.TERMINATE =>
      checkpointer.checkpoint()
      log.info("Terminate called, checkpoint initiated")
  }

  def processRecords(records: java.util.List[Record], checkpointer: IRecordProcessorCheckpointer) {
    log.debug("Process Records called")
    for (record <- records) {
      val recordData = record.getData
      val decoded = decoder.decode(recordData).toString
      log.debug("Calling fn(decoded)")
      fn(decoded)
    }
  }
}

object KinesisConsumer {
  def withActor(config: KinesisClientLibConfiguration, actor: ActorRef, metricsFactory: IMetricsFactory = new NullMetricsFactory())(implicit ec: ExecutionContext) =
    new KinesisConsumer(config, { actor ! _ }, metricsFactory)

  def withFunction(config: KinesisClientLibConfiguration, fn: String => Unit, metricsFactory: IMetricsFactory = new NullMetricsFactory())(implicit ec: ExecutionContext) =
    new KinesisConsumer(config, fn, metricsFactory)
}

class KinesisConsumer(
                       config: KinesisClientLibConfiguration,
                       fn: String => Unit,
                       metricsFactory: IMetricsFactory,
                       processorFactory: Option[IRecordProcessorFactory] = None
                       )(implicit ec: ExecutionContext) {
  lazy val log = LoggerFactory getLogger this.getClass
  val pf = processorFactory getOrElse new DefaultProcessorFactory(fn)
  val worker = new Worker(
    pf,
    config,
    metricsFactory
  )

  def start(): Unit = Future { log.debug("Starting worker"); worker.run() }

  def stop(): Unit = { log.debug("Stopping worker"); worker.shutdown() }
}