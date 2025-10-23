package com.proyecto.auth.repo;

import com.proyecto.auth.model.TeamMember;
import com.proyecto.auth.model.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
}
