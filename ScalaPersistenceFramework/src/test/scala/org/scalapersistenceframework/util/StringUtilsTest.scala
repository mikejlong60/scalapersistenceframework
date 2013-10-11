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
package org.scalapersistenceframework.util

import org.scalatest.FunSuite
import java.util.logging.Logger

class StringUtilsTest extends FunSuite {

  val logger = Logger.getLogger(this.getClass().getName())

  test("Test isEmpty") {
    expectResult(true) { StringUtils.isEmpty(null) }
    expectResult(false) { StringUtils.isEmpty("1") }
    expectResult(true) { StringUtils.isEmpty("") }
  }

  test("Test isNotEmpty") {
    expectResult(false) { StringUtils.isNotEmpty(null) }
    expectResult(true) { StringUtils.isNotEmpty("1") }
    expectResult(false) { StringUtils.isNotEmpty("") }
  }
}