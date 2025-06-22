package com.sylhetpedia.backend.repository;

import com.sylhetpedia.backend.model.Donor;
import com.sylhetpedia.backend.model.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
public interface TutorRepository  extends JpaRepository <Tutor, Long>{
    List<Tutor> findByApprovedTrue();
    List<Tutor> findByApprovedFalse();
    void deleteById(Long id);
}
