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
package examples.query

import java.sql.ResultSet
import java.util.logging.Logger

import scala.collection.immutable.List

import org.scalapersistenceframework.dao.CrudDao
import org.scalapersistenceframework.dao.DefaultBooleanHandler.booleanHandler
import org.scalapersistenceframework.dao.QueryDao

import examples.crud.Address
import examples.crud.Order

class AddressDaoWithCustomQueries(override val connectionName: Option[String]) extends CrudDao[Address] with QueryDao {
  override val logger = Logger.getLogger(this.getClass().getName())

  def this() {
    this(None)
  }

  def validatePkForUpdate(vo: Address): Unit = validatePkForInsert(vo)
  def validatePkForInsert(vo: Address): Unit = {
    if (vo.id == null) throw new IllegalArgumentException("Missing ID")
  }

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
  override def mapForSelect(resultSet: ResultSet): Address = {
    new Address(resultSet.getLong("id"), resultSet.getString("name"), resultSet.getString("line1"), resultSet.getString("line2"), resultSet.getString("state"), resultSet.getString("zipcode"), resultSet.getTimestamp("created_ts"), resultSet.getTimestamp("updated_ts"), resultSet.getInt("persistent") match { case 1 => true case _ => false })
  }
  override def mapForUpdate(vo: Address): List[Any] = {
    List(vo.name, vo.line1, vo.line2, vo.state, vo.zipcode, vo.id)
  }
  override def mapForInsert(vo: Address): List[Any] = {
    List(vo.id, vo.name, vo.line1, vo.line2, vo.state, vo.zipcode)
  }
  override def mapForDelete(vo: Address): List[Any] = {
    List(vo.id)
  }

  override def getSQL_FIND_BY_ID(): String = { "select id, name, line1, line2, state, zipcode, created_ts, updated_ts, 1 as persistent from spf.address where id=?" }
  override def getSQL_LIST_ORDER_BY_ID(): String = { "select id, name, line1, line2, state, zipcode, created_ts, updated_ts, 1 as persistent from spf.address order by id" }
  override def getSQL_INSERT(): String = { "insert into spf.address (id, name, line1, line2, state, zipcode, created_ts, updated_ts) values (?,?,?,?,?,?,current_timestamp, current_timestamp)" }
  override def getSQL_UPDATE(): String = { "update spf.address set id=?,name=?, line1=?, line2=?, state=?, zipcode=?, updated_ts = current_timestamp where id=?" }
  override def getSQL_DELETE(): String = { "delete from spf.address where id=?" }

  def getAllBigOrders(description: String): Set[Order] = {
    executeQueryThatReturnsMultiRows[Order](getOrderQuery, mapForOrderQuery, List("%" + description + "%"))
  }

  def mapForOrderQuery(resultSet: ResultSet): Option[Order] =
    Some(new Order(resultSet.getLong("id"), resultSet.getLong("customer_id"), resultSet.getString("description"), nullableBoolean(resultSet, "complete"), resultSet.getInt("approved") match { case 1 => true case 0 => false }, None, nullableInteger(resultSet, "order_qty"), resultSet.getTimestamp("created_ts"), resultSet.getTimestamp("updated_ts"), resultSet.getInt("persistent") match { case 1 => true case _ => false }))

  def getOrderQuery(): String = { "select id, customer_id, description, complete, approved, order_qty, created_ts, updated_ts, 1 as persistent from spf.order where description like(?)" }

}