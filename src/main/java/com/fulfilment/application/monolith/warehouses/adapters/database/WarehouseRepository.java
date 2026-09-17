package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return find("archivedAt is null order by businessUnitCode")
            .list()
            .stream()
            .map(DbWarehouse::toWarehouse)
            .toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {

    var entity = DbWarehouse.fromWarehouse(warehouse);

    if (entity.createdAt == null) {
      entity.createdAt = java.time.LocalDateTime.now();
    }

    entity.archivedAt = null;

    persist(entity);

    warehouse.id = entity.id;
    warehouse.createdAt = entity.createdAt;
    warehouse.archivedAt = null;
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {

    if (warehouse.id == null) {
      throw new IllegalArgumentException(
              "Warehouse id must be provided for update.");
    }

    DbWarehouse entity = findById(warehouse.id);

    if (entity == null) {
      throw new IllegalArgumentException(
              "Warehouse with id of " + warehouse.id + " does not exist.");
    }

    entity.businessUnitCode = warehouse.businessUnitCode;
    entity.location = warehouse.location;
    entity.capacity = warehouse.capacity;
    entity.stock = warehouse.stock;
    entity.createdAt = warehouse.createdAt;
    entity.archivedAt = warehouse.archivedAt;
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {

    if (warehouse.id == null) {
      return;
    }

    DbWarehouse entity = findById(warehouse.id);

    if (entity != null) {
      delete(entity);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {

    if (buCode == null) {
      return null;
    }

    DbWarehouse entity =
            find(
                    "businessUnitCode = ?1 and archivedAt is null",
                    buCode)
                    .firstResult();

    return entity == null ? null : entity.toWarehouse();
  }

  @Override
  public Warehouse findWarehouseById(Long id) {

    if (id == null) {
      return null;
    }

    DbWarehouse entity =
            find(
                    "id = ?1 and archivedAt is null",
                    id)
                    .firstResult();

    return entity == null ? null : entity.toWarehouse();
  }

  @Override
  public List<Warehouse> findActiveByLocation(String location) {

    return find(
            "location = ?1 and archivedAt is null",
            location)
            .list()
            .stream()
            .map(DbWarehouse::toWarehouse)
            .toList();
  }

  @Override
  @Transactional
  public void replace(
          String businessUnitCode,
          Warehouse replacement) {

    DbWarehouse current =
            find(
                    "businessUnitCode = ?1 and archivedAt is null",
                    businessUnitCode)
                    .firstResult();

    if (current == null) {
      throw new IllegalArgumentException(
              "Warehouse with business unit code "
                      + businessUnitCode
                      + " does not exist.");
    }

    current.archivedAt = java.time.LocalDateTime.now();

    var newEntity =
            DbWarehouse.fromWarehouse(replacement);

    newEntity.id = null;
    newEntity.businessUnitCode = businessUnitCode;
    newEntity.archivedAt = null;

    if (newEntity.createdAt == null) {
      newEntity.createdAt = java.time.LocalDateTime.now();
    }

    persist(newEntity);

    replacement.id = newEntity.id;
    replacement.createdAt = newEntity.createdAt;
    replacement.archivedAt = null;
  }
}
