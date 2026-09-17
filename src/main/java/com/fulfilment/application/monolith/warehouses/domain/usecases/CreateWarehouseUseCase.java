package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreateWarehouseUseCase
        implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(
          WarehouseStore warehouseStore,
          LocationResolver locationResolver) {

    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {

    validateBasicFields(warehouse);

    if (warehouseStore.findByBusinessUnitCode(
            warehouse.businessUnitCode)
            != null) {

      throw new IllegalArgumentException(
              "Warehouse with business unit code "
                      + warehouse.businessUnitCode
                      + " already exists.");
    }

    Location location =
            locationResolver.resolveByIdentifier(
                    warehouse.location);

    if (location == null) {
      throw new IllegalArgumentException(
              "Location "
                      + warehouse.location
                      + " does not exist.");
    }

    var warehousesAtLocation =
            warehouseStore.findActiveByLocation(
                    warehouse.location);

    if (warehousesAtLocation.size()
            >= location.maxNumberOfWarehouses) {

      throw new IllegalArgumentException(
              "Maximum number of warehouses has been reached at location "
                      + warehouse.location
                      + ".");
    }

    int usedCapacity =
            warehousesAtLocation.stream()
                    .map(w -> w.capacity)
                    .mapToInt(Integer::intValue)
                    .sum();

    if (usedCapacity + warehouse.capacity
            > location.maxCapacity) {

      throw new IllegalArgumentException(
              "Warehouse capacity exceeds the maximum capacity of location "
                      + warehouse.location
                      + ".");
    }

    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException(
              "Warehouse capacity must be at least the warehouse stock.");
    }

    warehouse.createdAt =
            java.time.LocalDateTime.now();

    warehouse.archivedAt = null;

    warehouseStore.create(warehouse);
  }

  private void validateBasicFields(
          Warehouse warehouse) {

    if (warehouse == null) {
      throw new IllegalArgumentException(
              "Warehouse must be provided.");
    }

    if (isBlank(warehouse.businessUnitCode)) {
      throw new IllegalArgumentException(
              "Business unit code must be provided.");
    }

    if (isBlank(warehouse.location)) {
      throw new IllegalArgumentException(
              "Warehouse location must be provided.");
    }

    if (warehouse.capacity == null
            || warehouse.capacity < 0) {

      throw new IllegalArgumentException(
              "Warehouse capacity must be zero or greater.");
    }

    if (warehouse.stock == null
            || warehouse.stock < 0) {

      throw new IllegalArgumentException(
              "Warehouse stock must be zero or greater.");
    }
  }

  private boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
