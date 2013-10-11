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
package examples.query

import java.sql.ResultSet
import java.util.logging.Logger

import scala.Option.option2Iterable
import scala.util.Random

import org.scalapersistenceframework.DataSourceConfigurer
import org.scalapersistenceframework.DefaultTransactionPrefs.transIsolationLevel
import org.scalapersistenceframework.DefaultTransactionPrefs.transPropagation
import org.scalapersistenceframework.Transaction
import org.scalapersistenceframework.service.DaoService
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite

import examples.crud.Address
import examples.crud.AddressDao

/**
 * All these test functions show how to use a DAO by itself wrapped in a transaction. You shouldn't do it
 * this way in your code. Instead you should enclose it in a TransactionPropagation trait
 * "enclose" method like in the DaoService class or in the OrderService example.
 */
class AddressQueryTest extends FunSuite with DataSourceConfigurer with BeforeAndAfterEach {
  override val logger = Logger.getLogger(this.getClass().getName())
  val service = new DaoService(new AddressDao())
  val random = new Random(System.currentTimeMillis())
  val pk = random.nextInt//This is just to show that you can use a key of your own creation.

  override def beforeEach {
    super.configureJndi
    service.insert(new Address(pk, "Home Office", "111 Will Street", "", "CA", "12345", null, null, false))
  }

  override def afterEach {
    service.delete(service.findByPk(List(pk)).get)
    super.cleanupTransactions
  }

  def mapForAddressQuery(rs: ResultSet): Option[Address] =
    Some(new Address(rs.getLong("id"), rs.getString("name"), rs.getString("line1"), rs.getString("line2"), rs.getString("state"), rs.getString("zipcode"), rs.getTimestamp("created_ts"), rs.getTimestamp("updated_ts"), rs.getInt("persistent") match { case 1 => true case _ => false }))

  def getAddressQuery(): String = { "select id, name, line1, line2, state, zipcode, created_ts, updated_ts, 1 as persistent from spf.address where name like (?)" }

  test("Test a Query that returns multiple rows using Spring Mock JNDI container") {
    Transaction.getInstance.start(None, "fred")
    val queryDao = new AddressDaoWithCustomQueries()
    val result = queryDao.executeQueryThatReturnsMultiRows[Address](getAddressQuery, mapForAddressQuery, List("%Hom%"))
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
    expectResult(1) { result.size }

    Transaction.getInstance.start(None, "fred")
    val result2 = queryDao.executeQueryThatReturnsMultiRows[Address](getAddressQuery, mapForAddressQuery, List("%Hoasdm%"))
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
    expectResult(0) { result2.size }
  }

  test("Test a Query that returns a single row using Spring Mock JNDI container") {
    Transaction.getInstance.start(None, "fred")
    val queryDao = new AddressDaoWithCustomQueries
    val result = queryDao.executeQueryThatReturnsSingleRow[Address](getAddressQuery, mapForAddressQuery, List("%Hom%"))
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
    expectResult(1) { result.size }

    Transaction.getInstance.start(None, "fred")
    expectResult(None) { queryDao.executeQueryThatReturnsSingleRow[Address](getAddressQuery, mapForAddressQuery, List("%xs%")) }
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
  }

  test("Test another query for a different kind of return type attached to the AddressDaoWthCustomQueries object") {
    Transaction.getInstance.start(None, "fred")
    val queryDao = new AddressDaoWithCustomQueries
    val result = queryDao.getAllBigOrders("%ig%")
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
    assert(result.size > 1, "Should have found at least one Order.")

    Transaction.getInstance.start(None, "fred")
    expectResult(None) { queryDao.executeQueryThatReturnsSingleRow[Address](getAddressQuery, mapForAddressQuery, List("%xs%")) }
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
  }

}