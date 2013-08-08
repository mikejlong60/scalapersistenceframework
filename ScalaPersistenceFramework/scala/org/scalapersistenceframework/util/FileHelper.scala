package org.scalapersistenceframework.util

import java.io._

/**
 * This class decorates the java.io.File class with a recursive delete method, a method that
 * applies a given function to each line, and a method that adds a single line of text 
 * to the given file.
 * 
 * To use it see its accompanying test.  It came from Zemian Deng's work at:
 * 
 * http://www.jroller.com/thebugslayer/entry/improving_java_io_file_with
 *
 * @author Zemian Deng, Mike Long
 */
class FileHelper(file: File) {

  /**
   * Write a line of text to the file and close the file.
   */
  def write(text: String): Unit = {
    val fw = new FileWriter(file)
    try { fw.write(text) }
    finally { fw.close }
  }

  /**
   * Apply the given function to each line of the file.
   */
  def foreachLine(proc: String => Unit): Unit = {
    val br = new BufferedReader(new FileReader(file))
    try { while (br.ready) proc(br.readLine) }
    finally { br.close }
  }

  /**
   * Recursively deletes a directory and its contents using implicit conversions.
   * Note usage of tail recursion, pretty manly;)
   */
  def deleteAll: Unit = {
    def deleteFile(dfile: File): Unit = {
      if (dfile.isDirectory) {
        val files = dfile.listFiles
        if (files != null) files.foreach { f => deleteFile(f) }
      }
      dfile.delete
    }
    deleteFile(file)
  }
}

object FileHelper {
  implicit def file2helper(file: File) = new FileHelper(file)
}

