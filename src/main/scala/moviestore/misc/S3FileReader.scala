package moviestore.misc

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.S3ObjectInputStream
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConverters._

object S3FileReader {
  val logger: Logger = LoggerFactory.getLogger(S3FileReader.getClass)

  private lazy val s3Client = {
    val credentials = new DefaultAWSCredentialsProviderChain().getCredentials
    new AmazonS3Client(credentials)
  }

  def readData(bucketName: String, key: String): S3ObjectInputStream = {
    //we need bucket name and the resource key
    val `object` = s3Client.getObject(bucketName, key)
    `object`.getObjectContent
  }


  def readBucket(bucketName: String): Iterable[String] = {
    //we need bucket name and the resource key
    val `object` = s3Client.listObjects(bucketName)
    `object`.getObjectSummaries.asScala.map(summary => summary.getKey()).toIterable
  }

}
