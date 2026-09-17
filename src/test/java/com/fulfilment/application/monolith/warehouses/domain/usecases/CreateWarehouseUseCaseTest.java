package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class CreateWarehouseUseCaseTest {

    @Test
    void createsWarehouseWhenAllBusinessRulesAreSatisfied() {
        FakeStore store = new FakeStore();

        CreateWarehouseUseCase useCase =
                new CreateWarehouseUseCase(store, resolver());

        Warehouse warehouse =
                warehouse("BU-1", "LOC-1", 40, 10);

        useCase.create(warehouse);

        assertEquals(1, store.created.size());
        assertEquals(warehouse, store.created.get(0));
    }

    @Test
    void rejectsUnknownLocation() {
        FakeStore store = new FakeStore();

        CreateWarehouseUseCase useCase =
                new CreateWarehouseUseCase(store, id -> null);

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        useCase.create(
                                warehouse(
                                        "BU-1",
                                        "UNKNOWN",
                                        10,
                                        5)));
    }

    @Test
    void rejectsDuplicateBusinessUnitCode() {
        FakeStore store = new FakeStore();

        store.active.add(
                warehouse(
                        "BU-1",
                        "LOC-1",
                        10,
                        5));

        CreateWarehouseUseCase useCase =
                new CreateWarehouseUseCase(store, resolver());

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        useCase.create(
                                warehouse(
                                        "BU-1",
                                        "LOC-1",
                                        10,
                                        5)));
    }

    @Test
    void rejectsStockAboveCapacity() {
        FakeStore store = new FakeStore();

        CreateWarehouseUseCase useCase =
                new CreateWarehouseUseCase(store, resolver());

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        useCase.create(
                                warehouse(
                                        "BU-1",
                                        "LOC-1",
                                        10,
                                        11)));
    }

    private static LocationResolver resolver() {
        return id ->
                "LOC-1".equals(id)
                        ? new Location("LOC-1", 2, 100)
                        : null;
    }

    private static Warehouse warehouse(
            String businessUnitCode,
            String location,
            int capacity,
            int stock) {

        Warehouse warehouse = new Warehouse();

        warehouse.businessUnitCode = businessUnitCode;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = stock;

        return warehouse;
    }

    static class FakeStore implements WarehouseStore {

        final List<Warehouse> active = new ArrayList<>();

        final List<Warehouse> created =
                new ArrayList<>();

        @Override
        public List<Warehouse> getAll() {
            return active;
        }

        @Override
        public void create(Warehouse warehouse) {
            created.add(warehouse);
            active.add(warehouse);
        }

        @Override
        public void update(Warehouse warehouse) {}

        @Override
        public void remove(Warehouse warehouse) {}

        @Override
        public Warehouse findByBusinessUnitCode(
                String code) {

            return active.stream()
                    .filter(
                            warehouse ->
                                    code.equals(
                                            warehouse.businessUnitCode)
                                            && warehouse.archivedAt == null)
                    .findFirst()
                    .orElse(null);
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
