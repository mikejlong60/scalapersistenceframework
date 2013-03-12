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
package org.scalapersistenceframework

import java.util.Properties

/**
 * This class immediately loads the DAO properties file 'dao.properties' once in
 * memory and provides a constructor which takes the specific key which is to be
 * used as property key prefix of the DAO properties file. There is a property
 * getter which only returns the property prefixed with 'specificKey'.
 *
 * The sole constructor creates a DAOProperties instance for the given specific key which is to
 * be used as property key prefix of the DAO properties file.
 *
 * @param specificKey The specific key which is to be used as property key prefix.
 *
 * @link http://www.scalapersistenceframework.org
 *
 * @author Michael J Long 01/23/2013
 */

class DaoProperties(val specificKey: String) {

  // Constants
  // ----------------------------------------------------------------------------------
  private final val PROPERTIES_FILE = "dao.properties"
  private final val PROPERTIES = new Properties()

  {
    val classLoader = Thread.currentThread().getContextClassLoader()
    val propertiesFile = classLoader.getResourceAsStream(PROPERTIES_FILE)
    if (propertiesFile == null)
      throw new IllegalStateException("The properties file [" + PROPERTIES_FILE + "] does not exist at the root of the classpath.")
    PROPERTIES.load(propertiesFile)
  }

  /**
   * Returns an Option[String] instance-specific property value associated
   * with the given key(prefixed by the specificKey value you used to create the DaoProperties instance).
   *
   * @param key
   *            The key to be associated with a DAOProperties instance
   *            specific value.
   * @return The DAOProperties instance specific property value associated
   *         with the given key or None.
   */
  def getProperty(key: String): Option[String] = {
    val fullKey = specificKey + "." + key
    val result = PROPERTIES.getProperty(fullKey)
    if (result != null) {
      Some(result)
    } else {
      None
    }
  }
}
