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
package org.scalapersistenceframework.service

import java.util.logging.Logger

import org.scalapersistenceframework.GridCapableEntity
import org.scalapersistenceframework.IsolationLevel
import org.scalapersistenceframework.PersistentOperationType
import org.scalapersistenceframework.TransactionPropagation
import org.scalapersistenceframework.dao.CrudDao

/**
 * This class is a transactional service class that encloses any CrudDao class
 * in transactional methods. You don't have to extend it unless you need to add additional
 * methods to this class unless you need behavior beyond its default CRUD and
 * findAll behavior.  
 * 
 * Note the two implicit parameters for each method. This is the method by which
 * you declare the transactional behavior of a given method. See the examples for 
 * more information on how to use this.
 *
 * @see org.scalapersistenceframework.Transaction for an explanation of
 *      the transactional behavior of all the methods in this class.
 *
 * @link http://www.scalapersistenceframework.org
 *
 * @author Michael J Long 02/22/2013
 *
 */
class DaoService[T <: GridCapableEntity](val dao: CrudDao[T], val connectionName: Option[String] = None) {

  val logger = Logger.getLogger(this.getClass().getName())

  /**
   * Returns a row from the database matching the given primary key, otherwise
   * None.
   *
   * @param id
   *            The ID of the role to be returned.
   * @return The role from the database matching the given ID, otherwise None.
   * @throws Exception
   *             If something fails at database level.
   */
  def findByPk(pk: List[Any])(implicit trans: TransactionPropagation, transIsolationLevel: IsolationLevel): Option[T] = {
    trans enclose (connectionName, {
      logger.info(transIsolationLevel + " for findByPk")

      dao.findByPk(pk)
    })
  }

  /**
   * Saves the given Collection, element by element in a single transaction.
   *
   * @see org.scalapersistenceframework.GridCapableEntity for how to
   *      mark rows for insert update or delete appropriately.
   *
   * @param vos
   *            The Set of items for saving
   * @throws Exception
   *             if something goes wrong at the database level.
   */
  def save(vos: Set[T])(implicit trans: TransactionPropagation, transIsolationLevel: IsolationLevel): Unit = {
    trans enclose (connectionName, {
      vos foreach { vo =>
        vo.persistentOperationType match {
          case PersistentOperationType.UPDATE => {
            vo.persistent match { case true => update(vo) case false => insert(vo) }
          }
          case PersistentOperationType.DELETE => delete(vo)
          case _ => logger.info("Transaction type not UPDATE, or DELETE, must be NONE.")
        }
      }
    })
  }

  /**
   * Returns a Collection of all items of a given type from the database.
   *
   * @param vos
   *            The Collection of items
   * @throws Exception
   *             if something goes wrong at the database level.
   */
  def findAll()(implicit trans: TransactionPropagation, transIsolationLevel: IsolationLevel): Set[T] = {
    trans enclose (connectionName, {
      dao.findAll
    })
  }

  /**
   * Inserts the given row in the database.
   *
   * @param vo
   *            The row to be inserted into the database.
   * @returns the generated key value if an auto-generated key is declared in
   *          the database for the table. Otherwise null.
   * @throws Exception
   *             If something fails at database level.
   */
  def insert(vo: T)(implicit trans: TransactionPropagation, transIsolationLevel: IsolationLevel): Long = {
    trans enclose (connectionName, {
      logger.info(transIsolationLevel + " for insert on dao service")
      dao.insert(vo)
    })
  }

  /**
   * Updates the given row in the database.
   *
   * @param vo
   *            The row to be updated in the database.
   * @throws Exception
   *             If something fails at database level.
   */
  def update(vo: T)(implicit trans: TransactionPropagation, transIsolationLevel: IsolationLevel): Unit = {
    trans enclose (connectionName, {
      logger.info(transIsolationLevel + " for update")
      dao.update(vo)
    })
  }

  /**
   * Deletes the row in the database with the matching primary key
   *
   * @param vo
   *            The row to be deleted from the database.
   * @throws IllegalArgumentException
   *             If the primary key is null.
   * @throws DAOException
   *             if a row does not exist in the database with the given
   *             primary key
   * @throws Exception
   *             If something else fails at database level.
   */
  def delete(vo: T)(implicit trans: TransactionPropagation, transIsolationLevel: IsolationLevel): Unit = {
    trans enclose (connectionName, {
      logger.info(transIsolationLevel + " for delete")
      dao.delete(vo)
    })
  }
}