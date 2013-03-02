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
package org.scalapersistenceframework.dao

import java.sql.ResultSet

/**
 * This set of traits represents several alternate ways to represent
 * the boolean type in an RDBMS. Extend it as necessary beyond the
 * int type and string type.
 *
 * @author Michael J Long 3/01/2013
 *
 * @link http://www.scalapersistenceframework.org
 */
trait RDBMSBooleanHandler {
  /**
   * This method turns the value stored in a column of some SQL type
   * and returns false if the column in the database is null. Otherwise
   * it returns true or false.
   *
   * @param resultSet the result set
   * @param columnName the name of the  column in the result set
   * @return either true or false
   * @throws SQLException
   */
  def nonNullableBoolean(resultSet: ResultSet, columnName: String): Boolean

  /**
   * This method takes a column of some SQL type that represents a boolean
   * and properly returns null if the column in the database is null. Otherwise
   * it returns true or false.
   *
   * @param resultSet the result set
   * @param columnName the name of the column in the result set
   * @return either null or true or false.
   * @throws SQLException
   */
  def nullableBoolean(resultSet: ResultSet, columnName: String): Option[Boolean]
}

trait StringRDBMSBooleanHandler extends RDBMSBooleanHandler {
  /**
   * This method takes a String boolean (Y or N) column and returns false if
   * the column in the database is null. Otherwise it returns true or false.
   *
   * @param resultSet the result set
   * @param columnName the name of the String (Y or N) column in the result set
   * @return either true or false
   * @throws SQLException
   */
  def nonNullableBoolean(resultSet: ResultSet, columnName: String) = {
    val charValue = resultSet.getString(columnName)
    if (resultSet.wasNull()) {
      false
    } else {
      if (charValue.equalsIgnoreCase("Y")) true else false
    }
  }

  /**
   * This method takes a String boolean (Y or N) column and properly returns
   * null if the column in the database is null. Otherwise it returns true or
   * false.
   *
   * @param resultSet the result set
   * @param columnName the name of the String (Y or N) column in the result set
   * @return either null or true or false.
   * @throws SQLException
   */
  def nullableBoolean(resultSet: ResultSet, columnName: String): Option[Boolean] = {
    val value = resultSet.getInt(columnName)
    if (resultSet.wasNull()) {
      None
    } else {
      Some(if (value == 1) true else false)
    }
  }
}

trait IntRDBMSBooleanHandler extends RDBMSBooleanHandler {
  /**
   * This method takes an Int column and returns false if
   * the column in the database is null. Otherwise it returns true or false.
   *
   * @param resultSet
   *            the result set
   * @param columnName
   *            the name of the String (Y or N) column in the result set
   * @return either true or false
   * @throws SQLException
   */
  def nonNullableBoolean(resultSet: ResultSet, columnName: String) = {
    val value = resultSet.getInt(columnName)
    if (resultSet.wasNull()) {
      false
    } else {
      if (value == 1) true else false
    }
  }

  /**
   * This method takes an Int column and returns
   * null if the column in the database is null. Otherwise it returns true or
   * false.
   *
   * @param resultSet the result set
   * @param columnName the name of the Int column in the result set
   * @return either null or true or false.
   * @throws SQLException
   */
  def nullableBoolean(resultSet: ResultSet, columnName: String): Option[Boolean] = {
    val value = resultSet.getInt(columnName)
    if (resultSet.wasNull()) {
      None
    } else {
      Some(if (value == 1) true else false)
    }
  }
}
