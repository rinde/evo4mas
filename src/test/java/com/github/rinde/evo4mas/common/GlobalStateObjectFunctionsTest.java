/*
 * Copyright (C) 2011-2016 Rinde van Lon, iMinds-DistriNet, KU Leuven
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.rinde.evo4mas.common;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.Test;

import com.github.rinde.rinsim.central.GlobalStateObject;
import com.github.rinde.rinsim.central.GlobalStateObjectBuilder;
import com.github.rinde.rinsim.core.model.pdp.Parcel;
import com.github.rinde.rinsim.core.model.pdp.VehicleDTO;
import com.github.rinde.rinsim.geom.Point;
import com.github.rinde.rinsim.testutil.TestUtil;
import com.github.rinde.rinsim.util.TimeWindow;
import com.google.common.collect.ImmutableList;

import it.unimi.dsi.fastutil.longs.LongList;

/**
 *
 * @author Rinde van Lon
 */
public class GlobalStateObjectFunctionsTest {

  @Test
  public void testArrivalTimes() {
    TestUtil.testPrivateConstructor(GlobalStateObjectFunctions.class);

    final Parcel a = Parcel.builder(new Point(0, 0), new Point(1, 0))
      .serviceDuration(10)
      .build();
    final Parcel b = Parcel.builder(new Point(1, 1), new Point(0, 1))
      .pickupTimeWindow(TimeWindow.create(1800, 1800))
      .serviceDuration(0)
      .build();

    final GlobalStateObject gso = GlobalStateObjectBuilder.globalBuilder()
      .addAvailableParcel(a)
      .addAvailableParcel(b)
      .addVehicle(GlobalStateObjectBuilder.vehicleBuilder()
        .setVehicleDTO(VehicleDTO.builder()
          .availabilityTimeWindow(TimeWindow.create(0, 1900))
          .speed(10000)
          .build())
        .setLocation(new Point(-1, 0))
        .build())
      .build();

    final List<Long> earliest =
      GlobalStateObjectFunctions.computeEarliestArrivalTimes(gso,
        ImmutableList.of(a, a, b, b));

    final List<Long> latest =
      GlobalStateObjectFunctions.computeLatestArrivalTimes(gso,
        ImmutableList.of(a, a, b, b), (LongList) earliest);

    assertThat(earliest).containsExactly(360L, 730L, 1800L, 2160L, 2520L)
      .inOrder();
    assertThat(latest).containsExactly(1060L, 1430L, 1800L, 2160L, 2520L)
      .inOrder();

  }

  @Test
  public void testArrivalTimes2() {
    TestUtil.testPrivateConstructor(GlobalStateObjectFunctions.class);

    final Parcel a = Parcel.builder(new Point(0, 0), new Point(1, 0))
      .serviceDuration(10)
      .build();
    final Parcel b = Parcel.builder(new Point(1, 1), new Point(0, 1))
      .pickupTimeWindow(TimeWindow.create(1800, 1800))
      .serviceDuration(0)
      .build();

    final GlobalStateObject gso = GlobalStateObjectBuilder.globalBuilder()
      .addAvailableParcel(a)
      .addAvailableParcel(b)
      .addVehicle(GlobalStateObjectBuilder.vehicleBuilder()
        .setVehicleDTO(VehicleDTO.builder()
          .availabilityTimeWindow(TimeWindow.create(0, 1900))
          .speed(10000)
          .build())
        .setLocation(new Point(0, 0))
        .setRemainingServiceTime(10)
        .build())
      .build();

    final List<Long> earliest =
      GlobalStateObjectFunctions.computeEarliestArrivalTimes(gso,
        ImmutableList.of(a, a, b, b));

    final List<Long> latest =
      GlobalStateObjectFunctions.computeLatestArrivalTimes(gso,
        ImmutableList.of(a, a, b, b), (LongList) earliest);

    assertThat(earliest).containsExactly(0L, 370L, 1800L, 2160L, 2520L)
      .inOrder();
    assertThat(latest).containsExactly(1060L, 1430L, 1800L, 2160L, 2520L)
      .inOrder();

  }

}
