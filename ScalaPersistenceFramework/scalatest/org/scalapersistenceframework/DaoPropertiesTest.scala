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
package org.scalapersistenceframework

import org.scalatest.FunSuite
import java.util.logging.Logger

class DaoPropertiesTest extends FunSuite {

  val logger = Logger.getLogger(this.getClass().getName())

  test("Test the constructor with a good prefix") {
    val properties = new DaoProperties("postgres.jdbc")
    val result = properties.getProperty("driver")
    expectResult("org.postgresql.Driver") { result.get }
  }

  test("Test the constructor with a good prefix but a bad property key") {
    val properties = new DaoProperties("postgres.jdbc")
    val result = properties.getProperty("bogus")
    expectResult(None) { result }
  }

  test("Test the constructor with a bad prefix") {
    val properties = new DaoProperties("bogus.jdbc")
    logger.info(properties.toString())
    val result = properties.getProperty("driver")
    expectResult(None) { result }
  }
}