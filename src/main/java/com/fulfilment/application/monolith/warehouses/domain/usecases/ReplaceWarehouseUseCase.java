package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.stream.*;

@ApplicationScoped
public class ReplaceWarehouseUseCase
        implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public ReplaceWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationResolver locationResolver) {

    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void replace(Warehouse newWarehouse) {

    if (newWarehouse == null
            || isBlank(newWarehouse.businessUnitCode)) {

      throw new IllegalArgumentException(
              "Business unit code must be provided.");
    }

    if (isBlank(newWarehouse.location)) {
      throw new IllegalArgumentException(
              "Warehouse location must be provided.");
    }

    if (newWarehouse.capacity == null
            || newWarehouse.capacity < 0) {

      throw new IllegalArgumentException(
              "Warehouse capacity must be zero or greater.");
    }

    if (newWarehouse.stock == null
            || newWarehouse.stock < 0) {

      throw new IllegalArgumentException(
              "Warehouse stock must be zero or greater.");
    }

    Warehouse current =
            warehouseStore.findByBusinessUnitCode(
                    newWarehouse.businessUnitCode);

    if (current == null) {
      throw new IllegalArgumentException(
              "Warehouse with business unit code "
                      + newWarehouse.businessUnitCode
                      + " does not exist.");
    }

    // Stock must remain the same during replacement.
    if (!java.util.Objects.equals(
            current.stock,
            newWarehouse.stock)) {

      throw new IllegalArgumentException(
              "Replacement warehouse stock must match the current stock.");
    }

    // New warehouse must be capable of storing
    // the existing stock.
    if (newWarehouse.capacity < current.stock) {

      throw new IllegalArgumentException(
              "Replacement warehouse capacity must accommodate the current stock.");
    }

    Location location =
            locationResolver.resolveByIdentifier(
                    newWarehouse.location);

    if (location == null) {

      throw new IllegalArgumentException(
              "Location "
                      + newWarehouse.location
                      + " does not exist.");
    }

    var warehousesAtLocation =
            warehouseStore.findActiveByLocation(
                    newWarehouse.location);

    long activeCount =
            warehousesAtLocation.stream()
                    .filter(
                            w ->
                                    !w.businessUnitCode.equals(
                                            current.businessUnitCode))
                    .count();

    if (activeCount
            >= location.maxNumberOfWarehouses) {

      throw new IllegalArgumentException(
              "Maximum number of warehouses has been reached at location "
                      + newWarehouse.location
                      + ".");
    }

    int usedCapacity =
            warehousesAtLocation.stream()
                    .filter(
                            w ->
                                    !w.businessUnitCode.equals(
                                            current.businessUnitCode))
                    .map(w -> w.capacity)
                    .mapToInt(Integer::intValue)
                    .sum();

    if (usedCapacity + newWarehouse.capacity
            > location.maxCapacity) {

      throw new IllegalArgumentException(
              "Replacement warehouse capacity exceeds the maximum capacity of location "
                      + newWarehouse.location
                      + ".");
    }

    newWarehouse.createdAt =
            java.time.LocalDateTime.now();

    newWarehouse.archivedAt = null;

    warehouseStore.replace(
            current.businessUnitCode,
            newWarehouse);
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}