/*******************************************************************************
 * Copyright 2013 Michael J Long
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
package examples.storedprocedure

import java.util.logging.Logger

import org.scalapersistenceframework.DefaultTransactionPrefs.transIsolationLevel
import org.scalapersistenceframework.Transaction
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite

import org.scalapersistenceframework.DataSourceConfigurer

/**
 * All these test functions show how to use a DAO by itself wrapped in a transaction. You shouldn't do it
 * this way in your code. Instead you should enclose it in a TransactionPropagation trait
 * "enclose" method like in the DaoService class or in the OrderService example.
 */
class OrderStoredProcedureDaoTest extends FunSuite with DataSourceConfigurer with BeforeAndAfterEach {
  override val logger = Logger.getLogger(this.getClass().getName())

  override def beforeEach {
    super.configureJndi
  }

  override def afterEach {
    super.cleanupTransactions
  }

  test("Test a Stored procedure") {
    Transaction.getInstance.start(None, "fred")
    val storedProcedureDao = new OrderStoredProcedureDao()
    val result = storedProcedureDao.getAllOrdersUsingStoredProcedure
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
    logger.info(result.toString)
    assert(result.size > 0, "Should have returned at least 1 order object")
  }
}