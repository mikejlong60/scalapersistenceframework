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
package org.scalapersistenceframework.jdbcmetadata

import java.sql.Connection
import java.sql.ResultSet
import java.util.logging.Logger
class Schema(private val conn: Connection, val schema: String) extends NameConverter with Equals {
  val logger = Logger.getLogger(this.getClass().getName())

  override val scalaName = capitalize(dbNameToVariableName(schema))
  override val variableName = decapitalize(scalaName)

  //These getters make the fields available to Velocity Templates
  def getSchema = {schema}
  def getTables = {tables}
  ////////////////////////////
  override def canEqual(other: Any) = other.isInstanceOf[Schema]
  override def hashCode = {
    val prime = 41
    prime * (prime + Schema.super.hashCode + schema.hashCode)
  }
  override def equals(other: Any) = other match {
    case that: Schema => Schema.super.equals(that) && that.canEqual(Schema.this) && this.schema == that.schema
    case _ => false
  }
  
  /** All the tables in this schema */
  val tables = addRegularTablesForSchema(conn)

  def getTable(sqlName: String): Option[Table] = {
    tables.filter(x => x.sqlName == sqlName).headOption
  }
  /**
   * Adds regular tables to the list of tables.
   * Result has to be an Array to work properly
   * with Velocity.
   *
   * @throws SQLException
   */
  private def addRegularTablesForSchema(conn: Connection) = {
    var tableRs: ResultSet = null
    try {
      tableRs = conn.getMetaData().getTables(null, schema, null, Array("TABLE"));

      new Iterator[Table] {
        def hasNext = {
          tableRs.next()
        }
        def next() = {
          val tableName = tableRs.getString("TABLE_NAME");
          val tableType = tableRs.getString("TABLE_TYPE");
          val schemaName = tableRs.getString("TABLE_SCHEM");
          logger.info("schema:" + schema + "," + schemaName);
          logger.info("table:" + tableName);
          new Table(conn, tableName, schemaName);
        }
      }.toArray[Table]
    } finally {
      tableRs.close()
    }
  }
}
