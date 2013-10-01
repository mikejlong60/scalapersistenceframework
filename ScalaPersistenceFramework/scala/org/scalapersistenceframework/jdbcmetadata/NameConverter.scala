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

import org.scalapersistenceframework.util.StringUtils

abstract class NameConverter extends Equals {
  val scalaName: String
  val variableName: String
  
  //These two getters make the fields available to Velocity Templates
  lazy val getScalaName = {scalaName}
  lazy val getVariableName = {variableName}

  override def canEqual(other: Any) = other.isInstanceOf[NameConverter]
  override def hashCode = {
    val prime = 41
    prime * (prime + scalaName.hashCode)
  }
  override def equals(other: Any) = other match {
    case that: NameConverter => that.canEqual(NameConverter.this) && this.scalaName == that.scalaName
    case _ => false
  }

  /**
   * Makes the first character of a name lower case.
   *
   * @param name a name
   * @return the name with the first letter lower case.
   */
  protected def decapitalize(name: String): String = {
    if (StringUtils.isEmpty(name)) {
      throw new IllegalArgumentException("The name cannot be null or empty.")
    }
    if (StringUtils.isEmpty(name))
      throw new IllegalArgumentException(
        "The name cannot be null or empty.")
    name.length match {
      case 1 => name.toLowerCase
      case _ => {
        val nocaps = name.substring(0, 1).toLowerCase()
        val rest = name.substring(1)
        nocaps + rest
      }
    }
  }

  /**
   * Converts a database name (table or column) to a java name (first letter
   * decapitalised). employee_name -> employeeName.
   *
   * @param s
   *            The database name to convert.
   * @return The converted database name.
   */
  protected def dbNameToVariableName(name: String) = {
    if (StringUtils.isEmpty(name))
      throw new IllegalArgumentException("The name cannot be null or empty.")

    "[_-]([a-z\\d])".r.replaceAllIn(name, { m =>
      m.group(1).toUpperCase()
    })
  }
  
  /**
   * Makes the first character of a name upper case.
   *
   * @param name a name
   * @return the name with the first letter upper case.
   */
  protected def capitalize(name: String): String = {
    if (StringUtils.isEmpty(name))
      throw new IllegalArgumentException(
        "The name cannot be null or empty.")
    name.length match {
      case 1 => name.toUpperCase
      case _ => {
        val caps = name.substring(0, 1).toUpperCase()
        val rest = name.substring(1)
        caps + rest
      }
    }
  }
}