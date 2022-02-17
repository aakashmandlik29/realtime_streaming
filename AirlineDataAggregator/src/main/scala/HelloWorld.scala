import org.apache.spark.sql.SparkSession

object Hello {
  def main(args: Array[String]) = {
    println("Hello, world")
    val spark= SparkSession.builder()
      .master("spark://adeb3d10a03b:7077")
      .appName("Spark Streaming Example")
      .getOrCreate()

    val df = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "127.0.0.1:9092")
      .option("subscribe", "airlines")
      .option("startingOffsets", "earliest") // From starting
      .load()

    df.show()
  }
}