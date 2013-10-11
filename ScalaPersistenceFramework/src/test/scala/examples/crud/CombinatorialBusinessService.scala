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

import org.scalapersistenceframework.IsolationLevel
import org.scalapersistenceframework.TransactionPropagation

class CombinatorialBusinessService(val orderService: OrderService, val addressService: AddressService) {

  val connectionName: Option[String] = None

  /**
   * This method demonstrates additional transactional beyond the default behavior of CrudDao.
   */
  def findCompleteOrder(orderId: java.lang.Long)(implicit trans: TransactionPropagation, transIsolationLevel: IsolationLevel): Order = {
    val logger = Logger.getLogger(this.getClass().getName())
    trans enclose (connectionName, {
      logger.info(transIsolationLevel + " for findCompleteOrder")
      val address: Option[Address] = addressService.find(orderId)
      val order = orderService.findByPk(List(orderId)).orNull.asInstanceOf[Order]
      order.primaryAddress = address
      order
    })
  }
}