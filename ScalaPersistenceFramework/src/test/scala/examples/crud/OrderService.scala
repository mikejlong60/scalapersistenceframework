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

import org.scalapersistenceframework.service.DaoService
import org.scalapersistenceframework.TransactionPropagation
import org.scalapersistenceframework.IsolationLevel

/**
 * This is an example of how to wrap an arbitrary DAO method(query, insert, update, ... transactionally.
 * You don't have to extend the DAOService. Just include the implicit parameters in the 
 * method signature and wrap the function in the trait's enclose function. Any function will
 * behave transactionally as per the trait's definition. 
 */

class OrderService(override val dao: OrderDao) extends DaoService(dao) {

  /**
   * This method demonstrates additional transactional beyond the default behavior of CrudDao.
   */
  def find(vo: Order)(implicit trans: TransactionPropagation, transIsolationLevel: IsolationLevel): Option[Order] = {
    trans enclose (connectionName, {
      logger.info(transIsolationLevel + " for OrderService find")
      super.findByPk(List(vo.id))
    })
  }
}