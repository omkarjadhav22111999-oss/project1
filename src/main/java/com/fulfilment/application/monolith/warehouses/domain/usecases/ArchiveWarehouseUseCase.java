package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase
        implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(
          WarehouseStore warehouseStore) {

    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {

    if (warehouse == null
            || warehouse.id == null) {

      throw new IllegalArgumentException(
              "Warehouse must be an existing warehouse.");
    }

    if (warehouse.archivedAt != null) {
      return;
    }

    warehouse.archivedAt =
            LocalDateTime.now();

    warehouseStore.update(warehouse);
  }
}
