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

import java.util.logging.Logger

import scala.actors.Actor
import scala.actors.Actor.exit
import scala.actors.Actor.receiveWithin
import scala.actors.Actor.self
import scala.actors.TIMEOUT
import scala.util.Random

import org.scalapersistenceframework.DefaultTransactionPrefs.transIsolationLevel
import org.scalapersistenceframework.DefaultTransactionPrefs.transPropagation
import org.scalapersistenceframework.service.DaoService
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite

import examples.crud.Order
import examples.crud.OrderDao

object TransactionLoadTest {
  var result: Option[Order] = None
}

class TransactionLoadTest extends FunSuite with DataSourceConfigurer with BeforeAndAfterEach {
  override val logger = Logger.getLogger(this.getClass().getName())

  override def beforeEach {
    super.configureJndi
  }

  override def afterEach {
    super.cleanupTransactions
  }

  import org.scalapersistenceframework.DefaultTransactionPrefs.transIsolationLevel

  import TransactionLoadTest._

  test("Execute a bunch of destructive operations among a pool of threads") {
    val service = new DaoService(new OrderDao);
    val pk = service.insert(new Order(null, 2, "Big Order", None, false, null, Some(12), null, null, false))

    result = service.findByPk(List(pk))
    logger.info(result.toString())

    val actors = new scala.collection.mutable.ArrayBuffer[ConcurrentTransactionActor]

    val numThreads = 10
    for (i <- 0 to (numThreads - 1)) {
      actors += new ConcurrentTransactionActor
    }

    for (i <- 0 to (actors.length - 1)) {
      actors(i).start
    }
    for (i <- 0 to (actors.length - 1)) {
      for (x <- 0 to 10) {
        actors(i) ! self
      }
    }

    intercept[InterruptedException] {
      for (i <- 0 to (actors.length - 1)) {
        for (i <- 0 to (numThreads * 5)) {
          logger.info("receiving message from CurrentTransactionActor thread");
          receiveWithin(50000) {
            case TIMEOUT =>
              logger.info("Exiting initiating actor with timeout")
              exit
            case msg => logger.info(msg.toString)
          }
        }
      }
    }
  }
}

class ConcurrentTransactionActor extends Actor {
  val logger = Logger.getLogger(this.getClass().getName())
  val random = new Random(System.nanoTime())
  def act() {
    var continue = true
    while (continue) {
      receiveWithin(5000) {
        case (caller: Actor) =>
          caller ! {
            val service = new DaoService(new OrderDao);
            var order: Order = TransactionLoadTest.result.get
            val description = random.nextLong.toString
            order.description = description
            service.update(order)
            order = service.findByPk(List(order.id)).get

            "thread:" + Thread.currentThread + "done"
          }
        case "ping" => logger.info("ping!")
        case "quit" =>
          logger.info("exiting actor")
          exit
        case TIMEOUT =>
          continue = false
          logger.info("Exiting CurrentTransactionActor with timeout")
          exit
      }
    }
  }
}
