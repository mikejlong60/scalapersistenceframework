/**
 * *****************************************************************************
 * Copyright 2013 Functionicity LLC, All Rights Reserved
 *
 * ****************************************************************************
 */
import java.sql.ResultSet

import org.scalapersistenceframework.GridCapableEntity
import org.scalapersistenceframework.dao.CrudDao
import org.scalapersistenceframework.dao.DefaultBooleanHandler.booleanHandler
import org.scalapersistenceframework.dao.RDBMSBooleanHandler

/**
 * This class is a Value Object that provides a one-to-one mapping
 * to the fields in the database table of the same name.
 *
 */
 
#set($primaryKeysSize = $table.primaryKeysSize) 
#set($columnsSize = $table.columnsSize) 

class ${table.scalaName}(
            #foreach($column in $table.columns)
				var ${column.variableName}:${column.scalaType} , 
			#end
	override val persistent: Boolean) extends GridCapableEntity with Equals {

	override def canEqual(other: Any) = other.isInstanceOf[$table.scalaName]
	override def hashCode = {
		val prime = 41
		#foreach ($column in $table.primaryKeys)
			prime * (prime + ${table.scalaName}.super.hashCode + ${column.variableName}.hashCode) #if (($velocityCount) != $primaryKeysSize) + $column.variableName #end
		#end
	}


  override def equals(other: Any) = other match {
	#foreach ($column in $table.primaryKeys)	
		case that: ${table.scalaName} => ${table.scalaName}.super.equals(that) && that.canEqual(${table.scalaName}.this) && this.${column.variableName} == that.${column.variableName} #if (($velocityCount) != $primaryKeysSize) && this.${column.variableName} == that.${column.variableName} #end
		#end
    case _ => false
  }

}

class ${table.scalaName}Dao(override val connectionName: Option[String]) extends CrudDao[${table.scalaName}] {
  def this() {
    this(None)
  }

  def validatePkForUpdate(vo: ${table.scalaName}): Unit = {
    	#foreach($column in $table.primaryKeys)
    		require(vo.${column.variableName} != null, "The ${table.scalaName} is not created yet, the ${column.scalaName} cannot be null.")
	   #end
  }

  def validatePkForInsert(vo: ${table.scalaName}): Unit = {
    	#foreach($column in $table.primaryKeys)
    		require(vo.${column.variableName} == null, "The ${table.scalaName} is already created, the ${column.scalaName} is not null..")
        #end
  }

  /**
   * Map the current row of the given ResultSet to a value object.
   *
   * @param resultSet
   *            The ResultSet of which the current row is to be mapped to a
   *            value object.
   * @return The mapped value object from the current row of the given
   *         ResultSet.
   * @throws SQLException
   *             If something fails at database level.
   */
  override def mapForSelect(resultSet: ResultSet): ${table.scalaName} = {
    new ${table.scalaName}(
    #foreach($column in $table.columns)
	    ${column.scalaSelectMapper}(resultSet, "${column.sqlName}"), 
    #end
	nonNullableBoolean(resultSet, "persistent") 
  	)
  }

  override def mapForUpdate(vo: ${table.scalaName})(implicit booleanHandler: RDBMSBooleanHandler): List[Any] = {
    List(#foreach($column in $table.nonPrimaryKeyColumns) 
    	#if (${column.nullable}) 
    		#if ( $column.scalaType == "Option[Boolean]") 
    			booleanHandler.booleanOption2Sql(vo.${column.variableName}) 
    		#else 
    			vo.${column.variableName}.orNull 
    		#end
    	#else  
    		#if ( $column.scalaType == "Boolean") 
    			booleanHandler.boolean2Sql(vo.${column.variableName}) 
    		#else 
    			vo.${column.variableName} 
    		#end
    	#end 
    		, 
    #end
    #foreach($column in $table.primaryKeys) 
		vo.${column.variableName} 
		#if (($velocityCount) != $primaryKeysSize)
		, 
		#end 
    #end 
    )
  }

  override def mapForInsert(vo: ${table.scalaName})(implicit booleanHandler: RDBMSBooleanHandler): List[Any] = {
    List(#foreach($column in $table.nonPrimaryKeyColumns) 
    	#if (${column.nullable}) 
    		#if ( $column.scalaType == "Option[Boolean]") 
    			booleanHandler.booleanOption2Sql(vo.${column.variableName}) 
    		#else 
    			vo.${column.variableName}.orNull 
    		#end
    	#else  
    		#if ( $column.scalaType == "Boolean") 
    			booleanHandler.boolean2Sql(vo.${column.variableName}) 
    		#else 
    			vo.${column.variableName} 
    		#end
    	#end 
    	#if ($velocityCount < $table.nonPrimaryKeysSize)
    		, 
    	#end 
    #end 
    )
  }

  override def mapForDelete(vo: ${table.scalaName}): List[Any] = {
    List(#foreach($column in $table.primaryKeys) vo.${column.variableName} #if (($velocityCount) != $primaryKeysSize), #end #end)
  }

  override def getSQL_FIND_BY_ID(): String = { 
  		"select #set( $columnsSize = $table.columnsSize) #foreach($column in $table.columns) $column.sqlName, #end 1 as PERSISTENT from ${table.schema}.${table.sqlName} where #foreach($column in $table.primaryKeys) $column.sqlName = ? #if (($velocityCount) != $primaryKeysSize) and #end #end "}
  override def getSQL_LIST_ORDER_BY_ID(): String = { 
  		"select #set( $columnsSize = $table.columnsSize) #foreach($column in $table.columns) $column.sqlName, #end 1 as PERSISTENT from  ${table.schema}.${table.sqlName} order by #foreach($column in $table.primaryKeys) $column.sqlName #if (($velocityCount) != $primaryKeysSize), #end #end "
  
  }
 
 override def getSQL_INSERT(): String = { 
  		"insert into ${table.schema}.${table.sqlName} ( " +
  		"	#foreach($column in $table.nonPrimaryKeyColumns) $column.sqlName " +
  		"		#if ($velocityCount < $table.nonPrimaryKeysSize), #end " +
  		"#end " +
  		") " +
  		"values ( " +
  		"#foreach($column in $table.nonPrimaryKeyColumns) ?" +
  		"	#if ($velocityCount < $table.nonPrimaryKeysSize), #end " +
  		"#end " +
  		") "
  }
  override def getSQL_UPDATE(): String = { "update ${table.schema}.${table.sqlName} set #foreach($column in $table.nonPrimaryKeyColumns) $column.sqlName=? #if ($velocityCount < $table.nonPrimaryKeysSize), #end #end where #foreach($column in $table.primaryKeys) $column.sqlName=? #if (($velocityCount) != $primaryKeysSize), #end #end "}
  override def getSQL_DELETE(): String = { "delete from ${table.schema}.${table.sqlName} where #foreach($column in $table.primaryKeys) $column.sqlName=? #if (($velocityCount) != $primaryKeysSize), #end #end "}

}