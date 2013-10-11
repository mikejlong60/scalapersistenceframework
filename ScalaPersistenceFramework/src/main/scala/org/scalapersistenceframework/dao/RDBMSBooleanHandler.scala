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
  
  /**
   * This method takes an Option[Boolean] and turns
   * it into its SQL representation for storing in the database.
   * @param the value to convert to the database's format
   * @return Either null for None or the application-specific representation of true or false
   */
  def booleanOption2Sql(x: Option[Boolean]):Any

  /**
   * This method takes a Boolean and turns
   * it into its SQL representation for storing in the database.
   * @param the value to convert to the database's format
   * @return the application-specific representation of true or false
   */
  def boolean2Sql(x: Boolean): Any
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
  def nonNullableBoolean(resultSet: ResultSet, columnName: String): Boolean = {
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
    val charValue = resultSet.getString(columnName)
    if (resultSet.wasNull()) {
      None
    } else {
      Some(if (charValue.equalsIgnoreCase("Y")) true else false)
    }
  }
  /**
   * This method takes an Option[Boolean] and turns
   * it into its SQL representation for storing in the database.
   * @param the value to convert to the database's format
   * @return Either null for None, Y for true, or N for false 
   */
  def booleanOption2Sql(x: Option[Boolean]): String = {
    x match {
      case Some(true) => "Y"
      case Some(false) => "N"
      case None => null
    }
  }

  /**
   * This method takes a Boolean and turns
   * it into its SQL representation for storing in the database.
   * @param the value to convert to the database's format
   * @return Y for true, or N for false 
   */
  def boolean2Sql(x: Boolean): String = {
    x match {
      case true => "Y"
      case false => "N"
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
  def nonNullableBoolean(resultSet: ResultSet, columnName: String): Boolean = {
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
  
  /**
   * This method takes an Option[Boolean] and turns
   * it into its SQL representation for storing in the database.
   * @param the value to convert to the database's format
   * @return Either null for None, 1 for true, or 0 for false 
   */
  def booleanOption2Sql(x: Option[Boolean]): Any = {
    x match {
      case Some(true) => 1
      case Some(false) => 0
      case None => null
    }
  }

  /**
   * This method takes a Boolean and turns
   * it into its SQL representation for storing in the database.
   * @param the value to convert to the database's format
   * @return 1 for true, or 0 for false 
   */
  def boolean2Sql(x: Boolean): Int = {
    x match {
      case true => 1
      case false => 0
    }
  }
}