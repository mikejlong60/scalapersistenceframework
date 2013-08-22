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
package org.scalapersistenceframework.jdbcmetadata.velocity

import java.io.StringWriter
import org.apache.velocity.app.Velocity
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.VelocityEngine
import java.io.File
import java.io.BufferedWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.FileWriter
import java.io.FileReader
import java.net.URL
import org.scalapersistenceframework.DefaultTransactionPrefs.transIsolationLevel
import org.scalapersistenceframework.DefaultTransactionPrefs.transPropagation
import org.scalapersistenceframework.Transaction
import org.scalapersistenceframework.jdbcmetadata.Schema
import org.apache.velocity.app.FieldMethodizer
import org.scalapersistenceframework.DaoProperties
import java.util.logging.Logger

class VelocityTemplateFileGenerator {
  private val logger = Logger.getLogger(this.getClass().getName())

  private val properties = new DaoProperties("postgres.jdbc")
  private val dir = properties.getProperty("template.directory")
  private val schemaName = properties.getProperty("schema.name")

  def generate() {
    transPropagation enclose (None,
      {
        val conn = Transaction.getInstance.getConnectionForTransaction
        val schema = new Schema(conn, schemaName.get)

        for (table <- schema.tables) {

          /* lets make a Context and put data into it */
          val context = new VelocityContext
          context.put("table", table)

          val velocityEngine = new VelocityEngine();
          velocityEngine.init
          val files = recursiveListFiles(new File(dir.get))
          files map { file =>
            val template = new URL("file://" + file.getAbsolutePath())
            val templateReader = new BufferedReader(new InputStreamReader(template.openStream()))
            val writer = new BufferedWriter(new FileWriter(table.scalaName + file.getName))
            velocityEngine.evaluate(context, writer, "", templateReader)
            writer.flush()
            writer.close()
            templateReader.close()
          }
        }
      })
  }

  private def recursiveListFiles(f: File): Array[File] = {
    val files = f.listFiles
    files ++ files.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

}
