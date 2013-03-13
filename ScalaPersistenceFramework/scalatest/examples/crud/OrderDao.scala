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
package examples.crud

import java.sql.ResultSet
import java.sql.Timestamp
import scala.collection.immutable.List
import org.scalapersistenceframework.GridCapableEntity
import org.scalapersistenceframework.dao.CrudDao
import org.scalapersistenceframework.dao.DefaultBooleanHandler.booleanHandler
import org.scalapersistenceframework.dao.RDBMSBooleanHandler

case class Order(var id: java.lang.Long, var customerId: Long, var description: Option[String], var complete: Option[Boolean], var approved: Boolean, var primaryAddress: Option[Address] = None, var orderQty: Option[Integer] = None, var createdTs: Timestamp, var updatedTs: Timestamp, override val persistent: Boolean) extends GridCapableEntity(persistent) with java.io.Serializable {
  override def hashCode = (41 * (41 + id)).toInt
  override def equals(other: Any) = other match {
    case that: Order => this.id == that.id
    case _ => false
  }
}

class OrderDao(override val connectionName: Option[String]) extends CrudDao[Order] {
  def this() {
    this(None)
  }

  def validatePkForUpdate(vo: Order): Unit = {
    if (vo.id == null) throw new IllegalArgumentException("You have specify the PK for an update.")
  }

  def validatePkForInsert(vo: Order): Unit = {
    if (vo.id != null) throw new IllegalArgumentException("The Order table uses an auto-generated key. It needs to be null for an insert.")
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
  override def mapForSelect(resultSet: ResultSet): Order = {
    new Order(nonNullableLong(resultSet, "id"), nonNullableLong(resultSet, "customer_id"), nullableString(resultSet, "description"), nullableBoolean(resultSet, "complete"), nonNullableBoolean(resultSet, "approved"), None, nullableInteger(resultSet, "order_qty"), nonNullableTimestamp(resultSet, "created_ts"), nonNullableTimestamp(resultSet, "updated_ts"), nonNullableBoolean(resultSet, "persistent"))
  }

  override def mapForUpdate(vo: Order)(implicit booleanHandler: RDBMSBooleanHandler): List[Any] = {
    List(vo.customerId, vo.description.orNull, booleanHandler.booleanOption2Sql(vo.complete), booleanHandler.boolean2Sql(vo.approved), vo.orderQty.orNull, vo.id)
  }

  override def mapForInsert(vo: Order)(implicit booleanHandler: RDBMSBooleanHandler): List[Any] = {
    List(vo.customerId, vo.description.orNull, booleanHandler.booleanOption2Sql(vo.complete), booleanHandler.boolean2Sql(vo.approved), vo.orderQty.orNull)
  }
  override def mapForDelete(vo: Order): List[Any] = {
    List(vo.id)
  }

  override def getSQL_FIND_BY_ID(): String = { "select id, customer_id, description, complete, approved, order_qty, created_ts, updated_ts, 1 as persistent from spf.order where id=?" }
  override def getSQL_LIST_ORDER_BY_ID(): String = { "select id, customer_id, description, complete, approved, order_qty, created_ts, updated_ts, 1 as persistent from spf.order order by id" }
  override def getSQL_INSERT(): String = { "insert into spf.order (customer_id, description, complete, approved, order_qty, created_ts, updated_ts) values (?,?,?,?,?,current_timestamp, current_timestamp)" }
  override def getSQL_UPDATE(): String = { "update spf.order set customer_id=?, description=?, complete=?, approved=?, order_qty=?, updated_ts = current_timestamp where id=?" }
  override def getSQL_DELETE(): String = { "delete from spf.order where id=?" }

}