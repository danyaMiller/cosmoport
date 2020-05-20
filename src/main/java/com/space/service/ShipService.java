package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.Date;
import java.util.List;

public interface ShipService {
    List<Ship> getAllShips(String name, String planet, ShipType shipType,
                           Long after, Long before, Boolean isUsed, Double minSpeed,
                           Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                           Double minRating, Double maxRating, ShipOrder order,
                           Integer pageNumber, Integer pageSize);

    List<Ship> getAllShipsCount(String name, String planet, ShipType shipType,
                           Long after, Long before, Boolean isUsed, Double minSpeed,
                           Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                           Double minRating,Double maxRating);

    Ship addShip(Ship ship);
    Ship update(Ship oldShip, Ship newShip);
    void delete(Long id);
    Ship getById(Long id);

    boolean existById(Long id);
    boolean isShipValid(Ship ship);
    double computeRating(double speed, boolean isUsed, Date prod);
}
