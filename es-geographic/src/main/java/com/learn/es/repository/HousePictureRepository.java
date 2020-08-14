package com.learn.es.repository;

import java.util.List;

import com.learn.es.entity.HousePicture;
import org.springframework.data.repository.CrudRepository;

public interface HousePictureRepository extends CrudRepository<HousePicture, Long> {
    List<HousePicture> findAllByHouseId(Long id);
}
