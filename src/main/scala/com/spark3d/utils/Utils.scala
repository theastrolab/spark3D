/*
 * Copyright 2018 Julien Peloton
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.astrolabsoftware.spark3d.utils

import com.astrolabsoftware.spark3d.geometryObjects.Shape3D.Shape3D
import com.astrolabsoftware.spark3d.geometryObjects._
import com.google.common.collect.{Ordering => GuavaOrdering}

import org.apache.spark.rdd.RDD

import scala.reflect.ClassTag
import scala.collection.JavaConverters._

object Utils {

  /**
    * Convert a Point3D with cartesian coordinates in a
    * Point3D with spherical coordinates.
    *
    * @param p : (Point3D)
    *   Input Point3D with cartesian coordinates.
    * @return (Point3D) The same point but with spherical coordinates.
    */
  def cartesianToSpherical(p : Point3D) : Point3D = {
    if (p.isSpherical) {
      throw new AssertionError("""
        Cannot convert your point to spherical coordinates because
        it is already in spherical coordinates.""")
    }

    val r = math.sqrt(p.x*p.x + p.y*p.y + p.z*p.z)
    val theta = math.acos(p.z / r)
    val phi = math.atan(p.y / p.x)

    // Return the new point in spherical coordinates
    new Point3D(r, theta, phi, true)
  }

  /**
    * Convert a Point3D with spherical coordinates in a
    * Point3D with cartesian coordinates.
    *
    * @param p : (Point3D)
    *   Input Point3D with spherical coordinates.
    * @return (Point3D) The same point but with cartesian coordinates.
    */
  def sphericalToCartesian(p : Point3D) : Point3D = {
    if (!p.isSpherical) {
      throw new AssertionError("""
        Cannot convert your point to cartesian coordinates because
        it is already in cartesian coordinates.""")
    }

    val x = p.x * math.sin(p.y) * math.cos(p.z)
    val y = p.x * math.sin(p.y) * math.sin(p.z)
    val z = p.x * math.cos(p.y)

    // Return the new point in spherical coordinates
    new Point3D(x, y, z, false)
  }

  /**
    * Convert declination into theta
    *
    * @param dec : (Double)
    *   declination coordinate in degree
    * @param inputInRadian : (Boolean)
    *   If true, assume the input is in radian. Otherwise make the conversion
    *   deg2rad. Default is false.
    * @return (Double) theta coordinate in radian
    */
  def dec2theta(dec : Double, inputInRadian : Boolean = false) : Double = {
    if (!inputInRadian) {
      math.Pi / 2.0 - math.Pi / 180.0 * dec
    } else {
      math.Pi / 2.0 - dec
    }

  }

  /**
    * Convert right ascension into phi
    *
    * @param ra : (Double)
    *   RA coordinate in degree
    * @param inputInRadian : (Boolean)
    *   If true, assume the input is in radian. Otherwise make the conversion
    *   deg2rad. Default is false.
    * @return (Double) phi coordinate in radian
    *
    */
  def ra2phi(ra : Double, inputInRadian : Boolean = false) : Double = {
    if (!inputInRadian) {
      math.Pi / 180.0 * ra
    } else {
      ra
    }
  }

  def takeOrdered[T <: Shape3D: ClassTag](rdd: RDD[T], num: Int, queryObject: T, unique: Boolean = false)(ord: Ordering[T]): Array[T] = {

    if (unique) {
      if (num == 0) {
        Array.empty
      } else {
        val mapRDDs = rdd.mapPartitions { items =>
          val queue = new BoundedUniquePriorityQueue[T](num)(ord.reverse)
          queue ++= takeOrdered(items, num)(ord)
          Iterator.single(queue)
        }
        if (mapRDDs.partitions.length == 0) {
          return Array.empty
        } else {
          return mapRDDs.reduce { (queue1, queue2) =>
            queue1 ++= queue2
            queue1
          }.toArray.sorted(ord)
        }
      }

    }

    return rdd.takeOrdered(num)(new GeometryObjectComparator[T](queryObject.center))
  }

  private def takeOrdered[T](input: Iterator[T], num: Int)(implicit ord: Ordering[T]): Iterator[T] = {
    val ordering = new GuavaOrdering[T] {
      override def compare(l: T, r: T): Int = ord.compare(l, r)
    }
    ordering.leastOf(input.asJava, num).iterator.asScala
  }

}
