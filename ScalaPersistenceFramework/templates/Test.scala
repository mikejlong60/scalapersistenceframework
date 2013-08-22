/**
 * *****************************************************************************
 * Copyright 2013 Functionicity LLC, All Rights Reserved
 *
 * ****************************************************************************
 */

import java.util.logging.Logger
import org.scalapersistenceframework.PersistentOperationType
import org.scalapersistenceframework.Required
import org.scalapersistenceframework.TRANSACTION_READ_COMMITTED
import org.scalapersistenceframework.TRANSACTION_REPEATABLE_READ
import org.scalapersistenceframework.TransactionPropagation
import org.scalapersistenceframework.service.DaoService
import org.scalatest.BeforeAndAfterEach
import org.scalatest.FunSuite
import org.scalapersistenceframework.DataSourceConfigurer
import org.scalapersistenceframework.Transaction
import scala.util.Random

#set($primaryKeysSize = $table.primaryKeysSize) 
#set($columnsSize = $table.columnsSize) 
 
/**
 * This class is a scala test class around the transactional service
 * layer for a database table.
 *
 */

class ${table.scalaName}Test extends FunSuite with DataSourceConfigurer with BeforeAndAfterEach {

  override val logger = Logger.getLogger(this.getClass().getName())
  val service = new DaoService(new ${table.scalaName}Dao)
  val random = new Random(System.nanoTime)

  override def beforeEach {
    super.configureJdbc
  }

  override def afterEach {
    super.cleanupTransactions
  }
    
  /**
   * The following "implicit" mechanism allows you to express transactional behavior at three levels:
   *
   * 1) Adopting the default transactional object from the DaoService by placing the
   * following import in your code after the class keyword:
   * "import org.scalapersistenceframework.service.DaoService.DefaultTransactionPrefs._"
   *
   * 2) You can specify a class overload for transactional behavior by creating an implicit value specifying the transactional
   * behavior for this class like this:
   *   implicit val trans2 = new TransactionPropagation with Required
   *
   * 3) You can pass the transactional behavior at the method level explicitly as a parameter to each method call like this:
   *     service.insert(result){ trans2 } or service.insert(result)(trans2)
   *
   */
  //  import org.scalapersistenceframework.DefaultTransactionPrefs._
  //or
  object LocalTransactionPrefs {
    implicit val transPropagation = new TransactionPropagation with Required
    implicit val transIsolationLevel = TRANSACTION_READ_COMMITTED
  }
  import LocalTransactionPrefs._
  //or passed at the method level
  //  implicit val transIsolationLevel = TRANSACTION_READ_COMMITTED



  test("Test insert, get, findall, update and delete API of DAO service") ({

        //Test insert method.
        val newEntity = 
   new ${table.scalaName}(
    #foreach($column in $table.columns)
	    ${column.scalaConstructorMapper}, 
    #end
	false 
	)
            
    val pk = service.insert(newEntity)

	val result = service.findAll();
    assert(result.size > 0, "Should have found at least one Address object.")
	var foundNewEntity = false
	var gotEntity:Option[${table.scalaName}] = None
	result map { entity =>
	    #foreach($column in $table.primaryKeys)
		    assert(entity.${column.variableName} != null, "The ${table.variableName} primary key was null or empty.") 
	    #end

		// Make sure it got inserted.
		if (entity.id == pk) {
			foundNewEntity = true
			gotEntity = Some(entity)
		}
	}
	assert(foundNewEntity, "The ${table.scalaName} was not found in the list.")

        //Make sure it got inserted properly.
	#foreach($column in $table.nonPrimaryKeyColumns)
    	expectResult(gotEntity.get.${column.variableName}) {newEntity.${column.variableName}}	
    #end

        //Make sure update method works

    #foreach($column in $table.nonPrimaryKeyColumns)
    	val update${column.ScalaName} = ${column.scalaConstructorMapper}
		gotEntity.get.${column.variableName} = update${column.ScalaName}	
	#end		
   	service.update(gotEntity.get);
    val updatedEntity = service.findByPk(
    		List(
    				#foreach($column in $table.primaryKeys)
    					gotEntity.get.${column.variableName}
						#if (($velocityCount) != $primaryKeysSize)
						, 
						#end     				  
					#end
    				))        

	#foreach($column in $table.nonPrimaryKeyColumns)
    	expectResult(updatedEntity.get.${column.variableName}) {update${column.scalaName}}	
    #end

    //Make sure delete method works
    service.delete(updatedEntity.get);
    gotEntity = service.findByPk(
    		List(
    				#foreach($column in $table.primaryKeys)
    					updatedEntity.get.${column.variableName}
						#if (($velocityCount) != $primaryKeysSize)
						, 
						#end     				  
					#end
    				))       
    expectResult(None){gotEntity}
  })


  test("Test Save API of DAO service") ({

        //Test insert method.
        val newEntity = 
   new ${table.scalaName}(
    #foreach($column in $table.columns)
	    ${column.scalaConstructorMapper}, 
    #end
	false 
	)
            
    newEntity.persistentOperationType = PersistentOperationType.UPDATE
    service.save(Set(newEntity))

	val result = service.findAll();
    assert(result.size > 0, "Should have found at least one Address object.")
	var foundNewEntity = false
	var gotEntity:Option[${table.scalaName}] = None
	result map { entity =>
	    #foreach($column in $table.primaryKeys)
		    assert(entity.${column.variableName} != null, "The ${table.variableName} primary key was null or empty.") 
	    #end

		// Make sure it got inserted.
      if (entity.createdTs == newEntity.createdTs) {
			foundNewEntity = true
			gotEntity = Some(entity)
		}
	}
	assert(foundNewEntity, "The ${table.scalaName} was not found in the list.")

        //Make sure it got inserted properly.
	#foreach($column in $table.nonPrimaryKeyColumns)
    	expectResult(gotEntity.get.${column.variableName}) {newEntity.${column.variableName}}	
    #end

        //Make sure save can update

    #foreach($column in $table.nonPrimaryKeyColumns)
    	val update${column.ScalaName} = ${column.scalaConstructorMapper}
		gotEntity.get.${column.variableName} = update${column.ScalaName}	
	#end		
    gotEntity.get.persistentOperationType = PersistentOperationType.UPDATE
    service.save(Set(gotEntity.get));
    val updatedEntity = service.findByPk(
    		List(
    				#foreach($column in $table.primaryKeys)
    					gotEntity.get.${column.variableName}
						#if (($velocityCount) != $primaryKeysSize)
						, 
						#end     				  
					#end
    				))        

	#foreach($column in $table.nonPrimaryKeyColumns)
    	expectResult(updatedEntity.get.${column.variableName}) {update${column.scalaName}}	
    #end

    //Make sure save can delete
    gotEntity.get.persistentOperationType = PersistentOperationType.DELETE
    service.save(Set(gotEntity.get))
    gotEntity = service.findByPk(
    		List(
    				#foreach($column in $table.primaryKeys)
    					updatedEntity.get.${column.variableName}
						#if (($velocityCount) != $primaryKeysSize)
						, 
						#end     				  
					#end
    				))       
    expectResult(None){gotEntity}
  })


}
