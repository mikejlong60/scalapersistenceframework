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

import java.sql.Connection
import java.sql.DriverManager
import java.util.logging.Logger

import org.scalapersistenceframework.util.StringUtils

import javax.naming.InitialContext
import javax.sql.DataSource

/**
 * This class represents a transaction for an SQL database. Use
 * {@link #configure(String)} to obtain to configure the transaction object for
 * given database name. Then use {@link #configure(String)} to get an instance
 * for use in a DAO or transaction service object. The specific instance
 * returned depends on the properties file configuration. It provides methods to
 * start a transaction, get the current connection for a transaction, commit a
 * transaction, and roll back a transaction. Transactions are assigned per
 * thread so that this class can behave according to the rules of EJB or
 * Springframework declarative transactions. A typical use case for this class
 * is as follows: 1) A method registers itself as owning a transaction using
 * start(String transactionOwner). This method does nothing if there is already
 * a transaction in process for the current thread. 2) A DAO class obtains the
 * connection associated with the current transaction from the
 * currentTransactionForThread instance variable and issues some SQL statements.
 * The currentTransactionForThread field uses thread local storage. 3) The
 * method that started the transaction commits the transaction using the commit
 * method here. 4)The owner of the transaction then ends the transaction to
 * clean up resources(e.g. Return the database connection to the connection pool
 * and destroying the threadLocal to prevent memory leaks.
 *
 *
 * This class contains an internal CurrentConnection object which contains the
 * database connection and the transaction owner. This mechanism allows methods
 * to behave as per the "REQUIRED" EJB transaction attribute.
 *
 * <p>
 *
 * From the EJB Documentation: Required Attribute - If the client is running
 * within a transaction and invokes the enterprise bean's method, the method
 * executes within the clien's transaction. If the client is not associated
 * with a transaction, the container starts a new transaction before running the
 * method.
 *
 * <p>
 *
 * In Addition, this class requires a properties file named 'dao.properties' at the root
 * of the classpath with each of the following properties:
 *
 * <pre>
 * name.url *
 * name.driver
 * name.username
 * name.password
 * </pre>
 *
 * Those marked with * are required, others are optional and can be left away or
 * empty. Only the username is required when any password is specified.
 * <ul>
 * <li>The 'name' must represent the database name in
 * {@link #getInstance(String)}.</li>
 * <li>The 'name.url' must represent either the JDBC URL or JNDI name of the
 * database.</li>
 * <li>The 'name.driver' must represent the full qualified class name of the
 * JDBC driver.</li>
 * <li>The 'name.username' must represent the username of the database login.</li>
 * <li>The 'name.password' must represent the password of the database login.</li>
 * </ul>
 * If you specify the driver property, then the url property will be assumed as
 * JDBC URL. If you omit the driver property, then the url property will be
 * assumed as JNDI name. When using JNDI with username/password preconfigured,
 * you can omit the username and password properties as well.
 * <p
 * Here are basic examples of valid properties for a database with the name
 * 'javabase':
 *
 * <pre>
 * javabase.jdbc.url = jdbc:mysql://localhost:3306/javabase
 * javabase.jdbc.driver = com.mysql.jdbc.Driver
 * javabase.jdbc.username = java
 * javabase.jdbc.password = d$7hF_r!9Y
 * </pre>
 *
 * <pre>
 * javabase.jndi.url = java:comp/env/jdbc/myds
 * </pre>
 *
 * Here is a basic use example:
 *
 * <pre>
 * Transaction.configure(&quot;postgres.jdbc&quot;)
 * </pre>
 *
 * @link http://www.scalapersistenceframework.org
 *
 * @author Michael J Long 01/23/2013
 */

abstract class Transaction {
  val logger = Logger.getLogger(this.getClass().getName())

  val threadLocal = new ThreadLocal[CurrentTransaction]()

  /**
   * Returns a connection to the database.
   *
   * @return A connection to the database.
   * @throws SQLException
   *             If acquiring the connection fails.
   */
  def getConnection(): Connection

  /**
   * This method gets the connection associated with a transaction.
   */
  def getConnectionForTransaction() = {
    val current = threadLocal.get()
    if (current != null && current.connection != null) {
      current.connection
    } else {
      throw new IllegalStateException(
        "Either the currentTransaction is null or it does not have a database connection.")
    }
  }

  /**
   * This method commits the transaction if the currentMethodName owns the
   * transaction. Otherwise it does nothing.
   *
   * @param currentMethodName
   *            - An identifier uniquely naming the method.
   * @throws SQLException
   */
  def commit(currentMethodName: String) {
    if (StringUtils.isEmpty(currentMethodName)) {
      throw new IllegalArgumentException(
        "The currentMethodName cannot be null or empty.")
    }
    val current = threadLocal.get()
    if (currentMethodName.equals(current.transactionOwner)) {
      logger.info("committing transaction for connection:"
        + current.connection.hashCode())
      current.connection.commit()
    } else {
      logger.info("Not committing transaction for connection because this method is not the owner.")
    }
  }

