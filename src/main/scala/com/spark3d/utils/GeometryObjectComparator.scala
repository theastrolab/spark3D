/*
 * Copyright 2018 AstroLab Software
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

import com.astrolabsoftware.spark3d.geometryObjects.Point3D
import com.astrolabsoftware.spark3d.geometryObjects.Shape3D.Shape3D

class GeometryObjectComparator[A <: Shape3D](val queryObjectCenter: Point3D) extends Ordering[A] {

  override def compare(x: A, y: A): Int = {
    val dist1 = x.center.distanceTo(queryObjectCenter)
    val dist2 = y.center.distanceTo(queryObjectCenter)

    if (dist1 <= dist2) {
      return -1
    } else {
      return 1
    }
  }
}
