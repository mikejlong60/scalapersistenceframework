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

import org.springframework.mock.jndi.SimpleNamingContextBuilder
import java.util.logging.Logger
import java.util.logging.LogManager

trait DataSourceConfigurer {
  val logger = Logger.getLogger(this.getClass().getName())

  def configureJndi {
    val builder = new SimpleNamingContextBuilder
    val ds = new org.apache.commons.dbcp.BasicDataSource
    ds.setDriverClassName("org.postgresql.Driver")
    ds.setUrl("jdbc:postgresql://localhost:5432/spf")
    ds.setUsername("postgres")
    ds.setPassword("postgres")

    builder.bind("java:comp/env/jdbc/myds", ds)
    builder.activate()
    Transaction.configure("postgres.jndi")
  }

  def configureJdbc {
    Transaction.configure("postgres.jdbc")
  }

  def cleanupTransactions {
    Transaction.closeAll
  }

}
