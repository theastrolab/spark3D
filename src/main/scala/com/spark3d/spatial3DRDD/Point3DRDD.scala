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
package com.spark3d.spatial3DRDD

import com.spark3d.geometryObjects._
import com.spark3d.spatial3DRDD.Loader._

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col
import org.apache.spark.rdd.RDD


class Point3DRDD(rdd : RDD[Point3D], override val isSpherical: Boolean) extends Shape3DRDD[Point3D] {

  /**
    * Construct a Point3DRDD from CSV data.
    * {{{
    *   val fn = "src/test/resources/astro_obs.csv"
    *   val p3DRDD = new Point3DRDD(spark, fn, "RA,Dec,Z_COSMO", true)
    * }}}
    *
    * @param spark : (SparkSession)
    *   The spark session
    * @param filename : (String)
    *   File name where the data is stored
    * @param colnames : (String)
    * Comma-separated names of columns. Example: "RA,Dec,Z_COSMO".
    * @param isSpherical : (Boolean)
    *   If true, it assumes that the coordinates of the Point3D are (r, theta, phi).
    *   Otherwise, it assumes cartesian coordinates (x, y, z).
    *
    */
  def this(spark : SparkSession, filename : String, colnames : String, isSpherical: Boolean) {
    this(Point3DRDDFromCSV(spark, filename, colnames, isSpherical), isSpherical)
  }

  /**
    * Class to make a Point3D RDD from FITS data.
    * {{{
    *   val fn = "src/test/resources/astro_obs.fits"
    *   val p3DRDD = new Point3DRDD(spark, fn, 1, "RA,Dec,Z_COSMO", true)
    * }}}
    *
    * @param spark : (SparkSession)
    *   The spark session
    * @param filename : (String)
    *   File name where the data is stored
    * @param hdu : (Int)
    *   HDU to load.
    * @param colnames : (String)
    * Comma-separated names of columns. Example: "RA,Dec,Z_COSMO".
    * @param isSpherical : (Boolean)
    *   If true, it assumes that the coordinates of the Point3D are (r, theta, phi).
    *   Otherwise, it assumes cartesian coordinates (x, y, z). Default is false.
    *
    */
  def this(spark : SparkSession, filename : String, hdu : Int, colnames : String, isSpherical: Boolean) {
    this(Point3DRDDFromFITS(spark, filename, hdu, colnames, isSpherical), isSpherical)
  }

  // Raw partitioned RDD
  override val rawRDD = rdd
}

/**
  * Handle point3DRDD.
  */
object Point3DRDD {
  def apply(rdd : RDD[Point3D], isSpherical: Boolean): Point3DRDD = {
    new Point3DRDD(rdd, isSpherical)
  }
}
