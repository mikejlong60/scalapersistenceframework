package org.scalapersistenceframework.hadoop.util
/*------------------------------------------------------------------------------*\
**   Copyright 2008 Jonhnny Weslley                                             **
**                                                                              **
**   Licensed under the Apache License, Version 2.0 (the "License");            **
**   you may not use this file except in compliance with the License.           **
**   You may obtain a copy of the License at                                    **
**                                                                              **
**       http://www.apache.org/licenses/LICENSE-2.0                             **
**                                                                              **
**   Unless required by applicable law or agreed to in writing, software        **
**   distributed under the License is distributed on an "AS IS" BASIS,          **
**   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   **
**   See the License for the specific language governing permissions and        **
**   limitations under the License.                                             **
\*------------------------------------------------------------------------------*/

import org.apache.hadoop.fs.Path
import org.apache.hadoop.io.{ BooleanWritable, IntWritable, LongWritable, FloatWritable, Text, UTF8 }

/**
 * I got this from Jonhnny Weslley (http://blog.jonhnnyweslley.net/2008/05/shadoop.html)
 * and added the implicit conversions for java.lang.Iterable.
 *
 * @author Johnny Weslley, Mike Long
 */
object SHadoop {

  implicit def writable2boolean(value: BooleanWritable) = value.get
  implicit def boolean2writable(value: Boolean) = new BooleanWritable(value)

  implicit def writable2int(value: IntWritable) = value.get
  implicit def int2writable(value: Int) = new IntWritable(value)

  implicit def writable2long(value: LongWritable) = value.get
  implicit def long2writable(value: Long) = new LongWritable(value)

  implicit def writable2float(value: FloatWritable) = value.get
  implicit def float2writable(value: Float) = new FloatWritable(value)

  implicit def text2string(value: Text) = value.toString
  implicit def string2text(value: String) = new Text(value)

  implicit def uft82string(value: UTF8) = value.toString
  implicit def string2utf8(value: String) = new UTF8(value)

  implicit def path2string(value: Path) = value.toString
  implicit def string2path(value: String) = new Path(value)

  implicit def javaIterator2Iterator[A](value: java.util.Iterator[A]) = new Iterator[A] {
    def hasNext = value.hasNext
    def next = value.next
  }

  /**
   * Added by Mike Long to deal with implicit conversion from java.lang.Iterable
   */
  implicit def javaIterable2Iterator[A](value: java.lang.Iterable[A]) = {
    val result: java.util.Iterator[A] = value.iterator
    new Iterator[A] {
      def hasNext = result.hasNext
      def next = result.next
    }
  }

  implicit def javaIterator2BooleanIterator(value: java.util.Iterator[BooleanWritable]) = new Iterator[Boolean] {
    def hasNext = value.hasNext
    def next = value.next.get
  }

  /**
   * Added by Mike Long to deal with implicit conversion from java.lang.Iterable
   */
  implicit def javaIterable2BooleanIterator(value: java.lang.Iterable[BooleanWritable]) = {
    val result = value.iterator
    new Iterator[Boolean] {
      def hasNext = result.hasNext
      def next = result.next.get
    }
  }

  implicit def javaIterator2IntIterator(value: java.util.Iterator[IntWritable]) = new Iterator[Int] {
    def hasNext = value.hasNext
    def next = value.next.get
  }

  /**
   * Added by Mike Long to deal with implicit conversion from java.lang.Iterable
   */
  implicit def javaIterable2IntIterator(value: java.lang.Iterable[IntWritable]) = {
    val result = value.iterator
    new Iterator[Int] {
      def hasNext = result.hasNext
      def next = result.next.get
    }
  }

  implicit def javaIterator2LongIterator(value: java.util.Iterator[LongWritable]) = new Iterator[Long] {
    def hasNext = value.hasNext
    def next = value.next.get
  }

  /**
   * Added by Mike Long to deal with implicit conversion from java.lang.Iterable
   */
  implicit def javaIterable2LongIterator(value: java.lang.Iterable[LongWritable]) = {
    val result = value.iterator
    new Iterator[Long] {
      def hasNext = result.hasNext
      def next = result.next.get
    }
  }

  implicit def javaIterator2FloatIterator(value: java.util.Iterator[FloatWritable]) = new Iterator[Float] {
    def hasNext = value.hasNext
    def next = value.next.get
  }

  /**
   * Added by Mike Long to deal with implicit conversion from java.lang.Iterable
   */
  implicit def javaIterable2FloatIterator(value: java.lang.Iterable[FloatWritable]) = {
    val result = value.iterator
    new Iterator[Float] {
      def hasNext = result.hasNext
      def next = result.next.get
    }
  }

  implicit def javaIterator2TextIterator(value: java.util.Iterator[Text]) = new Iterator[String] {
    def hasNext = value.hasNext
    def next = value.next.toString
  }

  /**
   * Added by Mike Long to deal with implicit conversion from java.lang.Iterable
   */
  implicit def javaIterable2TextIterator(value: java.lang.Iterable[Text]) = {
    val result = value.iterator
    new Iterator[String] {
      def hasNext = result.hasNext
      def next = result.next.toString
    }
  }

  implicit def javaIterator2UTF8Iterator(value: java.util.Iterator[UTF8]) = new Iterator[String] {
    def hasNext = value.hasNext
    def next = value.next.toString
  }

  /**
   * Added by Mike Long to deal with implicit conversion from java.lang.Iterable
   */
  implicit def javaIterable2UTF8Iterator(value: java.lang.Iterable[UTF8]) = {
    val result = value.iterator
    new Iterator[String] {
      def hasNext = result.hasNext
      def next = result.next.toString
    }
  }
}
