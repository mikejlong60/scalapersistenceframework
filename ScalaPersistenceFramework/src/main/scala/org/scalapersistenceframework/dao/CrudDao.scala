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
package org.scalapersistenceframework.dao

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.logging.Logger

import org.scalapersistenceframework.GridCapableEntity
import org.scalapersistenceframework.Transaction
import org.scalapersistenceframework.dao.DefaultBooleanHandler._

/**
 * This class is a data access object that provides basic CRUD operations
 * for a single database table.
 *
 * @link http://www.scalapersistenceframework.org
 *
 * @author Michael J Long 02/22/2013
 */

trait CrudDao[T <: GridCapableEntity] extends BasicDao {
  override val logger = Logger.getLogger(this.getClass().getName())

  /**
   * Returns a row from the database matching the given primary key, otherwise
   * None.
   *
   * @param id
   *            The ID of the role to be returned.
   * @return The role from the database matching the given ID, otherwise None.
   * @throws SQLException
   *             If something fails at database level.
   */
  def findByPk(pk: List[Any]): Option[T] = {
    return find(getSQL_FIND_BY_ID(), pk)
  }

  /**
   * Returns a single row from the database matching the given SQL query with
   * the given values. The assumption is that no more than one row can exist
   * with the given values.
   *
   * @param sql
   *            The SQL query to be executed in the database.
   * @param values
   *            The PreparedStatement values to be set.
   * @return The role from the database matching the given SQL query with the
   *         given values.
   * @throws SQLException
   *             If something fails at database level.
   */
  protected def find(sql: String, values: List[Any]): Option[T] = {
    var connection: Connection = null
    var preparedStatement: PreparedStatement = null
    var resultSet: ResultSet = null
    var vo: Option[T] = None

    try {
      connection = Transaction.getInstance(connectionName).getConnectionForTransaction()
      preparedStatement = prepareStatement(connection, sql, false, values)
      resultSet = preparedStatement.executeQuery()
      if (resultSet.next()) {
        vo = Some(mapForSelect(resultSet))
      }
    } finally {
      close(preparedStatement, resultSet)
    }

    return vo
  }

  /**
   * Returns a Set of rows from the database matching the given SQL
   * query with the given values.
   *
   * @param sql
   *            The SQL query to be executed in the database.
   * @param values
   *            The PreparedStatement values to be set.
   * @return The role from the database matching the given SQL query with the
   *         given values.
   * @throws SQLException
   *             If something fails at database level.
   */
  protected def findList(sql: String, values: List[Any]): Set[T] = {

    var connection: Connection = null
    var preparedStatement: PreparedStatement = null
    var resultSet: ResultSet = null

    try {
      connection = Transaction.getInstance(connectionName).getConnectionForTransaction
      preparedStatement = prepareStatement(connection, sql, false, values)
      resultSet = preparedStatement.executeQuery

      new Iterator[T] {
        def hasNext = {
          resultSet.next()
        }
        def next() = {
          mapForSelect(resultSet)
        }
      }.toSet[T]
    } finally {
      close(preparedStatement, resultSet)
    }
  }

  /**
   * Returns a Set of all the rows from the database. The Set is
   * never null and is empty when the database does not contain any rows.
   *
   * @return A Set of all rows from the database.
   * @throws SQLException
   *             If something fails at database level.
   */
  def findAll(): Set[T] = {
    return findList(getSQL_LIST_ORDER_BY_ID(), List[Any]())
  }

  /**
   * Create the given row in the database.
   *
   * @param vo
   *            The row to be created in the database.
   * @return the generated key value if an auto-generated key is declared in
   *          the database for the table. Otherwise -1
   * @throws IllegalArgumentException
   *             If the primary key does not pass the validation function defined by the
   * subclass. Such a function should verify that the PK field is null if the key is
   * auto-generated by a database sequence. Otherwise that function should verify that
   * the PK field(s) are not null.
   * @throws SQLException
   *             If something fails at database level.
   */
  def insert(vo: T): Long = {
    validatePkForInsert(vo)

    val values: List[Any] = mapForInsert(vo)

    var connection: Connection = null
    var preparedStatement: PreparedStatement = null
    var generatedKeys: ResultSet = null

    try {
      connection = Transaction.getInstance(connectionName).getConnectionForTransaction()
      preparedStatement = prepareStatement(connection, getSQL_INSERT(), true, values)
      val affectedRows = preparedStatement.executeUpdate()
      if (affectedRows == 0) {
        throw new IllegalStateException(
          "Insert operation failed, no rows affected.")
      }

      val rs: ResultSet = preparedStatement.getGeneratedKeys()
      var insertedKeyValue: Long = -1
      if (rs.next()) {
        insertedKeyValue = rs.getLong(1)
        logger.info("key value for insert:" + insertedKeyValue)
      }
      insertedKeyValue
    } finally {
      close(preparedStatement, generatedKeys)
    }
  }

