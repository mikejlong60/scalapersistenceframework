/*******************************************************************************
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
 ***************************************************************************** */
package org.scalapersistenceframework.hadoop.hbase.dao

import scala.collection.JavaConversions.asScalaIterator
import scala.collection.JavaConversions.seqAsJavaList
import scala.util.Random

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.hbase.HTableDescriptor
import org.apache.hadoop.hbase.client.Delete
import org.apache.hadoop.hbase.client.Get
import org.apache.hadoop.hbase.client.HBaseAdmin
import org.apache.hadoop.hbase.client.HTable
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.client.Result
import org.apache.hadoop.hbase.client.Row
import org.apache.hadoop.hbase.client.Scan
import org.apache.hadoop.hbase.util.Bytes

class HTableDao(val config: Configuration, val tableName: String) {

  // The following 
  private val admin = new HBaseAdmin(config)
  private val htd = new HTableDescriptor(tableName) //The HBase table name
  private val tableNameInternal = htd.getName() //Gets the table with the column family you specified. 

  private val hTable = new HTable(config, tableNameInternal) //Initializes the reference to the table so we can do things to the 
  //data that it holds. This is an expensive operation because it checks the HBase metadata to make sure the table
  //exists. 
  hTable.setAutoFlush(false)
  private val random = new Random(System.currentTimeMillis)

  /**
   * Return the contents of the HBase  table subject to the given scan.
   *
   * @return the  table in ascending rowKey order.
   */
  def scan(scan: Scan): List[Result] = {

    var scanner = hTable.getScanner(scan)
    try {
      scanner = hTable.getScanner(scan)
      scanner.iterator.toList
    } finally {
      scanner.close
    }
  }

  /**
   * Returns a HBase result object containing the row and column family fields.
   *
   * @return the row pointed to by rowKey. Use the isEmpty method on the Result class
   * to see if the row exists.
   */
  def findByKey(rowKey: String): Result = {
    val get = new Get(Bytes.toBytes(rowKey))
    val result = hTable.get(get)
    result
  }

  /**
   * Deletes all the row pointed to by rowKey including all its column families.
   */
  def deleteByKey(rowKey: String) {
    val delete = new Delete(Bytes.toBytes(rowKey))
    val result = hTable.delete(delete)
    hTable.flushCommits
  }

  /**
   * Inserts a new object into the HBase  table.  It creates the row with a
   * key of the current time stamp and a random char and returns that key to the caller.
   *
   * @param columnGroup - the column family that the columnName attribute belongs to
   * @param columnName - the columnName within the above column family
   * @param columnValue - the value for that column.
   *
   * @return - the key of the row just inserted
   */
  def insert(columnGroup: String, columnName: String, columnValue: String): String = {

    val rowKey = System.nanoTime.toString + random.nextPrintableChar.toString
    val row = Bytes.toBytes(rowKey)
    val p = new Put(row)

    val databytes = Bytes.toBytes(columnGroup)
    p.add(databytes, Bytes.toBytes(columnName), Bytes.toBytes(columnValue))
    p.add(databytes, Bytes.toBytes("update_ts"), Bytes.toBytes(System.currentTimeMillis()))
    hTable.put(p)
    hTable.flushCommits
    rowKey
  }

  /**
   * Inserts, Updates, Deletes, or gets the given list of rows to/from the HBase  table. The list may include Get,
   * Put, and Delete objects.  If Put, or Delete it inserts or updates or deletes the row from the table in HBase.
   * If Get it returns the row by key in the result.
   *
   * @param rows - a list of Rows(Get, Put, Delete)
   *
   * @return an array of empty objects that matches the size of the rows parameter for Put or Delete requests,
   * or the row for Get requests assuming the row exists in the table.
   */
  def batch(rows: List[Row]): Array[Object] = {
    try {
      hTable.batch(rows)
    } finally {
      hTable.flushCommits
    }
  }

  /**
   * Updates the row pointed to by rowKey with the columnValue.
   *
   * @param columnGroup - the column family that the columnName attribute belongs to
   * @param columnName - the columnName within the above column family
   * @param columnValue - the value for that column.
   */
  def update(rowKey: String, columnGroup: String, columnName: String, columnValue: String) {

    val row = Bytes.toBytes(rowKey)
    val p = new Put(row)

    val databytes = Bytes.toBytes(columnGroup)
    p.add(databytes, Bytes.toBytes(columnName), Bytes.toBytes(columnValue))
    p.add(databytes, Bytes.toBytes("update_ts"), Bytes.toBytes(System.nanoTime))
    hTable.put(p)
    hTable.flushCommits
  }

  /**
   * Updates the row pointed to by rowKey with the columnValue if the value of the column is equal
   * to the passed value. This is how you do optimistic locking in HBase.
   *
   * @param columnGroup - the column family that the columnName attribute belongs to
   * @param columnName - the columnName within the above column family
   * @param columnValue - the value for that column.
   * @param valueToCompareAgainst - The value to compare with the current value of that column for the given row
   *
   * @return true if the current value of the row is equal to the valueToCompareAgainst and the row was successfully updated.
   * false otherwise.
   */
  def compareAndUpdate(rowKey: String, columnGroup: String, columnName: String, columnValue: String, valueToCompareAgainst: String): Boolean = {

    val row = Bytes.toBytes(rowKey)
    val p = new Put(row)

    val databytes = Bytes.toBytes(columnGroup)
    p.add(databytes, Bytes.toBytes(columnName), Bytes.toBytes(columnValue))
    val result = hTable.checkAndPut(Bytes.toBytes(rowKey), Bytes.toBytes(columnGroup), Bytes.toBytes(columnName), Bytes.toBytes(valueToCompareAgainst), p)
    hTable.flushCommits
    result
  }

}