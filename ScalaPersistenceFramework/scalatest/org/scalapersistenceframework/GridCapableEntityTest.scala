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

import org.scalatest.FunSuite

class GridCapableEntityTest extends FunSuite {

  class SomeGridCapableEntity(val id: Int, val description: String) extends GridCapableEntity {

  }

  test("Test the constructor of a subclass of GridCapableEntity") {
    val entity = new SomeGridCapableEntity(12, "foo")
    expectResult(PersistentOperationType.NONE) { entity.persistentOperationType }
    expectResult(false) { entity.persistent }
    expectResult("foo") { entity.description }
    expectResult(12) { entity.id }
    val entity2 = new SomeGridCapableEntity(13, "fred")
    expectResult("fred") { entity2.description }
    expectResult(13) { entity2.id }

  }

}