package examples.hadoop

import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Mapper
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.io.Writable
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.util.Bytes
import scala.collection.JavaConversions._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.hbase.client.Row

class MaxTemperatureHBaseMapper extends Mapper[LongWritable, Text, ImmutableBytesWritable, Writable] {

	private val MISSING = 9999

	override def map(offset: LongWritable, line:Text, context:Mapper[LongWritable,Text, ImmutableBytesWritable, Writable]#Context): Unit = {

		val lineString = line.toString()
		val rowKey = Bytes.toBytes(lineString.substring(15, 19))
		val put = new Put(rowKey)
		var airTemperature = 0
		if (line.charAt(87) == '+') { // parseInt doesn't like leading plus
			airTemperature = Integer.parseInt(lineString.substring(88, 92))
		} else {
			airTemperature = Integer.parseInt(lineString.substring(87, 92))
		}
		val quality = lineString.substring(92, 93)
		put.add(Bytes.toBytes("data"), Bytes.toBytes("airTemperature"), Bytes.toBytes(airTemperature))
		if (airTemperature != MISSING && quality.matches("[01459]")) {
			context.write(new ImmutableBytesWritable(rowKey), put)
		}
	}
}