  /**
   * Update the given row in the database. The primary key must not be null,
   * otherwise it will throw IllegalArgumentException.
   *
   * @param vo
   *            The row to be updated in the database.
   * @throws IllegalArgumentException
   *             If the primary key is null.
   * @throws SQLException
   *             If something fails at database level.
   */
  def update(vo: T): Unit = {

    validatePkForUpdate(vo)

    val values: List[Any] = mapForUpdate(vo)

    var connection: Connection = null
    var preparedStatement: PreparedStatement = null

    try {
      connection = Transaction.getInstance(connectionName).getConnectionForTransaction()
      preparedStatement = prepareStatement(connection, getSQL_UPDATE(), false, values)
      val affectedRows = preparedStatement.executeUpdate()
      if (affectedRows == 0) {
        throw new IllegalStateException("Updating failed, no rows affected.")
      }
      logger.info("update successful")
    } finally {
      close(preparedStatement)
    }
  }

  /**
   * Delete the given row from the database.
   *
   * @param vo
   *            The row to be deleted from the database.
   * @throws SQLException
   *             If something fails at database level.
   */
  def delete(vo: T): Unit = {
    val values: List[Any] = mapForDelete(vo)

    var connection: Connection = null
    var preparedStatement: PreparedStatement = null

    try {
      connection = Transaction.getInstance(connectionName).getConnectionForTransaction()
      preparedStatement = prepareStatement(connection, getSQL_DELETE(),
        false, values)
      val affectedRows = preparedStatement.executeUpdate()
      if (affectedRows == 0) {
        throw new IllegalStateException(
          "Delete operation failed, no rows affected.")
      }
    } finally {
      close(preparedStatement)
    }
  }

  /**
   * This is the validation function for the update statement. It should verify that the
   * primary keys are not null at a minimum.
   */
  protected def validatePkForUpdate(vo: T): Unit

  /**
   * This is the validation function for the insert statement. For auto-generated
   * keys the function should validate that the generated part of the primary key is null.
   * Otherwise it should validate the the PK is not null.
   */
  protected def validatePkForInsert(vo: T): Unit

  /**
   * Map the current row of the given ResultSet to a value object.
   *
   * @param resultSet
   *            The ResultSet of which the current row is to be mapped to a
   *            value object.
   * @return The mapped value object from the current row of the given
   *         ResultSet.
   * @throws SQLException
   *             If something fails at database level.
   */
  protected def mapForSelect(resultSet: ResultSet): T

  /**
   * Set the list of objects that are parameters to the update statement.
   */
  protected def mapForUpdate(vo: T)(implicit booleanHandler: RDBMSBooleanHandler): List[Any]

  /**
   * Set the list of objects that are parameters to the insert statement.
   */
  protected def mapForInsert(vo: T)(implicit booleanHandler: RDBMSBooleanHandler): List[Any]

  /**
   * Set the list of objects that are parameters to the delete statement.
   */
  protected def mapForDelete(vo: T): List[Any]

  /**
   * This function should return the SQL statement for finding by primary key.
   */
  protected def getSQL_FIND_BY_ID(): String

  /**
   * This function should return the SQL statement for finding all the rows.
   */
  protected def getSQL_LIST_ORDER_BY_ID(): String

  /**
   * This function should return the SQL statement for inserting a single row into the database.
   */
  protected def getSQL_INSERT(): String

  /**
   * This function should return the SQL statement for updating a single row into the database.
   * This is where you can employ optimistic locking if you wish to do so. For example:
   * update spf.address set id=?,name=?, line1=?, line2=?, state=?, zipcode=?, updated_ts = current_timestamp where id=? and updated_ts = ?
   */
  protected def getSQL_UPDATE(): String

  /**
   * This function should return the SQL statement for deleting a single row into the database.
   */
  protected def getSQL_DELETE(): String
}