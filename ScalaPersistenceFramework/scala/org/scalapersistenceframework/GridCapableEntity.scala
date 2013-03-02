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

object PersistentOperationType extends Enumeration {
  type PersistentOperationType = Value
  val DELETE, UPDATE, NONE = Value
}

import org.scalapersistenceframework.PersistentOperationType._

/**
 * Extensions of this class are capable of persisting lists of entities using
 * the save method (see DaoService). The DaoService notates all rows coming
 * from the database via a select statement with
 * GridCapableEntity.persistent=true.
 *
 * The service layer(DaoService.save method) inserts rows if the persistent
 * field is false and the PersistentOperationType is UPDATE. It updates if the
 * persistent field is true and the PersistentOperationType is UPDATE. It deletes the
 * row if the persistent field is true and the PersistentOperationType is DELETE.
 * Entities that need to be updated using the a grid control like a DOJO data
 * grid should extend this class.
 *
 * @link http://www.scalapersistenceframework.org
 *
 * @author Michael J Long 01/23/2013
 */
abstract class GridCapableEntity(val persistent: Boolean = false) {
  var persistentOperationType: PersistentOperationType = NONE
}