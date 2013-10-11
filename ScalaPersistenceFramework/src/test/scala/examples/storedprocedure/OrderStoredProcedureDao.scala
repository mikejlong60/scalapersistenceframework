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
package examples.storedprocedure

import java.sql.ResultSet

import org.scalapersistenceframework.dao.DefaultBooleanHandler.booleanHandler
import org.scalapersistenceframework.dao.StoredProcedureDao

import examples.crud.Order

class OrderStoredProcedureDao(override val connectionName: Option[String]) extends StoredProcedureDao {

  def this() {
    this(None)
  }

  def getAllOrdersUsingStoredProcedure(): Set[Order] = {
    this.executeStoredProcedureThatIteratesOverRefCursor(() => "{ ? = call samplerefcursorfunc() }", mapForOrderQuery, List())
  }

  private def mapForOrderQuery(resultSet: ResultSet): Some[Order] = Some(
          new Order(nonNullableLong(resultSet,"id"), nonNullableLong(resultSet,"customer_id"), nullableString(resultSet,"description"), nullableBoolean(resultSet, "complete"), nonNullableBoolean(resultSet,"approved"), None, nullableInteger(resultSet, "order_qty"), nonNullableTimestamp(resultSet,"created_ts"), nonNullableTimestamp(resultSet,"updated_ts"), true)
)
  
}