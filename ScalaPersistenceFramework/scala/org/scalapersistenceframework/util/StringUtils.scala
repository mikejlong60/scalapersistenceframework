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
package org.scalapersistenceframework.util

/**
 * This class contains common string operations like those found in the commons lang project.
 *
 * Copyright 2013 Functionicity, LLC. All Rights Reserved.
 * @link http://www.scalapersistenceframework.org
 *
 * @author Mike Long 02/22/2013
 *
 */
object StringUtils {

  def isEmpty(s: String) = {
    s == null || s.trim.isEmpty
  }
}