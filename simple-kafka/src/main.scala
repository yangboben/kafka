import java.io.{FileWriter, BufferedWriter, File}

/**
  * Created by shentao on 16-4-8.
  */
object main{
      def main(args: Array[String]): Unit ={

        //kafkawarpper.sendmessage()


         //kafkawarpper.sendmessage_sync()

     //kafkawarpper.get_message()
       kafkawarpper.get_message_simple()
//         val file = new File("test")
//        val writer = new BufferedWriter(new FileWriter(file))
//
//        while(file.length()<200*1024*1024){
//          writer.write("test")
//        }
//        writer.flush()

      }
}
