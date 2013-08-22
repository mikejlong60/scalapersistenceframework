package org.scalapersistenceframework.jdbcmetadata

import java.util.logging.Logger
/**
 * This class represents a column in the database.
 *
 * @author mjlong
 *
 */
class Column(val sqlType: Int, val sqlTypeName: String, val sqlName: String, val size: Int, val decimalDigits: Int, val isPk: Boolean, val isNullable: Boolean) extends NameConverter with Equals {

  val logger = Logger.getLogger(this.getClass().getName())

  override val scalaName = capitalize(dbNameToVariableName(sqlName))
  override val variableName = decapitalize(scalaName)
  private val scalaType = this.sqlTypeName match {
    //Integer type defaults
    case "serial" if (!isNullable) => "java.lang.Long"
    case "int4" if (!isNullable) => "java.lang.Long"
    case "int4" if (isNullable) => "Option[Long]"

    //Numeric type defaults
    case "numeric" if (size > 1 && decimalDigits == 0 && isPk) => "java.lang.Long"
    case "numeric" if (size > 1 && decimalDigits == 0 && isNullable) => "Option[Long]"
    case "numeric" if (size > 1 && decimalDigits == 0 && !isNullable) => "Long"
    case "numeric" if (size > 1 && decimalDigits > 0 && isNullable) =>  "java.lang.Double"

    //Boolean type defaults
    case "numeric" if (size == 1 && !isNullable) => "Boolean"
    case "numeric" if (size == 1 && isNullable) =>  "Option[Boolean]"
    
    //Float type defaults
    case "numeric" if (decimalDigits > 0 && isPk) => "java.lang.Double"
    case "numeric" if (decimalDigits > 0 && isNullable) => "Option[Double]"
    case "numeric" if (decimalDigits > 0 && !isNullable) => "Double"

    //varchar type defaults
    case "varchar" if (!isNullable) => "String"
    case "varchar" if (isNullable) => "Option[String]"

    //timestamp type defaults
    case "timestamp" if (!isNullable) => "java.sql.Timestamp"
    case "timestamp" if (isNullable) => "Option[java.sql.Timestamp]"
   
    //date type defaults
    case "date" if (!isNullable) => "java.sql.Date"
    case "date" if (isNullable) => "Option[java.sql.Date]"
    case default => throw new IllegalArgumentException("Unknown type[" + default + "]")
  }

  private val scalaTypeMap = Map(
    "java.lang.Long" -> List("nonNullableLong", "random.nextLong"),
    "Option[Long]" -> List("nullableLong", "Some(random.nextLong)"),
    "Long" -> List("nonNullableLong", "random.nextLong"),
    "java.lang.Double" -> List("nonNullableDouble", "random.nextDouble"),
    "Boolean" -> List("nonNullableBoolean", "false"),
    "Option[Boolean]" -> List("nullableBoolean", "Some(false)"),
    "Option[Double]" -> List("nullableDouble", "Some(random.nextDouble)"),
    "Double" -> List("nonNullableDouble","random.nextDouble"),
    "String" -> List("nonNullableString","java.lang.Long.toString(random.nextInt)"),
    "Option[String]" -> List("nullableString","Some(java.lang.Long.toString(random.nextInt))"),
    "java.sql.Timestamp" -> List("nonNullableTimestamp","new java.sql.Timestamp(System.currentTimeMillis)"),
    "Option[java.sql.Timestamp]" -> List("nullableTimestamp", "Some(new java.sql.Timestamp(System.currentTimeMillis))"),
    "java.sql.Date" -> List("nonNullableDate","new java.sql.Date(System.currentTimeMillis)"),
    "Option[java.sql.Date]" -> List("nullableDate","Some(new java.sql.Date(System.currentTimeMillis))")
  )
  

  //These getters make the fields available to Velocity Templates. 
  //Note, isPk and isNullable are already available because Velocity can find them from the byte code for the constructor.
  def getSqlType = { sqlType }
  def getSqlTypeName = { sqlTypeName }
  def getSqlName = { sqlName }
  def getSize = { size }
  def getDecimalDigits = { decimalDigits }
  def getScalaType = {scalaType}
  def getScalaSelectMapper = {scalaTypeMap(scalaType).head}
  def getScalaConstructorMapper = {if (sqlTypeName == "serial") "null" else scalaTypeMap(scalaType).tail.last}
  ////////////////////////////
  override def canEqual(other: Any) = other.isInstanceOf[Column]
  override def hashCode = {
    val prime = 41
    prime * (prime + Column.super.hashCode + sqlName.hashCode)
  }
  override def equals(other: Any) = other match {
    case that: Column => Column.super.equals(that) && that.canEqual(Column.this) && this.sqlName == that.sqlName
    case _ => false
  }
}
