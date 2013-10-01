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
package org.scalapersistenceframework.jdbcmetadata.velocity

import java.util.logging.Logger

import org.scalapersistenceframework.DataSourceConfigurer
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import java.io.File
import org.scalapersistenceframework.util.FileHelper.file2helper

class VelocityTemplateFileGeneratorTest extends FunSuite with DataSourceConfigurer with BeforeAndAfterEach {
  override val logger = Logger.getLogger(this.getClass().getName())

  override def beforeEach {
    super.configureJdbc
  }

  override def afterEach {
    super.cleanupTransactions
  }

  test("Test generation of a file from a template using jdbc metadata") {
    val generator = new VelocityTemplateFileGenerator
    generator.generate

    val f1 = new File("AddressDao.scala");
    val f2 = new File("AddressTest.scala");
    val f3 = new File("OrderDao.scala");
    val f4 = new java.io.File("OrderTest.scala");
    expectResult(true) { f1.exists }
    expectResult(true) { f2.exists }
    expectResult(true) { f3.exists }
    expectResult(true) { f4.exists }
    f1.delete
    f2.delete
    f3.delete
    f4.delete
    expectResult(false) { f1.exists }
    expectResult(false) { f2.exists }
    expectResult(false) { f3.exists }
    expectResult(false) { f4.exists }
  }
}