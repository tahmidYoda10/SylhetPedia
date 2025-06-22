package com.sylhetpedia.backend.repository;

import com.sylhetpedia.backend.model.Donor;
import com.sylhetpedia.backend.model.RentHouse;
import com.sylhetpedia.backend.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface RentHouseRepository  extends JpaRepository <RentHouse, Long>{
    List<RentHouse> findByApprovedTrue();
    List<RentHouse> findByApprovedFalse();
    void deleteById(Long id);
}