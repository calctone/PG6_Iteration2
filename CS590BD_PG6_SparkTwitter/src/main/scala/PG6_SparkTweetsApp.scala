import org.apache.spark.SparkConf
import org.apache.spark.streaming.twitter.TwitterUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scalaj.http.{Http, HttpOptions}

/**
 * Created by Jeff Lanning on 07/07/15.
 */
object PG6_SparkTweetsApp {
  def main(args: Array[String]) {
    PG6_ScalaHelper.setStreamingLogLevels()

    val filters = Array("#kc", "#kcpolice", "#kcmo", "#kansascity", "#UMKC_SG3")

    // Set the system properties so that Twitter4j library used by twitter stream
    // can use them to generate OAuth credentials
    /*System.setProperty("twitter4j.oauth.consumerKey", "K5irO3T6OnBimYiLwKI1aDPv0")
    System.setProperty("twitter4j.oauth.consumerSecret", "sswoK3Dgjpr17AAUaWlQyfLdFpA0ENEs11wDoCQ2ahghcAaZvu")
    System.setProperty("twitter4j.oauth.accessToken", "3248175864-yiPSna2GQo0b3WHUSHPWeFl0kHjmb4zBPy648A4")
    System.setProperty("twitter4j.oauth.accessTokenSecret", "ZAVqYA8UTavzk0gg9I1ksthmq404LZtsoXvpbFuLBHJwr")
*/

    // Configure Twitter credentials using twitter.txt
    PG6_ScalaHelper.configureTwitterCredentials()

    //Create a spark configuration with a custom name and master
    // For more master configuration see  https://spark.apache.org/docs/1.4.0/submitting-applications.html#master-urls
    val sparkConf = new SparkConf().setAppName("STweetsApp").setMaster("local[*]")

    //Create a Streaming Context with 2 second window
    val ssc = new StreamingContext(sparkConf, Seconds(2))

    /*
    // Create DStream that will connect to hostname:port, like localhost:9999
    val lines = ssc.socketTextStream("localhost", 9999)

    // Split lines into words
    val words = lines.flatMap(_.split(" "))

    // Count each word in each batch
    val pairs = words.map(word => (word, 1))
    val wordCounts = pairs.reduceByKey(_ + _)

    // Print the first ten elements of each RDD generated in this DStream to the console
    wordCounts.print()

    ssc.start()
    ssc.awaitTermination()
  }
    */

    //Using the streaming context, open a twitter stream (By the way you can also use filters)
    //Stream generates a series of random tweets
    val stream = TwitterUtils.createStream(ssc, None, filters)

    //Map : Retrieving Hash Tags
    val hashTags = stream.flatMap(status => status.getText.split(" ").filter(_.startsWith("#")))

    //Finding the top hash Tgas on 10 second window
    val topCounts10 = hashTags.map((_, 1)).reduceByKeyAndWindow(_ + _, Seconds(10))
      .map { case (topic, count) => (count, topic) }
      .transform(_.sortByKey(false))

    topCounts10.foreachRDD(rdd => {
      var jsonTopics = Array[String]()
      val topList = rdd.take(10)
      println("\nPopular topics in last 10 seconds (%s total):".format(rdd.count()))
      topList.foreach { case (count, tag) => println("%s (%s tweets)".format(tag, count)) }

      topList.foreach {
         case (count, tag) =>
           val jsonTopic = """%s: """.format(tag) + """%s (tweets)""".format(count)
           jsonTopics = jsonTopics :+ jsonTopic
      }

      if (!jsonTopics.isEmpty) {

        var jsonItems = ""
        var index = 1
        jsonTopics.foreach {
          case (topic) =>
            jsonItems += topic
            if (index < jsonTopics.length) {
              jsonItems += ","
            }
            println("\nIndex: " + index + " Size: " + jsonTopics.length)
            index += 1
        }

        val jsonHeaders = """{"topTopics":[""".concat( """"""").concat(jsonItems).concat( """"""").concat( """]}""")
        println("\nTWITTER DATA: %s".format(jsonHeaders))
        //val jsonHeaders = """{"jsonrpc": "2.0", "method": "someMethod", "params": {"dataIds":["12348" , "456"]}, "data2": "777"}"""

        val url = "https://api.mongolab.com/api/1/databases/cs590_sg3/collections/TwitterData?apiKey=j-xyG_r8AOd5fKxRNNGlmsS9vradUXAU"
        val result = Http(url).postData(jsonHeaders)
          .header("content-type", "application/json")
          .header("X-Application", "myCode")
          .header("X-Authentication", "myCode2")
          .option(HttpOptions.connTimeout(10000)).option(HttpOptions.readTimeout(50000))
          .asString

        println("\n" + result)
      }
    })

    ssc.start()

    ssc.awaitTermination()
  }
}
