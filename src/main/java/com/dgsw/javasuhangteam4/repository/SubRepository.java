package com.dgsw.javasuhangteam4.repository;

import com.dgsw.javasuhangteam4.enitity.Sub;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface SubRepository extends JpaRepository<Sub, Long> {

    @Query("""
            select s from Sub s join fetch s.member
            where s.active = true
              and s.startDate <= :today
              and s.billingDay between :day and :lastBillingDay
            """)
    List<Sub> findDueSubs(LocalDate today, int day, int lastBillingDay);

    @Query("""
            select s from Sub s
            where s.active = true
              and s.startDate <= :monthEnd
            order by s.billingDay asc, s.id asc
            """)
    List<Sub> findActiveSubsStartedBy(LocalDate monthEnd);
}
