package examples.hadoop

import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Reducer

import org.scalapersistenceframework.hadoop.util.SHadoop.int2writable
import org.scalapersistenceframework.hadoop.util.SHadoop.javaIterable2IntIterator

class MaxTemperatureReducer extends Reducer[Text, IntWritable, Text, IntWritable] {

  override def reduce(key: Text, values: java.lang.Iterable[IntWritable], context: Reducer[Text, IntWritable, Text, IntWritable]#Context): Unit = {
    val max = values.reduceLeft((x, y) => x max y)
    context.write(key, max)
  }
}
