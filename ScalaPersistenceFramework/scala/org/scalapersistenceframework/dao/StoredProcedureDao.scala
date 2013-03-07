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
package org.scalapersistenceframework.dao

import java.sql.CallableStatement
import java.sql.Connection
import java.sql.ResultSet
import java.sql.Types
import java.util.logging.Logger

import org.scalapersistenceframework.Transaction

/**
 * This class is a data access object that provides features to support
 * execution of stored procedures. For now it is really rudimentary, only
 * supports a single method that iterates over the cursor returned by
 * a stored procedure. But see the JDBC docs and add whatever else you
 * need, either here or by extension of this trait.
 *
 * @link http://www.scalapersistenceframework.org
 *
 * @author Mike Long 02/22/2013
 *
 */
trait StoredProcedureDao extends BasicDao {
  override val logger = Logger.getLogger(this.getClass().getName())

  def executeStoredProcedureThatIteratesOverRefCursor[T](query: () => String, mapper: ResultSet => Option[T], values: List[Any]): Set[T] = {
    var connection: Connection = null
    var callableStatement: CallableStatement = null
    var resultSet: ResultSet = null
    var vo: Option[T] = None

    try {
      connection = Transaction.getInstance(connectionName).getConnectionForTransaction()
      callableStatement = prepareCallableStatement(connection, query(), values)
      callableStatement.registerOutParameter(1, Types.OTHER)
      callableStatement.execute()
      var resultSet = callableStatement.getObject(1) match {
        case resultSet: ResultSet => resultSet
        case _ => throw new ClassCastException("Not a supported return type. Currently only supports a refCursor that is a JDBC ResultSet.")
      }
      new Iterator[T] {
        def hasNext = {
          resultSet.next()
        }
        def next() = {
          var vo: Option[T] = mapper(resultSet)
          vo.get
        }
      }.toSet[T]
    } finally {
      close(callableStatement, resultSet)
    }
  }
}