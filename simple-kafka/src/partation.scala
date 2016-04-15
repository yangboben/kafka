import kafka.utils.VerifiableProperties

/**
  * Created by shentao on 16-4-12.
  */
class partation(props: VerifiableProperties) extends kafka.producer.Partitioner[String]{
  override def partition(key: String, numPartitions: Int): Int = {

     if(key.charAt(0).isLetter)
        return  1;
     else
        return  0;


  }
}
