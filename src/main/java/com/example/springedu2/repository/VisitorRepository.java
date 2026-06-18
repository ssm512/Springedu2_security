package com.example.springedu2.repository;

import com.example.springedu2.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VisitorRepository extends JpaRepository<Visitor, Long> {
    //
    List<Visitor> findByName(String key);

    List<Visitor> findByMemoContainingIgnoreCaseOrderByIdDesc(String key);

    @Query(value = "select v from Visitor v where v.name = ?1", nativeQuery = false)
    List<Visitor> findByIrum(String key);
}
