package com.fulfilment.application.monolith.warehouses.domain.usecases;


import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

    @Test
    void archivesExistingWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.id = 1L;

        FakeStore store = new FakeStore();

        new ArchiveWarehouseUseCase(store).archive(warehouse);

        assertNotNull(warehouse.archivedAt);
    }

    @Test
    void rejectsWarehouseWithoutId() {
        assertThrows(
                IllegalArgumentException.class,
                () ->
                        new ArchiveWarehouseUseCase(new FakeStore())
                                .archive(new Warehouse()));
    }

    static class FakeStore implements WarehouseStore {

        @Override
        public List<Warehouse> getAll() {
            return new ArrayList<>();
        }

        @Override
        public void create(Warehouse warehouse) {}

        @Override
        public void update(Warehouse warehouse) {}

        @Override
        public void remove(Warehouse warehouse) {}

        @Override
        public Warehouse findByBusinessUnitCode(String code) {
            return null;
        }

        @Override
        public Warehouse findWarehouseById(Long id) {
            return null;
        }

        @Override
        public List<Warehouse> findActiveByLocation(String location) {
            return List.of();
        }

        @Override
        public void replace(String businessUnitCode, Warehouse replacement) {

        }
    }
}
