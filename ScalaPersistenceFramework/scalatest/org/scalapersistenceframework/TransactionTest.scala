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
package org.scalapersistenceframework

import org.scalatest.FunSuite
import LocalTransactionPrefs._
import java.util.logging.Logger
import org.scalatest.BeforeAndAfterEach
import examples.crud.DataSourceConfigurer

object LocalTransactionPrefs {
  implicit val trans = new TransactionPropagation with Required
  implicit val transIsolationLevel = TRANSACTION_READ_COMMITTED
}

class TransactionTest extends FunSuite with BeforeAndAfterEach with DataSourceConfigurer {
  val logger = Logger.getLogger(this.getClass().getName())

  override def beforeEach {
    super.configureJndi
  }

  test("Make sure all instances of Transaction point to same underlying object") {
    val t = Transaction.getInstance
    val t2 = Transaction.getInstance
    logger.info(t.toString())
    logger.info(t2.toString())
    assert(t == t2)
    val t4 = t2
    assert(t4 == t)
  }

  test("Test configure on the singleton transaction object.") {
    Transaction.getInstance.start(None, "fred")
    val conn = Transaction.getInstance.getConnectionForTransaction
    assert(conn != null, "The connection was null.")
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
  }

  test("Test orNull on an option field") {
    val x = Some("mike")
    logger.info(x.orNull)
    val y = None
    val p = "sd"
    logger.info(y.orNull)
    logger.info(y.getOrElse({ "12" + "13"; val x = 2 * 3 }).toString)
  }

  test("Test default transaction2 propagation trait.") {
    executeInTransaction
  }

  test("Test default transaction propagation trait.") {
    Transaction.configure("postgres.jndi")
    executeInTransaction(new TransactionPropagation with Required)
  }

  def executeInTransaction(implicit trans: TransactionPropagation) = {
    trans.enclose(None, {
      val conn = Transaction.getInstance.getConnectionForTransaction
      assert(conn != null, "The connection was null.")
      executeAgainInTransaction //(new TransactionPropagation with Required)
    })

  }

  def executeAgainInTransaction(implicit trans: TransactionPropagation) {
    trans.enclose(None, {
      val conn = Transaction.getInstance.getConnectionForTransaction
      assert(conn != null, "The connection was null.")
    })

  }

  test("Test that by configuring the same transaction object repeatedly with the same name does not produce another transaction object") {
    Transaction.configure("postgres.jndi")
    val trans = Transaction.getInstance(Some("postgres.jndi"))

    Transaction.configure("postgres.jndi")
    val trans2 = Transaction.getInstance(Some("postgres.jndi"))
    assert(trans === trans2, "Should not have produced more than one transaction object with the same name")
  }

  test("Test JNDI configuration for all three kinds of data sources") {
    Transaction.configure("postgres.jndi")
    Transaction.configure("postgres.jdbc")
    Transaction.configure("postgres.jndilogin")
    val trans = Transaction.getInstance(Some("postgres.jndi"))
    assert(trans match { case _: DataSourceTransaction => true case _ => false }, "Wrong kind of transaction. Should have been a DataSourceTransaction")

    val trans2 = Transaction.getInstance(Some("postgres.jdbc"))
    assert(trans2 match { case _: DriverManagerTransaction => true case _ => false }, "Wrong kind of transaction. Should have been a DriverManagerTransaction")

    val trans3 = Transaction.getInstance(Some("postgres.jndilogin"))
    assert(trans3 match { case _: DataSourceWithLoginTransaction => true case _ => false }, "Wrong kind of transaction. Should have been a DataSourceWithLoginTransaction")

    assert(trans.getConnection != null, "Failed to get a database connection.")
    assert(trans2.getConnection != null, "Failed to get a database connection.")
    //The following is not supported in Postgres.
    //   assert(trans3.getConnection != null, "Failed to get a database connection.")
  }

  test("Test getting instance for non-configured data source") {
    Transaction.configure("bogus.jdbc")
    val trans2 = Transaction.getInstance(Some("bogus.jndi"))
    assert(trans2 match { case _: DriverManagerTransaction => true case _ => false }, "Wrong kind of transaction. Should have been a DriverManagerTransaction")

  }

}