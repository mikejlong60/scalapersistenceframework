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

import java.io.File
import java.util.logging.Logger

import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.IntWritable
import org.apache.hadoop.io.Text
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.scalatest.FunSuite
import org.scalapersistenceframework.util.FileHelper.file2helper


class MaxTemperatureTest extends FunSuite {
  val logger = Logger.getLogger(this.getClass().getName())

  test("Test Simple MapReduce")({
    val job = new Job();
    job.setJarByClass(this.getClass)

    FileInputFormat.addInputPath(job, new Path("hadoop_sampledata/weather_station_observations.txt"))
    FileOutputFormat.setOutputPath(job, new Path("output"))

    job.setMapperClass(classOf[MaxTemperatureMapper])
    job.setReducerClass(classOf[MaxTemperatureReducer])
    job.setCombinerClass(classOf[MaxTemperatureReducer])

    job.setOutputKeyClass(classOf[Text])
    job.setOutputValueClass(classOf[IntWritable])

    job.waitForCompletion(true)
  })

}