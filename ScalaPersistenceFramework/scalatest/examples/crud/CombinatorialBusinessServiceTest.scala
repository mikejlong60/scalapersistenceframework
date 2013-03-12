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
import org.scalapersistenceframework.Required
import org.scalapersistenceframework.TRANSACTION_READ_COMMITTED
import org.scalapersistenceframework.TransactionPropagation
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import examples.crud.AddressService.DefaultAddressServiceTransactionPrefs.transIsolationLevel
import examples.crud.AddressService.DefaultAddressServiceTransactionPrefs.transPropagation
import org.scalapersistenceframework.DataSourceConfigurer

object LocalTransactionPrefs {
  implicit val transPropagation = new TransactionPropagation with Required
  implicit val transIsolationLevel = TRANSACTION_READ_COMMITTED
}

class CombinatorialBusinessServiceTest extends FunSuite with DataSourceConfigurer with BeforeAndAfterEach {
  override val logger = Logger.getLogger(this.getClass().getName())

  override def beforeEach {
    super.configureJndi
  }

  override def afterEach {
    super.cleanupTransactions
  }

  test("Test that transactional behavior allows you to override specfically when not in the context of an owning transaaction")({
    import examples.crud.AddressService.DefaultAddressServiceTransactionPrefs._
    var address = new Address(6, "Home Address", "123 Stribling Lane", "", "Virginia", "22656", null, null, false)
    logger.info(address.toString())
    val addressService = new AddressService(new AddressDao);
    addressService.insert(address);
    addressService.delete(address)
  })

  test("Test API of Combinatorial service using a JNDI connection pool for a specific DAO class")({
    var order = new Order(null, 2, "Big Order", None, false, null, Some(1), null, null, false)
    var address = new Address(6, "Home Address", "123 Stribling Lane", "", "Virginia", "22656", null, null, false)
    val orderService = new OrderService(new OrderDao);
    val addressService = new AddressService(new AddressDao);
    val pk = orderService.insert(order)
    address.id = pk
    addressService.insert(address);

    val combinedService = new CombinatorialBusinessService(orderService, addressService)
    order = combinedService.findCompleteOrder(pk)
    expectResult(pk) { order.id }
    expectResult(None) { order.complete }
    expectResult(2) { order.customerId }
    expectResult("Big Order") { order.description }
    expectResult(1) { order.orderQty.orNull }
    assert(order.createdTs != null)
    assert(order.updatedTs != null)
    expectResult(false) { order.approved }

    addressService.delete(address)
    orderService.delete(order)
  })

}