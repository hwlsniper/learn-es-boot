package com.learn.es.repository;

import java.util.List;

import com.learn.es.entity.SubwayStation;
import org.springframework.data.repository.CrudRepository;

public interface SubwayStationRepository extends CrudRepository<SubwayStation, Long> {
    List<SubwayStation> findAllBySubwayId(Long subwayId);
}