  /**
   * This method rolls back the transaction if the currentMethodName owns the
   * transaction. Otherwise it marks it for rollback by the owning method
   * name.
   *
   * @param currentMethodName
   *            - An identifier uniquely naming the method.
   * @throws SQLException
   */
  def rollback(currentMethodName: String) {
    if (StringUtils.isEmpty(currentMethodName)) {
      throw new IllegalArgumentException(
        "The currentMethodName cannot be null or empty.")
    }
    val current = threadLocal.get()
    if (currentMethodName.equals(current.transactionOwner)) {
      logger.info("rolling back transaction for connection:"
        + current.connection.hashCode())
      current.connection.rollback()
    } else {
      logger.info("Not rolling back transaction for connection because this method is not the owner.")
    }
  }

  /**
   * This method creates a new connection for the thread and stores it in
   * thread local storage for use by other methods in the current thread. If a
   * transaction has already been started for the thread it does nothing.
   *
   * Note the setting of the connection's transaction isolation level to the value
   * of the implicit parameter isolationLevel.
   *
   * @param currentMethodName An identifier uniquely naming the method.
   * @throws SQLException
   */

  def start(connectionName: Option[String], currentMethodName: String)(implicit transIsolationLevel: IsolationLevel) {
    if (threadLocal.get() == null) {
      logger.info("starting transaction")
      val currentTransaction = new CurrentTransaction(currentMethodName, connectionName)

      //Setting the TransactionIsolationLevel on the connection to NONE throws an exception which is 
      //somehow getting swallowed even if I try to catch and re-throw it here. To fix this
      //I eliminated the NONE option in the case class TransactionIsolation. Mike Long 2/7/2013
      currentTransaction.connection.setTransactionIsolation(transIsolationLevel.level)

      threadLocal.set(currentTransaction)
    }
  }

  /**
   * This returns true if a transaction exists for the current thread.
   *
   */
  def exists() = {
    threadLocal.get() != null
  }

  /**
   * This method ends the transaction for the thread if the transaction name
   * (usually the method name with some UID appended to allow recursive calls)
   * matches the currentMethodName. Otherwise it does nothing.
   *
   * For the former it currently returns the connection to the pool or closes
   * it.
   *
   * Note the setting of the connection's transaction isolation level back to its default. The
   * start method may have changed this to another value.
   *
   *     @param currentMethodName An identifier uniquely naming the method.
   * @throws SQLException
   */
  def end(connectionName: Option[String], currentMethodName: String) {
    if (StringUtils.isEmpty(currentMethodName)) {
      throw new IllegalArgumentException(
        "The currentMethodName cannot be null or empty.")
    }
    val current = threadLocal.get()
    if (currentMethodName.equals(current.transactionOwner)) {
      logger.info("Ending transaction.")
      current.connection.setTransactionIsolation(current.defaultTransactionIsolation) //Put back the default isolation level
      Transaction.getInstance(connectionName).close(current.connection)
      threadLocal.remove()
    } else {
      logger.info("Not ending transaction because this method is not the owner.")
    }
  }

  /**
   * Close the Connection.
   *
   * @param connection
   *            The Connection to be closed quietly.
   * @throws SQLException
   */
  private def close(connection: Connection) {
    if (connection != null) {
      connection.close()
    }
  }

}

case class DataSourceTransaction(val dataSource: DataSource) extends Transaction {

  def getConnection: Connection = {
    return dataSource.getConnection
  }

}

case class DriverManagerTransaction(val url: String, val username: String, val password: String) extends Transaction {

  if (url == null || url.length == 0) throw new IllegalArgumentException("url cannot be empty.")
  if (username == null || username.length == 0) throw new IllegalArgumentException("username cannot be empty.")
  if (password == null || password.length == 0) throw new IllegalArgumentException("password cannot be empty.")
  def getConnection: Connection = {
    return DriverManager.getConnection(url, username, password)
  }
}

case class DataSourceWithLoginTransaction(val dataSource: DataSource, val username: String, val password: String) extends Transaction {

  def getConnection: Connection = {
    return dataSource.getConnection(username, password)
  }

}
/**
 * Note the recording of the connection's default transaction isolation level
 * and the setting of the connection's transaction isolation level to the value
 * of the implicit parameter isolationLevel.
 */
class CurrentTransaction(val transactionOwner: String, val connectionName: Option[String]) {

  if (transactionOwner == null || transactionOwner.isEmpty)
    throw new IllegalArgumentException("The transactionOwner cannot be null or empty.")
  val connection: Connection = Transaction.getInstance(connectionName).getConnection()
  connection.setAutoCommit(false)
  val defaultTransactionIsolation = connection.getTransactionIsolation()
}

object Transaction {
  val logger = Logger.getLogger(Transaction.this.getClass().getName())

  import Transaction._

  /**
   * This map is a map of configured Transaction objects registered by
   * name via the Transaction.configure method.
   */
  private var transactions: Map[String, Transaction] = Map.empty

  /**
   * This field is the single configured Transaction object registered via the Transaction.configure method.
   */
  final val PROPERTY_URL = "url"
  final val PROPERTY_DRIVER = "driver"
  final val PROPERTY_USERNAME = "username"
  final val PROPERTY_PASSWORD = "password"

