package pl.pgizka.photosmanager

import java.io.{File, IOException}
import java.nio.file.{Files, Paths}

import com.amazonaws.{AmazonClientException, AmazonServiceException}
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.{CannedAccessControlList, PutObjectRequest}
import com.rabbitmq.client.Channel
import com.rabbitmq.client.{AMQP, ConnectionFactory, DefaultConsumer, Envelope}

object S3 {

  def setup() = {

    val bucketName = "jnpprojectphotos"
    val s3 = new AmazonS3Client(new BasicAWSCredentials("", ""))

    import com.rabbitmq.client.ConnectionFactory
    val factory = new ConnectionFactory
    factory.setHost("rabbitmq")
    val conn = factory.newConnection

    val channel = conn.createChannel

    channel.exchangeDeclare("ech", "direct", true)
    val queueName = channel.queueDeclare.getQueue
    channel.queueBind(queueName, "ech", "key")

    import com.rabbitmq.client.AMQP
    import com.rabbitmq.client.DefaultConsumer
    import java.io.IOException
    val autoAck = false
    channel.basicConsume(queueName, autoAck, "myConsumerTag", new DefaultConsumer(channel) {
      @throws[IOException]
      override def handleDelivery(consumerTag: String, envelope: Envelope, properties: AMQP.BasicProperties, body: Array[Byte]): Unit = {
        val routingKey = envelope.getRoutingKey
        val contentType = properties.getContentType
        val deliveryTag = envelope.getDeliveryTag
        // (process the message components here ...)

        val photoId = properties.getHeaders.getOrDefault("id", "nic").toString
        println(s"got message from queue ${photoId}")
        //
        Files.write(Paths.get(photoId), body)
        val file = new File(photoId)

        try {
          s3.putObject(new PutObjectRequest(bucketName, photoId, file).withCannedAcl(CannedAccessControlList.PublicRead))
          file.delete()
          channel.basicAck(deliveryTag, false)
          println(s"photo ${photoId} uploaded")
        } catch {
          case ase: AmazonServiceException =>
            println("Caught an AmazonServiceException, which means your request made it " + "to Amazon S3, but was rejected with an error response for some reason.")
            println("Error Message:    " + ase.getMessage)
            println("HTTP Status Code: " + ase.getStatusCode)
            println("AWS Error Code:   " + ase.getErrorCode)
            println("Error Type:       " + ase.getErrorType)
            println("Request ID:       " + ase.getRequestId)
            file.delete()
          case ace: AmazonClientException =>
            println("Caught an AmazonClientException, which means the client encountered " + "a serious internal problem while trying to communicate with S3, " + "such as not being able to access the network.")
            println("Error Message: " + ace.getMessage)
            file.delete()
          case e: Throwable =>
            println(s"Unnknow exception occured $e")
            file.delete()

        }

      }
    })

  }


}