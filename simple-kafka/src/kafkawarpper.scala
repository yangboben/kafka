import java.io.{File, FileReader, BufferedReader}
import java.util
import java.util.Properties


import kafka.api.{OffsetRequest, PartitionOffsetRequestInfo, FetchRequestBuilder}
import kafka.common.TopicAndPartition
import kafka.consumer.{SimpleConsumer, Consumer, ConsumerConfig}
import kafka.producer.{KeyedMessage, ProducerConfig, Producer}
import kafka.serializer.StringEncoder

import scala.actors.scheduler.ExecutorScheduler
import scala.actors.threadpool.Executors


/**
  * Created by shentao on 16-4-8.
  */
object kafkawarpper {


  def sendmessage(): Unit ={

     val props = new Properties()

    props.put("metadata.broker.list","localhost:9092")
    //props.put("producerType","sync")
    props.put("serializer.class","kafka.serializer.StringEncoder")
    props.put("key.serializer.class", "kafka.serializer.StringEncoder")
    props.put("partitioner.class", "partation")
    props.put("request.required.acks", "1")

    val config = new ProducerConfig(props)

    val producer = new Producer[String,String](config)


    for( i <- 0 to 100){
      val record = new KeyedMessage[String,String]("hw",i+"",i+"")
      producer.send(record)
    }
    var c: Char='a'
    for(c <- 'a' to 'z'){
      val record = new KeyedMessage[String,String]("hw",c+"",c+"")
      producer.send(record)
    }
  }

  def sendmessage_sync(): Unit ={

    val props = new Properties()
    props.put("metadata.broker.list","localhost:9092")
    props.put("producer.type","sync")
    props.put("serializer.class","kafka.serializer.StringEncoder")
    props.put("key.serializer.class", "kafka.serializer.StringEncoder")
    props.put("partitioner.class", "partation")
    props.put("request.required.acks", "1")

    val config = new ProducerConfig(props)

    val producer = new Producer[String,String](config)

    val reader = new BufferedReader(new FileReader(new File("test")))
    val string = reader.readLine()
    val record = new KeyedMessage[String,String]("hw",string)

    producer.send(record)
  }

  //high-level consumer
  def get_message(): Unit ={
    val props = new Properties()
    props.put("zookeeper.connect", "localhost:2181");
    props.put("group.id", "0");
    props.put("auto.offset.reset", "smallest");
    props.put("zookeeper.session.timeout.ms", "400");
    props.put("zookeeper.sync.time.ms", "200");
    props.put("auto.commit.interval.ms", "1000");

    val config = new ConsumerConfig(props)

    val consumer = Consumer.create(config)
    val topicCountMap = Map("hw" -> 2)
    val ConsumerMap=consumer.createMessageStreams(topicCountMap)
    val streams = ConsumerMap.get("hw").get

     val stream = streams(1)
    //for(stream <- streams){
       val it = stream.iterator()
      while(it.hasNext()){

        System.out.println(new String(it.next().message));
      }
    //}
  }

  def get_message_simple(): Unit ={
    val topic = "hw"
    val leadBroker = "localhost"
    val port = 9092
    val clientname = "test"

    val partition1 = new thread_consumer(50,leadBroker,port,clientname,topic,0)
    val partition2 = new thread_consumer('h'-'a',leadBroker,port,clientname,topic,1)

    val threadpool = Executors.newFixedThreadPool(2)
    threadpool.execute(partition1)
    threadpool.execute(partition2)
  }

  def get_least_offset(consumer:SimpleConsumer,topic:String,partition:Int,whichtime:Long,clientname :String):Long ={
     val topicAndPartition = new TopicAndPartition(topic,partition)


       val requestinfo = Map(topicAndPartition -> new PartitionOffsetRequestInfo(whichtime,1))
       val request = new OffsetRequest(requestinfo,OffsetRequest.CurrentVersion,0,clientname,0)
       val response = consumer.getOffsetsBefore(request)

       val offsets = response.offsetsGroupedByTopic.get(topic)
       offsets.get(topicAndPartition).offsets(0)

  }
  class thread_consumer(val offset:Int,val leadBroker:String,val port:Int,val clientname:String,val topic:String,val partition:Int) extends Runnable {

    override def run(): Unit = {
      val consumer = new SimpleConsumer(leadBroker,port,10000,64*1024,clientname)

      var readoffset = get_least_offset(consumer,topic,partition,OffsetRequest.EarliestTime,clientname)+offset

      while(true){
        val req = new FetchRequestBuilder().clientId(clientname).addFetch(topic,partition,readoffset,1000000).build()
        val fetchResponse = consumer.fetch(req)
        for(messageAndOffset <- fetchResponse.messageSet(topic,partition)){
          readoffset = messageAndOffset.nextOffset
          var payload = messageAndOffset.message.payload
          val bytes = new Array[Byte](payload.limit())
          payload.get(bytes)
          System.out.println(new String(bytes,"UTF-8"));
        }
      }
    }
  }
}
