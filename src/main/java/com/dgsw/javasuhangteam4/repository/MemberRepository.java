package com.dgsw.javasuhangteam4.repository;

import com.dgsw.javasuhangteam4.enitity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
