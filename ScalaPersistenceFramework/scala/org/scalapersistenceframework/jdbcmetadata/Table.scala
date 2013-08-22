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
import java.util.logging.Logger
import java.sql.DatabaseMetaData
import java.sql.ResultSet
/**
 * This class represents a table in a database.
 *
 * @author Mike Long
 * @created 06/01/2012
 */
class Table(private val conn: Connection, val sqlName: String, val schema: String) extends NameConverter with Equals {

  val logger = Logger.getLogger(this.getClass().getName())

  override val scalaName = capitalize(dbNameToVariableName(sqlName))
  override val variableName = decapitalize(scalaName)

  /** All the primary key columns */
  val primaryKeyFieldNames = populatePrimaryKeyFieldNames
  /** All the columns of this table except for the primary key columns */
  private val columns = populateColumns
  private val primaryKeys = columns.filter(column => column.isPk)

  //These getters make the fields available to Velocity Templates
  def getSchema = { schema }
  def getSqlName = { sqlName }
  def getColumns = { columns }
  def getColumnsSize = { columns.size }
  def getPrimaryKeys = { primaryKeys }
  def getPrimaryKeysSize = { primaryKeys.size }
  def getNonPrimaryKeysSize = {columns.size - primaryKeys.size}
  def getNonPrimaryKeyColumns = {columns.filter(column => !column.isPk)}
  ////////////////////////////

  override def canEqual(other: Any) = other.isInstanceOf[Table]
  override def hashCode = {
    val prime = 41
    prime * (prime + Table.super.hashCode + sqlName.hashCode) + sqlName.hashCode + sqlName.hashCode
  }

  override def equals(other: Any) = other match {
    case that: Table => Table.super.equals(that) && that.canEqual(Table.this) && this.sqlName == that.sqlName
    case _ => false
  }

  def getColumn(sqlName: String): Option[Column] = {
    columns.filter(column => { column.sqlName == sqlName }).headOption
  }

  private def populateColumns = {
    // In case none of the columns were primary keys, issue a warning.
    if (primaryKeyFieldNames.size == 0) {
      logger.info("WARNING: The JDBC driver didn't report any primary key columns in " + sqlName);
    }
    var columnRs: ResultSet = null
    try {
      columnRs = conn.getMetaData().getColumns(null, schema, sqlName, null);

      new Iterator[Column] {
        def hasNext = {
          columnRs.next()
        }
        def next() = {
          val sqlType = columnRs.getInt("DATA_TYPE")
          val sqlTypeName = columnRs.getString("TYPE_NAME")
          val columnName = columnRs.getString("COLUMN_NAME")
          // if columnNoNulls or columnNullableUnknown assume "not nullable"
          val isNullable = DatabaseMetaData.columnNullable == columnRs.getInt("NULLABLE")
          val size = columnRs.getInt("COLUMN_SIZE")
          val decimalDigits = columnRs.getInt("DECIMAL_DIGITS")
          logger.info("getting column:" + columnName + " sqlType:" + sqlType + " sqlTypeName:" +sqlTypeName + " size:" + size + " decimalDigits:" + decimalDigits)
          val isPk = primaryKeyFieldNames.contains(columnName)
          new Column(sqlType, sqlTypeName, columnName, size, decimalDigits, isPk, isNullable);
        }
      }.toArray[Column]
    } finally {
      columnRs.close()
    }
  }

  private def populatePrimaryKeyFieldNames = {
    var primaryKeyRs: ResultSet = null
    try {
      primaryKeyRs = conn.getMetaData().getPrimaryKeys(null, schema, sqlName)

      new Iterator[String] {
        def hasNext = {
          primaryKeyRs.next()
        }
        def next() = {
          primaryKeyRs.getString("COLUMN_NAME")
        }
      }.toArray[String]
    } finally {
      primaryKeyRs.close()
    }
  }
}
