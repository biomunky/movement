package me.biomunky.movement.amazon

import com.amazonaws.auth.{AWSCredentialsProvider, BasicAWSCredentials, AWSCredentials}
import org.slf4j.LoggerFactory

class BasicAWSCredentialsProvider(accessKey: String, secretKey: String) extends AWSCredentialsProvider {

  val log = LoggerFactory getLogger this.getClass

  def getCredentials: AWSCredentials = basicCreds

  def refresh: Unit = {}

  private val basicCreds = new BasicAWSCredentials(accessKey, secretKey)
}