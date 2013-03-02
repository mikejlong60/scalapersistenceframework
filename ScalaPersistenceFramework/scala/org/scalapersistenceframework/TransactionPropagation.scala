/**
 * *****************************************************************************
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
 * ****************************************************************************
 */
package org.scalapersistenceframework

import java.rmi.RemoteException

import scala.util.Random
/**
 * This set of traits represents the transaction behaviors that are analogous
 * to the J2EE or SpringFramework transaction types.  Not all are implemented here
 * but each should extend the TransactionPropagation trait. This is the place to
 * implement transaction behavior of various types including No SQL databases.
 *
 * See the automated tests for examples of how to declare the transactional behavior
 * of a method and enclose a method in a transaction.
 *
 * @author Michael J Long 2/02/2013
 *
 * @link http://www.scalapersistenceframework.org
 */
trait TransactionPropagation {
  def enclose[T](connectionName: Option[String], codeBlock: => T)(implicit transIsolationLevel: IsolationLevel): T
}

trait Required extends TransactionPropagation {
  val Random = new Random

  override def enclose[T](connectionName: Option[String], codeBlock: => T)(implicit transIsolationLevel: IsolationLevel): T = {

    val random = new Random(System.nanoTime())
    val methodGuid = random.nextLong() + System.currentTimeMillis()
    val methodName = methodGuid.toString()
    try {
      Transaction.getInstance(connectionName).start(connectionName, methodName)
      val result = codeBlock
      Transaction.getInstance(connectionName).commit(methodName)
      result
    } catch {
      case e: Throwable => {
        Transaction.getInstance(connectionName).rollback(methodName)
        throw e
      }
    } finally {
      Transaction.getInstance(connectionName).end(connectionName, methodName)
    }
  }
}

trait Never extends TransactionPropagation {
  override def enclose[T](connectionName: Option[String], codeBlock: => T)(implicit transIsolationLevel: IsolationLevel): T = {
    if (Transaction.getInstance(connectionName).exists) throw new RemoteException("A transaction already exists for the current thread.")
    codeBlock
  }
}
