package examples.hadoop

import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.LongWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Mapper

class MaxTemperatureMapper extends Mapper[LongWritable, Text, Text, IntWritable] {

	private val MISSING = 9999

	override def map(key: LongWritable, value:Text, context:Mapper[LongWritable,Text, Text, IntWritable]#Context): Unit = {

		val line = value.toString()
		val year = line.substring(15, 19)
		var airTemperature = 0
		if (line.charAt(87) == '+') { // parseInt doesn't like leading plus
			airTemperature = Integer.parseInt(line.substring(88, 92))
		} else {
			airTemperature = Integer.parseInt(line.substring(87, 92))
		}
		val quality = line.substring(92, 93)
		if (airTemperature != MISSING && quality.matches("[01459]")) {
			context.write(new Text(year), new IntWritable(airTemperature))
		}
	}
}
