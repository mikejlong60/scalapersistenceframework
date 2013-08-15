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
import java.sql.Statement
import java.util.logging.Logger

/**
 * This trait provides lots of features necessary for data access objects.
 *
 * @link http://www.scalapersistenceframework.org
 *
 * @author Mike Long 02/22/2013
 *
 */

trait BasicDao {
  val logger = Logger.getLogger(this.getClass().getName())

  val connectionName: Option[String] = None

  /**
   * This method returns a String object from a sql String column. 
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the sql String column in the result set
   * @return the column as a Scala object.
   * @throws SQLException
   */
  protected def nonNullableString(resultSet: ResultSet, columnName: String): String = {
    resultSet.getString(columnName)
  }

  /**
   * This method returns None if the column in the database is null. Otherwise
   * it returns an Option of the given type.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the column in the result set
   * @return either None or Option[the Given Type]
   * @throws SQLException
   */
  protected def nullableString(resultSet: ResultSet, columnName: String):Option[String] = {
    getAsOption[String]({ resultSet.getString(columnName) }, resultSet)
  }

  /**
   * This method returns a java.sql.Timestamp object from a sql Timestamp column. 
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the sql Timestamp column in the result set
   * @return the column as a Scala object.
   * @throws SQLException
   */
  protected def nonNullableTimestamp(resultSet: ResultSet, columnName: String): java.sql.Timestamp = {
    resultSet.getTimestamp(columnName)
  }

  /**
   * This method returns None if the column in the database is null. Otherwise
   * it returns an Option of the given type.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the column in the result set
   * @return either None or Option[the Given Type]
   * @throws SQLException
   */
  protected def nullableTimestamp(resultSet: ResultSet, columnName: String):Option[java.sql.Timestamp] = {
    getAsOption[java.sql.Timestamp]({ resultSet.getTimestamp(columnName) }, resultSet)
  }

  /**
   * This method returns a java.sql.Date object from a sql Date column. 
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the sql Date column in the result set
   * @return the column as a Scala object.
   * @throws SQLException
   */
  protected def nonNullableDate(resultSet: ResultSet, columnName: String): java.sql.Date = {
    resultSet.getDate(columnName)
  }

  /**
   * This method returns None if the column in the database is null. Otherwise
   * it returns an Option of the given type.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the column in the result set
   * @return either None or Option[the Given Type]
   * @throws SQLException
   */
  protected def nullableDate(resultSet: ResultSet, columnName: String):Option[java.sql.Date] = {
    getAsOption[java.sql.Date]({ resultSet.getDate(columnName) }, resultSet)
  }

  /**
   * This method returns a Short object from a sql numeric column. 
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the sql Short column in the result set
   * @return the column as a Scala object.
   * @throws SQLException
   */
  protected def nonNullableShort(resultSet: ResultSet, columnName: String): Short = {
    resultSet.getShort(columnName)
  }

  /**
   * This method returns None if the column in the database is null. Otherwise
   * it returns an Option of the given type.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the column in the result set
   * @return either None or Option[the Given Type]
   * @throws SQLException
   */
  protected def nullableShort(resultSet: ResultSet, columnName: String):Option[Short] = {
    getAsOption[Short]({ resultSet.getShort(columnName) }, resultSet)
  }

  /**
   * This method returns a Double object from a sql Double column. 
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the sql Double column in the result set
   * @return the column as a Scala object.
   * @throws SQLException
   */
  protected def nonNullableDouble(resultSet: ResultSet, columnName: String): Double = {
    resultSet.getDouble(columnName)
  }

  /**
   * This method returns None if the column in the database is null. Otherwise
   * it returns an Option of the given type.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the column in the result set
   * @return either None or Option[the Given Type]
   * @throws SQLException
   */
  protected def nullableDouble(resultSet: ResultSet, columnName: String):Option[Double] = {
    getAsOption[Double]({ resultSet.getDouble(columnName) }, resultSet)
  }

  /**
   * This method returns a Float object from a sql Float column. 
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the sql Float column in the result set
   * @return the column as a Scala object.
   * @throws SQLException
   */
  protected def nonNullableFloat(resultSet: ResultSet, columnName: String): Float = {
    resultSet.getFloat(columnName)
  }

  /**
   * This method returns None if the column in the database is null. Otherwise
   * it returns an Option of the given type.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the column in the result set
   * @return either None or Option[the Given Type]
   * @throws SQLException
   */
  protected def nullableFloat(resultSet: ResultSet, columnName: String):Option[Float] = {
    getAsOption[Float]({ resultSet.getFloat(columnName) }, resultSet)
  }

  /**
   * This method returns a Long object from a sql Long column. 
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the sql Long column in the result set
   * @return the column as a Scala object.
   * @throws SQLException
   */
  protected def nonNullableLong(resultSet: ResultSet, columnName: String): Long = {
    resultSet.getLong(columnName)
  }

  /**
   * This method returns None if the column in the database is null. Otherwise
   * it returns an Option of the given type.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the column in the result set
   * @return either None or Option[the Given Type]
   * @throws SQLException
   */
  protected def nullableLong(resultSet: ResultSet, columnName: String): Option[Long] = {
    getAsOption[Long]({ resultSet.getLong(columnName) }, resultSet)
  }

