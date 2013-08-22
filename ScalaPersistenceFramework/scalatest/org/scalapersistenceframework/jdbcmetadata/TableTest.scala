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
package org.scalapersistenceframework.jdbcmetadata

import java.util.logging.Logger

import org.scalapersistenceframework.DataSourceConfigurer
import org.scalapersistenceframework.DefaultTransactionPrefs.transIsolationLevel
import org.scalapersistenceframework.DefaultTransactionPrefs.transPropagation
import org.scalapersistenceframework.Transaction
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite

class TableTest extends FunSuite with DataSourceConfigurer with BeforeAndAfterEach {
  override val logger = Logger.getLogger(this.getClass().getName())

  override def beforeEach {
    super.configureJdbc
  }

  override def afterEach {
    super.cleanupTransactions
  }

  test("Test All aspects of of creating a populating a jdbc metadata table") {
    transPropagation enclose (None,
      {
        val conn = Transaction.getInstance.getConnectionForTransaction
        val t = new Table(conn, "order", "spf") //val conn: Connection, val sqlName: String, val schema: String
        expectResult("Order") { t.scalaName }
        expectResult("spf") { t.schema }
        expectResult("order") { t.sqlName }
        expectResult("order") { t.variableName }
        expectResult(new Column(1, "numeric", "id", 12, 0, true, false)) { new Column(1, "numeric", "id", 12, 0, true, false) }

        expectResult(8) { t.getColumns.size }
        var col = t.getColumn("id").get
        expectResult(true) { col.isPk }
        expectResult(0) { col.decimalDigits }
        expectResult(false) { col.isNullable }
        expectResult("Id") { col.scalaName }
        expectResult(10) { col.size }
        expectResult("id") { col.sqlName }
        expectResult(4) { col.sqlType }
        expectResult("serial") { col.sqlTypeName }
        expectResult("id") { col.variableName }
        expectResult("java.lang.Long") { col.getScalaType }
        expectResult("nonNullableLong") { col.getScalaSelectMapper }

        col = t.getColumn("order_qty").get
        expectResult(false) { col.isPk }
        expectResult(0) { col.decimalDigits }
        expectResult(true) { col.isNullable }
        expectResult("OrderQty") { col.scalaName }
        expectResult(32) { col.size }
        expectResult("order_qty") { col.sqlName }
        expectResult(2) { col.sqlType }
        expectResult("numeric") { col.sqlTypeName }
        expectResult("orderQty") { col.variableName }
        expectResult("Option[Long]") { col.getScalaType }
        expectResult("nullableLong") { col.getScalaSelectMapper }

        col = t.getColumn("approved").get
        expectResult(false) { col.isPk }
        expectResult(0) { col.decimalDigits }
        expectResult(false) { col.isNullable }
        expectResult("Approved") { col.scalaName }
        expectResult(1) { col.size }
        expectResult("approved") { col.sqlName }
        expectResult(2) { col.sqlType }
        expectResult("numeric") { col.sqlTypeName }
        expectResult("approved") { col.variableName }
        expectResult("Boolean") { col.getScalaType }
        expectResult("nonNullableBoolean") { col.getScalaSelectMapper }

        col = t.getColumn("complete").get
        expectResult(false) { col.isPk }
        expectResult(0) { col.decimalDigits }
        expectResult(true) { col.isNullable }
        expectResult("Complete") { col.scalaName }
        expectResult(1) { col.size }
        expectResult("complete") { col.sqlName }
        expectResult(2) { col.sqlType }
        expectResult("numeric") { col.sqlTypeName }
        expectResult("complete") { col.variableName }
        expectResult("Option[Boolean]") { col.getScalaType }
        expectResult("nullableBoolean") { col.getScalaSelectMapper }

        col = t.getColumn("description").get
        expectResult(false) { col.isPk }
        expectResult(0) { col.decimalDigits }
        expectResult(true) { col.isNullable }
        expectResult("Description") { col.scalaName }
        expectResult(2000) { col.size }
        expectResult("description") { col.sqlName }
        expectResult(12) { col.sqlType }
        expectResult("varchar") { col.sqlTypeName }
        expectResult("description") { col.variableName }
        expectResult("Option[String]") { col.getScalaType }
        expectResult("nullableString") { col.getScalaSelectMapper }

        col = t.getColumn("customer_id").get
        expectResult(false) { col.isPk }
        expectResult(0) { col.decimalDigits }
        expectResult(false) { col.isNullable }
        expectResult("CustomerId") { col.scalaName }
        expectResult(32) { col.size }
        expectResult("customer_id") { col.sqlName }
        expectResult(2) { col.sqlType }
        expectResult("numeric") { col.sqlTypeName }
        expectResult("customerId") { col.variableName }
        expectResult("Long") { col.getScalaType }
        expectResult("nonNullableLong") { col.getScalaSelectMapper }

        col = t.getColumn("created_ts").get
        expectResult(false) { col.isPk }
        expectResult(6) { col.decimalDigits }
        expectResult(false) { col.isNullable }
        expectResult("CreatedTs") { col.scalaName }
        expectResult(29) { col.size }
        expectResult("created_ts") { col.sqlName }
        expectResult(93) { col.sqlType }
        expectResult("timestamp") { col.sqlTypeName }
        expectResult("createdTs") { col.variableName }
        expectResult("java.sql.Timestamp") { col.getScalaType }
        expectResult("nonNullableTimestamp") { col.getScalaSelectMapper }

        col = t.getColumn("updated_ts").get
        expectResult(false) { col.isPk }
        expectResult(6) { col.decimalDigits }
        expectResult(false) { col.isNullable }
        expectResult("UpdatedTs") { col.scalaName }
        expectResult(29) { col.size }
        expectResult("updated_ts") { col.sqlName }
        expectResult(93) { col.sqlType }
        expectResult("timestamp") { col.sqlTypeName }
        expectResult("updatedTs") { col.variableName }
        expectResult("java.sql.Timestamp") { col.getScalaType }
        expectResult("nonNullableTimestamp") { col.getScalaSelectMapper }

        expectResult(None) { t.getColumn("bogus") }
        expectResult(None) { t.getColumn("") }
        expectResult(None) { t.getColumn(null) }

        expectResult(1) { t.getPrimaryKeys.size }
        col = t.getColumn("id").get
        val pk = t.getPrimaryKeys.head
        assert(pk.sqlName == col.sqlName)
        assert(pk == col)
      })
  }
}