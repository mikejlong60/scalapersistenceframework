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
package examples.crud

import org.scalapersistenceframework.IsolationLevel
import org.scalapersistenceframework.Required
import org.scalapersistenceframework.TRANSACTION_SERIALIZABLE
import org.scalapersistenceframework.TransactionPropagation
import org.scalapersistenceframework.service.DaoService

object AddressService {
  object DefaultAddressServiceTransactionPrefs {
    implicit val transPropagation = new TransactionPropagation with Required
    implicit val transIsolationLevel = TRANSACTION_SERIALIZABLE
  }
}
class AddressService(override val dao: AddressDao) extends DaoService(dao) {

  /**
   * This method demonstrates additional transactional beyond the default behavior of CrudDao.
   */
  def find(id: java.lang.Long)(implicit trans: TransactionPropagation, transIsolationLevel: IsolationLevel): Option[Address] = {
    trans enclose (connectionName, {
      logger.info(transIsolationLevel + " for AddressService find")
      super.findByPk(List(id))
    })
  }
}