  private def getAsOption[T](f: => T, resultSet: ResultSet): Option[T] = {
    val value = f
    if (resultSet.wasNull())
      None
    else
      Some(value)
  }

  /**
   * This method returns a Integer object from a sql Int column. 
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the sql Int column in the result set
   * @return the column as a Scala object.
   * @throws SQLException
   */
  protected def nonNullableInteger(resultSet: ResultSet, columnName: String): Integer = {
    resultSet.getInt(columnName)
  }

  /**
   * This method returns None if the column in the database is null. Otherwise
   * it returns an Option of the given type.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the column in the result set
   * @return either None or Option[the Given Type]
   * @throws SQLException
   */
  protected def nullableInteger(resultSet: ResultSet, columnName: String): Option[Integer] = {
    getAsOption[Integer]({ resultSet.getInt(columnName) }, resultSet)
  }

  /**
   * This method returns false if the column in the database is null. Otherwise it returns true or false
   * based upon the evaluation of the implicit RDBMSBooleanHandler.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the column in the result set
   * @return either true or false
   * @throws SQLException
   */
  protected def nonNullableBoolean(resultSet: ResultSet, columnName: String)(implicit booleanHandler: RDBMSBooleanHandler): Boolean = {
    booleanHandler.nonNullableBoolean(resultSet, columnName)
  }

  /**
   * This method returns None if the column in the database if null. Otherwise
   * it returns an Option[Boolean] based upon the evaluation of the implicit 
   * RDBMSBooleanHandler.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the column in the result set
   * @return either None or Option[Boolean]
   * @throws SQLException
   */
  protected def nullableBoolean(resultSet: ResultSet, columnName: String)(implicit booleanHandler: RDBMSBooleanHandler): Option[Boolean] = {
    booleanHandler.nullableBoolean(resultSet, columnName)
  }

  /**
   * Returns a PreparedStatement of the given connection, set with the given
   * SQL query and the given parameter values.
   *
   * @param connection
   *            The Connection to create the PreparedStatement from.
   * @param sql
   *            The SQL query to construct the PreparedStatement with.
   * @param returnGeneratedKeys
   *            Set whether to return generated keys or not.
   * @param values
   *            The parameter values to be set in the created
   *            PreparedStatement.
   * @throws SQLException
   *             If something fails during creating the PreparedStatement.
   */
  protected def prepareStatement(connection: Connection, sql: String, returnGeneratedKeys: Boolean, values: List[Any]) = {
    val preparedStatement = connection.prepareStatement(sql, if (returnGeneratedKeys) Statement.RETURN_GENERATED_KEYS else Statement.NO_GENERATED_KEYS)
    logger.info("SQL Statement:" + sql)
    setValues(preparedStatement, values)
    preparedStatement
  }

  /**
   * Returns a CallableStatement for the given connection, set with the given
   * SQL and the given parameter values.
   *
   * @param connection
   *            The Connection to create the CallableStatement from.
   * @param sql
   *            The callableStatement String
   * @param values
   *            The parameter values to be set in the created CallableStatement.
   * @throws SQLException
   *             If something fails during creating the CallableStatement.
   */
  protected def prepareCallableStatement(connection: Connection, sql: String, values: List[Any]) = {
    val callableStatement = connection.prepareCall(sql)
    logger.info("Callable Statement:" + sql)
    setValues(callableStatement, values)
    callableStatement
  }

  /**
   * Set the given parameter values in the given PreparedStatement.
   *
   * @param connection
   *            The PreparedStatement to set the given parameter values in.
   * @param values
   *            The parameter values to be set in the created
   *            PreparedStatement.
   * @throws SQLException
   *             If something fails during setting the PreparedStatement
   *             values.
   */
  private def setValues(preparedStatement: PreparedStatement, values: List[Any]) {
    (1 /: values) {
      (i, value) =>
        {
          preparedStatement.setObject(i, value)
          logger.info("Setting Parameter[" + i + "] of preparedStatment to[" + value + "].")
          i + 1
        }
    }
  }

  /**
   * Close the Statement.
   *
   * @param statement
   *            The Statement to be closed quietly.
   * @throws SQLException
   */
  protected def close(statement: Statement) {
    if (statement != null) {
      statement.close()
    }
  }

  /**
   * Close the ResultSet.
   *
   * @param resultSet
   *            The ResultSet to be closed.
   * @throws SQLException
   */
  protected def close(resultSet: ResultSet) {
    if (resultSet != null) {
      resultSet.close()
    }
  }

  /**
   * Close Statement and ResultSet.
   *
   * @param statement
   *            The Statement to be closed.
   * @param resultSet
   *            The ResultSet to be closed.
   * @throws SQLException
   */
  protected def close(statement: Statement, resultSet: ResultSet) {
    close(resultSet)
    close(statement)
  }
}

object DefaultBooleanHandler {
  implicit val booleanHandler = new RDBMSBooleanHandler with IntRDBMSBooleanHandler
}
