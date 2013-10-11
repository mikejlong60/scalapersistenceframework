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
package examples.crud

import java.util.logging.Logger
import org.scalapersistenceframework.PersistentOperationType
import org.scalapersistenceframework.Required
import org.scalapersistenceframework.TRANSACTION_READ_COMMITTED
import org.scalapersistenceframework.TRANSACTION_REPEATABLE_READ
import org.scalapersistenceframework.TransactionPropagation
import org.scalapersistenceframework.service.DaoService
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import org.scalapersistenceframework.DataSourceConfigurer
import org.springframework.mock.jndi.SimpleNamingContextBuilder
import org.scalapersistenceframework.Transaction

class DaoServiceTest extends FunSuite with DataSourceConfigurer with BeforeAndAfterEach {
  override val logger = Logger.getLogger(this.getClass().getName())

  override def beforeEach {
    super.configureJndi
  }

  override def afterEach {
    super.cleanupTransactions
  }


  /**
   * The following "implicit" mechanism allows you to express transactional behavior at three levels:
   *
   * 1) Adopting the default transactional object from the DaoService by placing the
   * following import in your code after the class keyword:
   * "import org.scalapersistenceframework.service.DaoService.DefaultTransactionPrefs._"
   *
   * 2) You can specify a class overload for transactional behavior by creating an implicit value specifying the transactional
   * behavior for this class like this:
   *   implicit val trans2 = new TransactionPropagation with Required
   *
   * 3) You can pass the transactional behavior at the method level explicitly as a parameter to each method call like this:
   *     service.insert(result){ trans2 } or service.insert(result)(trans2)
   *
   */
  //  import org.scalapersistenceframework.DefaultTransactionPrefs._
  //or
  object LocalTransactionPrefs {
    implicit val transPropagation = new TransactionPropagation with Required
    implicit val transIsolationLevel = TRANSACTION_READ_COMMITTED
  }
  import LocalTransactionPrefs._
  //or passed at the method level
  //  implicit val transIsolationLevel = TRANSACTION_READ_COMMITTED

  test("Test API of DAO service using a JNDI connection pool for a specific DAO class")({
    var result = new Order(null, 2, Some("Big Order"), None, false, None, Some(1), null, null, false)
    logger.info(result.toString())
    val service = new DaoService(new OrderDao);
    val pk = service.insert(result)

    result = service.findByPk(List(pk)).orNull
    expectResult(pk) { result.id }
    expectResult(None) { result.complete }
    expectResult(2) { result.customerId }
    expectResult(Some("Big Order")) { result.description }
    expectResult(1) { result.orderQty.orNull }
    expectResult(true) { result.persistent }
    assert(result.createdTs != null)
    assert(result.updatedTs != null)
    expectResult(false) { result.approved }

    result.description = Some("updated description")
    result.complete = Some(true)
    result.approved = true
    service.update(result)(new TransactionPropagation with Required, TRANSACTION_REPEATABLE_READ)
    result = service.findByPk(List(pk)).orNull
    expectResult(pk) { result.id }
    expectResult(Some(true)) { result.complete }
    expectResult(2) { result.customerId }
    expectResult(Some("updated description")) { result.description }
    assert(result.createdTs != null)
    assert(result.updatedTs != null)
    expectResult(true) { result.approved }
    val result3 = service.findAll()
    assert(result3.size > 1, "Should have returned at least one row.")
    service.delete(result)
    val result2 = service.findByPk(List(pk))
    expectResult(None) { result2 }
    //TODO - put pattern match in here to make sure its the right kind.
    //TODO - Add tests for bad name
    //TODO - Add test for trying to get before you configured it
  })

  test("Test Save method of DAO service using a JNDI connection pool for a specific DAO class")({
    var result = new Order(null, 2, Some("Big Order"), None, false, null, Some(1), null, null, false)
    logger.info(result.toString())
    val service = new DaoService(new OrderDao);
    val pk = service.insert(result)
    logger.info(pk.toString)
    val result2 = service.findAll()
    assert(result2.size > 1, "Should have returned at least one row.")

    //An example of a foreach chained after a filter.
    val result3 = result2 filter { vo => vo.id == pk } foreach { vo => vo.persistentOperationType = PersistentOperationType.DELETE }
    //    logger.info(result3.toString)

    //       val result5 = result2 filter { vo => vo.id == pk} yield { vo.persistentOperationType = PersistentOperationType.DELETE }

    //The same thing done with a yield and a for loop with a filter.
    for (vo <- result2; if vo.id == pk) yield { vo.persistentOperationType = PersistentOperationType.DELETE }
    service save result2
    val result4 = service.findByPk(List(pk))
    expectResult(None) { result4 }
  })

}