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

class SchemaTest extends FunSuite with DataSourceConfigurer with BeforeAndAfterEach {
  override val logger = Logger.getLogger(this.getClass().getName())

  override def beforeEach {
    super.configureJdbc
  }

  override def afterEach {
    super.cleanupTransactions
  }

  test("Test All aspects of of creating a populating a jdbc metadata schema") {
    transPropagation enclose (None,
      {
        val conn = Transaction.getInstance.getConnectionForTransaction
        val t = new Schema(conn, "spf") 
        expectResult("Spf") { t.scalaName }
        expectResult("spf") { t.schema }
        expectResult("spf") { t.variableName }

        expectResult(2) { t.tables.size }
        var table = t.getTable("order").get
        expectResult("Order") { table.scalaName }
        expectResult("order") { table.sqlName }
        expectResult("spf") { table.schema }
        expectResult("order") { table.variableName }
        expectResult(8) { table.getColumns.size }
        
        table = t.getTable("address").get
        expectResult("Address") { table.scalaName }
        expectResult("address") { table.sqlName }
        expectResult("spf") { table.schema }
        expectResult("address") { table.variableName }
        expectResult(8) { table.getColumns.size }
        expectResult(None) { t.getTable("bogus") }
        expectResult(None) { t.getTable("") }
        expectResult(None) { t.getTable(null) }
      })
  }
}