package com.learn.es.repository;

import java.util.List;

import com.learn.es.entity.Subway;
import org.springframework.data.repository.CrudRepository;

public interface SubwayRepository extends CrudRepository<Subway, Long>{
    List<Subway> findAllByCityEnName(String cityEnName);
}
