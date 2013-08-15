/**
 * *****************************************************************************
 * Copyright 2013 Functionicity LLC, All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ****************************************************************************
 */
package examples.hadoop

import java.util.logging.Logger
import org.apache.hadoop.fs.Path
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.io.Writable
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.scalatest.FunSuite
import org.apache.hadoop.io.IntWritable
import java.io.File
import org.scalapersistenceframework.util.FileHelper.file2helper
import org.scalapersistenceframework.hadoop.hbase.dao.HTableDao
import org.apache.hadoop.hbase.HBaseConfiguration


class MaxTemperatureHBaseTest extends FunSuite {
  val logger = Logger.getLogger(this.getClass().getName())

  test("Test Simple MapReduce that reads data from a big file and puts it into an HBase table.")({
    //Get rid of old row so test tell the truth about inserting it.
    val maxtemperatureDao = new HTableDao(HBaseConfiguration.create(), "maxtemperature")
    maxtemperatureDao.deleteByKey("1949")
    expectResult(true) { maxtemperatureDao.findByKey("1949").isEmpty }

    //Create a new job to insert these rows from the weather file into the HBase maxtemperatrure table.
    //There are only 4 years of observations so the mapper initially inserts the 4 rows and
    //then updates them over and over again until the mapper has read every line
    //in the input file.
    val job = new Job();
    job.setJarByClass(this.getClass)

    FileInputFormat.addInputPath(job, new Path("hadoop_sampledata/weather_station_observations.txt"))

    job.setMapperClass(classOf[MaxTemperatureHBaseMapper])
    job.setOutputFormatClass(classOf[TableOutputFormat[IntWritable]])
    job.getConfiguration().set(TableOutputFormat.OUTPUT_TABLE, "maxtemperature")
    job.setOutputKeyClass(classOf[ImmutableBytesWritable])
    job.setOutputValueClass(classOf[Writable])
    
    //There are not any reducers since the MaxTempertureHBaseMapper writes to HBase directly.
    job.setNumReduceTasks(0)

    job.waitForCompletion(true)

    expectResult(false) { maxtemperatureDao.findByKey("1949").isEmpty }
  })
}