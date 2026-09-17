package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

    @Test
    void replacesWhenStockMatchesAndCapacityAccommodatesIt() {

        FakeStore store = new FakeStore();

        store.current =
                warehouse(
                        "BU-1",
                        "LOC-1",
                        30,
                        20);

        ReplaceWarehouseUseCase useCase =
                new ReplaceWarehouseUseCase(
                        store,
                        id ->
                                "LOC-1".equals(id)
                                        ? new Location(
                                        "LOC-1",
                                        1,
                                        50)
                                        : null);

        Warehouse replacement =
                warehouse(
                        "BU-1",
                        "LOC-1",
                        25,
                        20);

        useCase.replace(replacement);

        assertEquals(
                replacement,
                store.replacement);
    }

    @Test
    void rejectsMismatchedStock() {

        FakeStore store = new FakeStore();

        store.current =
                warehouse(
                        "BU-1",
                        "LOC-1",
                        30,
                        20);

        ReplaceWarehouseUseCase useCase =
                new ReplaceWarehouseUseCase(
                        store,
                        id ->
                                new Location(
                                        "LOC-1",
                                        1,
                                        50));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        useCase.replace(
                                warehouse(
                                        "BU-1",
                                        "LOC-1",
                                        25,
                                        19)));
    }

    @Test
    void rejectsCapacityThatCannotHoldCurrentStock() {

        FakeStore store = new FakeStore();

        store.current =
                warehouse(
                        "BU-1",
                        "LOC-1",
                        30,
                        20);

        ReplaceWarehouseUseCase useCase =
                new ReplaceWarehouseUseCase(
                        store,
                        id ->
                                new Location(
                                        "LOC-1",
                                        1,
                                        50));

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        useCase.replace(
                                warehouse(
                                        "BU-1",
                                        "LOC-1",
                                        19,
                                        20)));
    }

    private static Warehouse warehouse(
            String businessUnitCode,
            String location,
            int capacity,
            int stock) {

        Warehouse warehouse = new Warehouse();

        warehouse.id = 1L;
        warehouse.businessUnitCode =
                businessUnitCode;
        warehouse.location = location;
        warehouse.capacity = capacity;
        warehouse.stock = stock;

        return warehouse;
    }

    static class FakeStore
            implements WarehouseStore {

        Warehouse current;

        Warehouse replacement;

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
        public Warehouse findByBusinessUnitCode(
                String code) {

            return current;
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
        public void replace(
                String code,
                Warehouse warehouse) {

            replacement = warehouse;
        }
    }
}