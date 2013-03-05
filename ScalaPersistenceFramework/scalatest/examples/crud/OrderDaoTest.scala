/**
 * *****************************************************************************
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
 * ****************************************************************************
 */
package examples.crud

import java.util.logging.Logger
import org.scalapersistenceframework.DefaultTransactionPrefs.transIsolationLevel
import org.scalapersistenceframework.PersistentOperationType
import org.scalapersistenceframework.Transaction
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import org.scalapersistenceframework.DataSourceConfigurer

/**
 * All these test functions show how to use a DAO by itself wrapped in a transaction. You shouldn't do it
 * this way in your code. Instead you should enclose it in a TransactionPropagation trait
 * "enclose" method like in the DaoService class or in the OrderService example.
 */

class OrderDaoTest extends FunSuite with DataSourceConfigurer with BeforeAndAfterEach {
  override val logger = Logger.getLogger(this.getClass().getName())

  override def beforeEach {
    super.configureJndi
  }

  override def afterEach {
    super.cleanupTransactions
  }

  test("Test nullable column setting using an Option") {
    var result = new Order(null, 2, "Big Order", None, false, null, None, null, null, false)
    logger.info(result.toString())
    val dao = new OrderDao();
    //Insert a new row
    Transaction.getInstance.start(None, "fred")
    val pk = dao.insert(result)
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")

    Transaction.getInstance.start(None, "fred")
    result = dao.findByPk(List(pk)).orNull
    expectResult(pk) { result.id }
    expectResult(None) { result.complete }
    expectResult(2) { result.customerId }
    expectResult(null) { result.orderQty.orNull }
    expectResult("Big Order") { result.description }
    assert(result.createdTs != null)
    assert(result.updatedTs != null)
    expectResult(false) { result.approved }

    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")

    //Now make sure update works
    Transaction.getInstance.start(None, "fred")
    result.orderQty = Some(12)
    dao.update(result)
    result = dao.findByPk(List(pk)).orNull
    expectResult(pk) { result.id }
    expectResult(None) { result.complete }
    expectResult(2) { result.customerId }
    expectResult(12) { result.orderQty.orNull }
    expectResult("Big Order") { result.description }
    assert(result.createdTs != null)
    assert(result.updatedTs != null)
    expectResult(false) { result.approved }
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")

    //Update orderQty back to null
    Transaction.getInstance.start(None, "fred")
    result.orderQty = None
    dao.update(result)
    result = dao.findByPk(List(pk)).orNull
    expectResult(pk) { result.id }
    expectResult(None) { result.complete }
    expectResult(2) { result.customerId }
    expectResult(null) { result.orderQty.orNull }
    expectResult("Big Order") { result.description }
    assert(result.createdTs != null)
    assert(result.updatedTs != null)
    expectResult(false) { result.approved }
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")

  }

  test("Test JNDI configuration using Spring Mock JNDI container") {
    var result = new Order(null, 2, "Big Order", None, false, null, Some(6), null, null, false)
    logger.info(result.toString())
    val dao = new OrderDao();
    Transaction.getInstance.start(None, "fred")
    val pk = dao.insert(result)
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")

    Transaction.getInstance.start(None, "fred")
    result = dao.findByPk(List(pk)).orNull
    expectResult(pk) { result.id }
    expectResult(None) { result.complete }
    expectResult(2) { result.customerId }
    expectResult(6) { result.orderQty.orNull }
    expectResult("Big Order") { result.description }
    assert(result.createdTs != null)
    assert(result.updatedTs != null)
    expectResult(false) { result.approved }

    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")

    result.description = "updated description"
    Transaction.getInstance.start(None, "fred")
    result.complete = Some(true)
    result.approved = true
    result.id = pk
    dao.update(result)
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
    Transaction.getInstance.start(None, "fred")
    result = dao.findByPk(List(pk)).orNull
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
    expectResult(pk) { result.id }
    expectResult(Some(true)) { result.complete }
    expectResult(2) { result.customerId }
    expectResult("updated description") { result.description }
    assert(result.createdTs != null)
    assert(result.updatedTs != null)
    expectResult(true) { result.approved }
    Transaction.getInstance.start(None, "fred")
    val result3: Set[Order] = dao.findAll()
    Transaction.getInstance.commit("fred")
    assert(result3.size > 10, "Expected to get at least 10 rows.")
    Transaction.getInstance.end(None, "fred")
    Transaction.getInstance.start(None, "fred")
    dao.delete(result)
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
    Transaction.getInstance.start(None, "fred")
    val result2 = dao.findByPk(List(pk))
    Transaction.getInstance.commit("fred")
    Transaction.getInstance.end(None, "fred")
    expectResult(None) { result2 }
    //TODO - put pattern match in here to make sure its the right kind.
    //TODO - Add tests for bad name
    //TODO - Add test for trying to get before you configured it
  }

  test("Make sure that you can configure more than one connection to the database") {
    var result = new Order(null, 2, "Big Order", None, false, null, Some(5), null, null, false)
    logger.info(result.toString())
    val dao = new OrderDao();
    Transaction.getInstance(Some("postgres.jndi")).start(Some("postgres.jndi"), "fred")
    var pk = dao.insert(result)
    Transaction.getInstance(Some("postgres.jndi")).commit("fred")
    Transaction.getInstance(Some("postgres.jndi")).end(Some("postgres.jndi"), "fred")

    Transaction.getInstance(Some("postgres.jndi")).start(Some("postgres.jndi"), "fred")
    result = dao.findByPk(List(pk)).orNull
    Transaction.getInstance(Some("postgres.jndi")).commit("fred")
    Transaction.getInstance(Some("postgres.jndi")).end(Some("postgres.jndi"), "fred")
    expectResult(pk) { result.id }
    expectResult(None) { result.complete }
    expectResult(2) { result.customerId }
    expectResult("Big Order") { result.description }
    expectResult(PersistentOperationType.NONE) { result.persistentOperationType }

    assert(result.createdTs != null)
    assert(result.updatedTs != null)
    expectResult(false) { result.approved }

    //Try another connection
    Transaction.configure("postgres.jdbc")
    result = new Order(null, 2, "Big Order", None, false, null, Some(3), null, null, false)
    logger.info(result.toString())
    Transaction.getInstance(Some("postgres.jdbc")).start(Some("postgres.jdbc"), "fred")
    val dao2 = new OrderDao(Some("postgres.jdbc"))
    pk = dao2.insert(result)
    Transaction.getInstance(Some("postgres.jdbc")).commit("fred")
    Transaction.getInstance(Some("postgres.jdbc")).end(Some("postgres.jdbc"), "fred")

    Transaction.getInstance(Some("postgres.jdbc")).start(Some("postgres.jdbc"), "fred")
    result = dao2.findByPk(List(pk)).orNull
    Transaction.getInstance(Some("postgres.jdbc")).commit("fred")
    Transaction.getInstance(Some("postgres.jdbc")).end(Some("postgres.jdbc"), "fred")
    expectResult(pk) { result.id }
    expectResult(3) { result.orderQty.orNull }
    expectResult(None) { result.complete }
    expectResult(2) { result.customerId }
    expectResult("Big Order") { result.description }
    assert(result.createdTs != null)
    assert(result.updatedTs != null)
    expectResult(false) { result.approved }
  }
}