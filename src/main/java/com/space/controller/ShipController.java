package com.space.controller;


import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/ships")
public class ShipController {

    private ShipService shipService;

    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping
    public ResponseEntity<List<Ship>> getShipsList
            (@RequestParam(required = false) String name,
             @RequestParam(required = false) String planet,
             @RequestParam(required = false) ShipType shipType,
             @RequestParam(required = false) Long after,
             @RequestParam(required = false) Long before,
             @RequestParam(required = false) Boolean isUsed,
             @RequestParam(required = false) Double minSpeed,
             @RequestParam(required = false) Double maxSpeed,
             @RequestParam(required = false) Integer minCrewSize,
             @RequestParam(required = false) Integer maxCrewSize,
             @RequestParam(required = false) Double minRating,
             @RequestParam(required = false) Double maxRating,
             @RequestParam(required = false, defaultValue = "ID") ShipOrder order,
             @RequestParam(required = false, defaultValue = "0") Integer pageNumber,
             @RequestParam(required = false, defaultValue = "3") Integer pageSize) {

        List<Ship> ships = shipService.getAllShips(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, order, pageNumber, pageSize);
        return new ResponseEntity<>(ships, HttpStatus.OK);
    }

    @GetMapping("/count")
    public Integer getShipsCount
            (@RequestParam(required = false) String name,
             @RequestParam(required = false) String planet,
             @RequestParam(required = false) ShipType shipType,
             @RequestParam(required = false) Long after,
             @RequestParam(required = false) Long before,
             @RequestParam(required = false) Boolean isUsed,
             @RequestParam(required = false) Double minSpeed,
             @RequestParam(required = false) Double maxSpeed,
             @RequestParam(required = false) Integer minCrewSize,
             @RequestParam(required = false) Integer maxCrewSize,
             @RequestParam(required = false) Double minRating,
             @RequestParam(required = false) Double maxRating) {

        List<Ship> ships = shipService.getAllShipsCount(name, planet, shipType, after, before, isUsed, minSpeed,
                maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);
        return ships.size();
    }

    @PostMapping
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship) {
        if (!shipService.isShipValid(ship))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Ship savedShip = shipService.addShip(ship);
        return new ResponseEntity<>(savedShip, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable("id") Long id) {
        if (id == null || id <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Ship ship = shipService.getById(id);
        if (ship == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(ship, HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable("id") Long id,
                                           @RequestBody Ship newShip) {
        if (id == null || id <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Ship oldShip = shipService.getById(id);
        if (oldShip == null)
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Ship shipAfterUpdate;
        try {
            shipAfterUpdate = shipService.update(oldShip, newShip);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        double newRating = shipService.computeRating(shipAfterUpdate.getSpeed(),
                shipAfterUpdate.getUsed(),
                shipAfterUpdate.getProdDate());
        shipAfterUpdate.setRating(newRating);
        return new ResponseEntity<>(shipAfterUpdate, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Ship> deleteShip(@PathVariable("id") Long id) {
        if (id == null || id <= 0)
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (!shipService.existById(id))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        shipService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
