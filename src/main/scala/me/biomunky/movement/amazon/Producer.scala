package me.biomunky.movement.amazon

import org.slf4j.LoggerFactory
import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import com.amazonaws.services.kinesis.AmazonKinesisClient
import java.nio.ByteBuffer
import com.amazonaws.services.kinesis.model.PutRecordRequest


object KinesisSinkActor {
  def fromConfig(implicit system: ActorSystem): ActorRef = {
    system.actorOf(
      Props(
        new KinesisSinkActor(
          "accessKey",
          "secretKey",
          "streamName"
        )
      )
    )
  }
}

class KinesisSinkActor(
  accessKey: String,
  secretKey: String,
  streamName: String) extends Actor {

  val log = LoggerFactory getLogger this.getClass

  def createCredentials = new BasicAWSCredentialsProvider(accessKey, secretKey)

  def createRecord(partitionKey: String, messageByteArray: ByteBuffer): PutRecordRequest = {
    val request = new PutRecordRequest()
    request.setData(messageByteArray)
    request.setStreamName(streamName)
    request.setPartitionKey(partitionKey)
    request
  }

  def sendToKinesis(payload: String) {
    val ba = payload map {_.toByte} toArray
    val partitionKey = "definedByPayload"
    val recordToSend = createRecord(partitionKey, ByteBuffer.wrap(ba))
    kinesisClient.putRecord(recordToSend)
  }

  def receive: Receive = {
    case s: String => sendToKinesis(s)
    case e        => log.error(s"$e is not a valid message")
  }

  val kinesisClient = new AmazonKinesisClient(createCredentials)
}