  /**
   * Creates a new Transaction object for the given database name and adds it
   * to the list of Transaction instances available with getInstance. If a
   * transaction already exists for the given database name it does not
   * replace it.
   *
   * @param connectionName
   *            The database name to configure a new Transaction instance for.
   * @throws IllegalStateException
   *             If the database name is null, or if the properties file is
   *             missing in the classpath or cannot be loaded, or if a
   *             required property is missing in the properties file, or if
   *             either the driver cannot be loaded or the datasource cannot
   *             be found.
   *
   */
  def configure(connectionName: String) {
    Transaction.this.synchronized {
      if (connectionName == null || connectionName.length == 0) {
        throw new IllegalArgumentException("The connection name was null or empty.")
      }

      var namedTransaction: Transaction = null
      val properties = new DaoProperties(connectionName)
      val url = properties.getProperty(PROPERTY_URL)
      val driverClassName = properties.getProperty(PROPERTY_DRIVER)
      val password = properties.getProperty(PROPERTY_PASSWORD)
      val username = properties.getProperty(PROPERTY_USERNAME)

      // If driver is specified, then load it to let it register itself with
      // DriverManager.
      if (driverClassName != None) {
        Class.forName(driverClassName.get)
        namedTransaction = new DriverManagerTransaction(url.get, username.get, password.get)
      } // Else assume URL as DataSource URL and lookup it in the JNDI.
      else {
        val dataSource: DataSource = new InitialContext().lookup(url.get) match {
          case good: DataSource => good
          case _ => throw new IllegalStateException("Couldn't find a JNDI DataSource with the name[" + url.get + "]")
        }
        if (username != None) {
          namedTransaction = new DataSourceWithLoginTransaction(dataSource, username.get, password.get)
        } else {
          namedTransaction = new DataSourceTransaction(dataSource)
        }
      }
      transactions += ((connectionName, namedTransaction))
      logger.info(transactions.toString)
    }
  }

  /**
   * This method returns the default configured Transaction instance. This
   * mechanism is not meant to support distributed transactions. It merely
   * allows different named database connections for a given application.
   *
   *
   * @return the current default(there can only be one) transaction instance.
   * @throws IllegalStateException
   *             if more than one transaction has been configured. You must
   *             use the overloaded version of getInstance(String name) if you
   *             have transactions to more than one database.
   *
   * @see Transaction.configure(String connectionName)
   */
  def getInstance: Transaction = {
    if (transactions.size == 0) {
      throw new IllegalStateException(
        "You must first configure the transaction with database connection information.")
    }
    if (transactions.size > 1) {
      throw new IllegalStateException(
        "More than one transaction has been configured via Transaction.configure. Please use the overloaded version of this method which takes a connectionName parameter.")
    }
    val (key, value) = transactions.head
    value
  }

  /**
   * This method returns the Transaction instance configured with the given
   * connectionName. This mechanism is not meant to support distributed
   * transactions. It merely allows different named database connections for a
   * given application.
   *
   * @param connectionName
   *            The database name to return a new Transaction instance for.
   * @return the Transaction with the given name.
   * @throws IllegalStateException
   *             if more than one transaction has been configured. You must
   *             use the overloaded version of getInstance(String name) if you
   *             have transactions to more than one database.
   *
   * @see Transaction.configure(String connectionName)
   */
  def getInstance(connectionName: Option[String]): Transaction = {
    if (transactions.size == 0) {
      throw new IllegalArgumentException(
        "You must first configure the transaction with database connection information.")
    }

    connectionName match {
      case None =>
        getInstance
      case null =>
        throw new IllegalArgumentException("The connectionName is null.")
      case _ =>
        transactions.get(connectionName.get).getOrElse {
          throw new IllegalArgumentException("No database named["
            + connectionName
            + "] has been configured on the Transaction object.")
        }
    }
  }

}

sealed trait IsolationLevel {
  val level: Int
}

case object TRANSACTION_READ_COMMITTED extends IsolationLevel {
  override val level: Int = Connection.TRANSACTION_READ_COMMITTED
}

case object TRANSACTION_READ_UNCOMMITTED extends IsolationLevel {
  override val level: Int = Connection.TRANSACTION_READ_UNCOMMITTED
}
case object TRANSACTION_REPEATABLE_READ extends IsolationLevel {
  override val level: Int = Connection.TRANSACTION_REPEATABLE_READ
}
case object TRANSACTION_SERIALIZABLE extends IsolationLevel {
  override val level: Int = Connection.TRANSACTION_SERIALIZABLE
}
//The following case class is commented out because it does not work for most drivers.  I
//just left it here to document that I thought about it.
//case object TRANSACTION_NONE extends IsolationLevel {
//  override val level: Int = Connection.TRANSACTION_NONE
//}

object DefaultTransactionPrefs {
  implicit val transPropagation = new TransactionPropagation with Required
  implicit val transIsolationLevel = TRANSACTION_READ_COMMITTED
}
