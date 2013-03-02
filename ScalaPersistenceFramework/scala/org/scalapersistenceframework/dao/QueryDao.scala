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

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.util.logging.Logger

import org.scalapersistenceframework.Transaction

/**
 * This class is a data access object that provides features to support
 * queries of an arbitrary type that return a single or set of value objects
 * of a given type.
 *
 * @link http://www.scalapersistenceframework.org
 *
 * @author Michael J Long 02/22/2013
 *
 */
trait QueryDao extends BasicDao {
  override val logger = Logger.getLogger(this.getClass().getName())

  def executeQueryThatReturnsSingleRow[T](query: () => String, mapper: ResultSet => Option[T], values: List[Any]): Option[T] = {
    var connection: Connection = null
    var preparedStatement: PreparedStatement = null
    var resultSet: ResultSet = null
    var vo: Option[T] = None

    try {
      connection = Transaction.getInstance(connectionName).getConnectionForTransaction()
      preparedStatement = prepareStatement(connection,
        query(), false, values)
      resultSet = preparedStatement.executeQuery()
      if (resultSet.next()) {
        vo = mapper(resultSet)
      }
    } finally {
      close(preparedStatement, resultSet)
    }
    vo
  }

  /**
   * Execute the given query and return the result set mapped to value
   * objects.
   *
   * @param queryMapper
   * @param values
   * @return
   * @throws SQLException
   */
  def executeQueryThatReturnsMultiRows[T](query: () => String, mapper: ResultSet => Option[T], values: List[Any]): Set[T] = {
    var connection: Connection = null
    var preparedStatement: PreparedStatement = null
    var resultSet: ResultSet = null
    var result = Set[T]()

    try {
      connection = Transaction.getInstance(connectionName).getConnectionForTransaction()
      preparedStatement = prepareStatement(connection, query(), false, values)
      resultSet = preparedStatement.executeQuery()
      result = new Iterator[T] {
        def hasNext = {
          resultSet.next()
        }
        def next() = {
          var vo: Option[T] = mapper(resultSet)
          vo.get
        }
      }.toSet[T]
    } finally {
      close(preparedStatement, resultSet)
    }
    result
  }
}