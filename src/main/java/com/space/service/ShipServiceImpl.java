package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ShipServiceImpl implements ShipService {

    private ShipRepository shipRepository;

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }


    @Override
    public List<Ship> getAllShips(String name, String planet, ShipType shipType, Long after, Long before,
                                  Boolean isUsed, Double minSpeed, Double maxSpeed, Integer minCrewSize,
                                  Integer maxCrewSize, Double minRating, Double maxRating, ShipOrder order,
                                  Integer pageNumber, Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(order.getFieldName()));

        Specification<Ship> specification = Specification.where(filterByName(name))
                .and(filterByPlanet(planet))
                .and(filterByShipType(shipType))
                .and(filterByDate(after, before))
                .and(filterByUsed(isUsed))
                .and(filterBySpeed(minSpeed, maxSpeed))
                .and(filterByCrewSize(minCrewSize, maxCrewSize))
                .and(filterByRating(minRating, maxRating));
        Page<Ship> pageShip = shipRepository.findAll(specification, pageable);
        return pageShip.getContent();


    }

    @Override
    public List<Ship> getAllShipsCount(String name, String planet, ShipType shipType,
                                       Long after, Long before, Boolean isUsed, Double minSpeed,
                                       Double maxSpeed, Integer minCrewSize, Integer maxCrewSize,
                                       Double minRating, Double maxRating) {

        Specification<Ship> specification = Specification.where(filterByName(name))
                .and(filterByPlanet(planet))
                .and(filterByShipType(shipType))
                .and(filterByDate(after, before))
                .and(filterByUsed(isUsed))
                .and(filterBySpeed(minSpeed, maxSpeed))
                .and(filterByCrewSize(minCrewSize, maxCrewSize))
                .and(filterByRating(minRating, maxRating));
        return shipRepository.findAll(specification);
    }
    private Specification<Ship> filterByName(String name) {
        return (root, query, cb) -> name == null ? null : cb.like(root.get("name"), "%" + name + "%");
    }

    private Specification<Ship> filterByPlanet(String planet) {
        return (root, query, cb) -> planet == null ? null : cb.like(root.get("planet"), "%" + planet + "%");
    }

    private Specification<Ship> filterByShipType(ShipType shipType) {
        return (root, query, cb) -> shipType == null ? null : cb.equal(root.get("shipType"), shipType);
    }

    private Specification<Ship> filterByUsed(Boolean isUsed) {
        return (root, query, cb) -> isUsed == null ? null : cb.equal(root.get("isUsed"), isUsed);
    }

    private Specification<Ship> filterByDate(Long after, Long before) {
        return (root, query, cb) -> {
            if (after == null && before == null) return null;
            if (after == null) {
                Date beforeDate = new Date(before);
                return cb.lessThanOrEqualTo(root.get("prodDate"), beforeDate);
            }
            if (before == null) {
                Date afterDate = new Date(after);
                return cb.greaterThanOrEqualTo(root.get("prodDate"), afterDate);
            }
            return cb.between(root.get("prodDate"), new Date(after), new Date(before));
        };
    }

    private Specification<Ship> filterBySpeed(Double minSpeed, Double maxSpeed) {
        return (root, query, cb) -> {
            if (minSpeed == null && maxSpeed == null) return null;
            if (minSpeed == null) return cb.lessThanOrEqualTo(root.get("speed"), maxSpeed);
            if (maxSpeed == null) return cb.greaterThanOrEqualTo(root.get("speed"), minSpeed);
            return cb.between(root.get("speed"), minSpeed, maxSpeed);
        };
    }

    private Specification<Ship> filterByCrewSize(Integer minCrewSize, Integer maxCrewSize) {
        return (root, query, cb) -> {
            if (minCrewSize == null && maxCrewSize == null) return null;
            if (minCrewSize == null) return cb.lessThanOrEqualTo(root.get("crewSize"), maxCrewSize);
            if (maxCrewSize == null) return cb.greaterThanOrEqualTo(root.get("crewSize"), minCrewSize);
            return cb.between(root.get("crewSize"), minCrewSize, maxCrewSize);
        };
    }

    private Specification<Ship> filterByRating(Double minRating, Double maxRating) {
        return new Specification<Ship>() {
            @Override
            public Predicate toPredicate(Root<Ship> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
                if (minRating == null && maxRating == null) return null;
                if (minRating == null) return cb.lessThanOrEqualTo(root.get("rating"), maxRating);
                if (maxRating == null) return cb.greaterThanOrEqualTo(root.get("rating"), minRating);
                return cb.between(root.get("rating"), minRating, maxRating);
            }
        };
    }

    @Override
    public Ship addShip(Ship ship) {
        if (ship.getUsed() == null) {
            ship.setUsed(false);
        }
        double rating = computeRating(ship.getSpeed(), ship.getUsed(), ship.getProdDate());
        ship.setRating(rating);
        return shipRepository.save(ship);
    }

    @Override
    public Ship update(Ship oldShip, Ship newShip) {
        String name = newShip.getName();
        String planet = newShip.getPlanet();
        ShipType type = newShip.getShipType();
        Date prodDate = newShip.getProdDate();
        Boolean used = newShip.getUsed();
        Double speed = newShip.getSpeed();
        Integer crewSize = newShip.getCrewSize();

        if (name != null) {
            if (isStringValid(name)) {
                oldShip.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (planet != null) {
            if (isStringValid(planet)) {
                oldShip.setPlanet(planet);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (type != null) {
            oldShip.setShipType(type);
        }

        if (prodDate != null) {
            if (isProdDateValid(prodDate)) {
                oldShip.setProdDate(prodDate);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (used != null) {
            oldShip.setUsed(used);
        }

        if (speed != null) {
            if (isSpeedValid(speed)) {
                oldShip.setSpeed(speed);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (crewSize != null) {
            if (isCrewSizeValid(crewSize)) {
                oldShip.setCrewSize(crewSize);
            } else {
                throw new IllegalArgumentException();
            }
        }
        return oldShip;
    }

    @Override
    public void delete(Long id) {
        shipRepository.deleteById(id);
    }

    @Override
    public Ship getById(Long id) {
        return shipRepository.findById(id).orElse(null);
    }

    @Override
    public boolean existById(Long id) {
        return shipRepository.existsById(id);
    }

    @Override
    public boolean isShipValid(Ship ship) {
        return ship != null && isStringValid(ship.getName()) && isStringValid(ship.getPlanet())
                && isProdDateValid(ship.getProdDate())
                && isSpeedValid(ship.getSpeed())
                && isCrewSizeValid(ship.getCrewSize());
    }

    @Override
    public double computeRating(double speed, boolean isUsed, Date prod) {
        int now = 3019;
        int prodYear = getYearFromDate(prod);
        double k = isUsed ? 0.5 : 1;
        double rating = 80 * speed * k / (now - prodYear + 1);
        return Math.round(rating * 100) / 100D;
    }
    private boolean isCrewSizeValid(Integer crewSize) {
        int minCrewSize = 1;
        int maxCrewSize = 9999;
        return crewSize != null && crewSize.compareTo(minCrewSize) >= 0 && crewSize.compareTo(maxCrewSize) <= 0;
    }

    private boolean isSpeedValid(Double speed) {
        double minSpeed = 0.01;
        double maxSpeed = 0.99;
        return speed != null && speed.compareTo(minSpeed) >= 0 && speed.compareTo(maxSpeed) <= 0;
    }

    private boolean isStringValid(String value) {
        int maxStringLength = 50;
        return value != null && !value.isEmpty() && value.length() <= maxStringLength;
    }

    private boolean isProdDateValid(Date prodDate) {
        Date startProd = getDateForYear(2800);
        Date endProd = getDateForYear(3019);
        return prodDate != null && prodDate.after(startProd) && prodDate.before(endProd);
    }

    private Date getDateForYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    private int getYearFromDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }
}
