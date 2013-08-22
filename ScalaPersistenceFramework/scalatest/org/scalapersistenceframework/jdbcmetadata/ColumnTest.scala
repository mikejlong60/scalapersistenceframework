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

import org.scalatest.FunSuite

import java.util.logging.Logger

class ColumnTest extends FunSuite {

  val logger = Logger.getLogger(this.getClass().getName())

  test("test implicit conversion") {
    implicit def stringOption2String(x: Option[String]) = x.orNull
    val y = Some("Hello")
    val z = y
    expectResult("Hello") { stringOption2String(z) }
    val w = None
    expectResult(null) {w.orNull}
    val q:String = y
    expectResult("Hello") {q}
  }

  test("Test Construction with name conversions") {
    var c = new Column(1, "numeric", "customer_id", 12, 0, true, false)
    expectResult("customerId") { c.variableName }
    expectResult("CustomerId") { c.scalaName }
    intercept[IllegalArgumentException] {
      new Column(1, "numeric", null, 12, 0, true, false)
    }

    c = new Column(1, "numeric", "fred_smith_was_a_guy", 12, 0, true, false)
    expectResult("fredSmithWasAGuy") { c.variableName }
    expectResult("FredSmithWasAGuy") { c.scalaName }

    c = new Column(1, "numeric", " fred_smith_was_a_guy", 12, 0, true, false)
    expectResult(" fredSmithWasAGuy") { c.variableName }
    expectResult(" fredSmithWasAGuy") { c.scalaName }

    c = new Column(1, "numeric", "_fred_smith_was_a_guy", 12, 0, true, false)
    expectResult("fredSmithWasAGuy") { c.variableName }
    expectResult("FredSmithWasAGuy") { c.scalaName }

    c = new Column(1, "numeric", "-fred-smith-was-a-guy", 12, 0, true, false)
    expectResult("fredSmithWasAGuy") { c.variableName }
    expectResult("FredSmithWasAGuy") { c.scalaName }

    c = new Column(1, "numeric", "xyz", 12, 0, true, false)
    expectResult("xyz") { c.variableName }
    expectResult("Xyz") { c.scalaName }

    c = new Column(1, "numeric", "x", 12, 0, true, false)
    expectResult("x") { c.variableName }
    expectResult("X") { c.scalaName }
  }
}