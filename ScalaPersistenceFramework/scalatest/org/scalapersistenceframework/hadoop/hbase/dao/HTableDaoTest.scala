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
package org.scalapersistenceframework.hadoop.hbase.dao

import java.util.logging.Logger

import scala.util.Random

import org.apache.hadoop.hbase.HBaseConfiguration
import org.apache.hadoop.hbase.client.Delete
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.Row
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.filter.PageFilter
import org.apache.hadoop.hbase.util.Bytes
import org.scalatest.FunSuite

class HTableDaoTest extends FunSuite {
  val logger = Logger.getLogger(HTableDaoTest.this.getClass().getName())

  test("Test scan starting at specific row")({
    val orderDao = new HTableDao(HBaseConfiguration.create(), "order")
    val rowKey = orderDao.insert("orderFamily", "description", "another test order") //columnGroup:String, columnName:String, columnValue:String

    val scan = new Scan
    scan.setStartRow(Bytes.toBytes(rowKey))
    val result = orderDao.scan(scan)
    expectResult(1) { result.length }
  })

  test("Test scan with page pagination filter")({
    val orderDao = new HTableDao(HBaseConfiguration.create(), "order")
    //Insert 500000 rows to test pagination
    val start = System.currentTimeMillis
    for (i <- 1 to 50000) orderDao.insert("orderFamily", "description", "another test order")
    println("took [" + (System.currentTimeMillis - start) + "ms] to insert one at a time")
    val scan = new Scan
    val paginationFilter = new PageFilter(50)
    scan setFilter (paginationFilter)
    val result = orderDao.scan(scan)
    expectResult(50) { result.length }
  })

  test("Test FindByKey")({
    val orderDao = new HTableDao(HBaseConfiguration.create(), "order")
    val rowKey = orderDao.insert("orderFamily", "description", "Lowes wheelbarrow order") //columnGroup:String, columnName:String, columnValue:String
    expectResult(true) { rowKey.length > 10 }

    val result = orderDao.findByKey(rowKey)
    expectResult(false) { result.isEmpty }
  })

  test("Test deleteByKey")({
    val orderDao = new HTableDao(HBaseConfiguration.create(), "order")
    val rowKey = orderDao.insert("orderFamily", "description", "Lowes wheelbarrow order") //columnGroup:String, columnName:String, columnValue:String
    expectResult(true) { rowKey.length > 10 }

    orderDao.deleteByKey(rowKey)
    val result = orderDao.findByKey(rowKey)
    expectResult(true) { result.isEmpty }
  })

  test("Test Batch API")({
    val orderDao = new HTableDao(HBaseConfiguration.create(), "order")
    var batch = scala.collection.mutable.ListBuffer.empty[Row]
    var start = System.currentTimeMillis
    val random = new Random(System.currentTimeMillis)
    var rowKey = System.nanoTime.toString + random.nextPrintableChar.toString
    val firstRowKey = rowKey
    for (i <- 1 to 50000) {
      rowKey = System.nanoTime.toString + random.nextPrintableChar.toString
      val p = new Put(Bytes.toBytes(rowKey))
      val databytes = Bytes.toBytes("orderFamily")
      p.add(databytes, Bytes.toBytes("description"), Bytes.toBytes("lowes item" + rowKey))
      p.add(databytes, Bytes.toBytes("update_ts"), Bytes.toBytes(System.currentTimeMillis))
      batch += p
    }
    orderDao.batch(batch.toList)
    println("took [" + (System.currentTimeMillis - start) + "ms] to insert in batch")

    //Now get the last row you just inserted using the batch API
    batch = scala.collection.mutable.ListBuffer.empty[Row]
    val get = new Get(Bytes.toBytes(rowKey))
    batch += get
    val batchGetResult = orderDao.batch(batch.toList)
    expectResult(1) { batchGetResult.length }

    //Now make sure all 50,000 rows got inserted
    val scan = new Scan
    scan.setStartRow(Bytes.toBytes(firstRowKey))
    var result = orderDao.scan(scan)
    expectResult(50000) { result.length }

    //Now use the batch API to delete the first row you inserted
    batch = scala.collection.mutable.ListBuffer.empty[Row]
    val row = Bytes.toBytes(firstRowKey)
    val d = new Delete(Bytes.toBytes(rowKey))
    batch += d
    //And verify that it got deleted
    orderDao.findByKey(firstRowKey)
    val result2 = orderDao.findByKey(firstRowKey)
    expectResult(true) { result2.isEmpty }

  })

  test("Test that the columnFamily name gets validated")({
    intercept[IllegalArgumentException] {
      val orderDao = new HTableDao(HBaseConfiguration.create(), "order")
      orderDao.insert("badFamilyName", "description", "Lowes wheelbarrow order")
    }
  })

  test("Test compareAndUpdate")({
    val orderDao = new HTableDao(HBaseConfiguration.create(), "order")
    val rowKey = orderDao.insert("orderFamily", "description", "Lowes wheelbarrow order") //columnGroup:String, columnName:String, columnValue:String
    expectResult(true) { rowKey.length > 10 }

    val result = orderDao.findByKey(rowKey)
    expectResult(false) { result.isEmpty }
    expectResult(true) { orderDao.compareAndUpdate(rowKey, "orderFamily", "description", "Lowes wheelbarrow order update", "Lowes wheelbarrow order") }
    expectResult(false) { orderDao.compareAndUpdate(rowKey, "orderFamily", "description", "Lowes wheelbarrow order update", "fred") }
  })

  test("Test getColumnFamilies")({
    val orderDao = new HTableDao(HBaseConfiguration.create(), "order")
    orderDao.columnFamilies.foreach(family => expectResult(true) {
      family.getNameAsString match {
        case "orderFamily" => true
        case "addressFamily" => true
        case _ => false
      }
    })
  })

  test("Test getTables")({
    val orderDao = new HTableDao(HBaseConfiguration.create(), "order")
    orderDao.tables.foreach(table => expectResult(true) {
      table.getNameAsString match {
        case "order" => true
        case "address" => true
        case "maxtemperature" => true
        case _ => false
      }
    })
  })
}