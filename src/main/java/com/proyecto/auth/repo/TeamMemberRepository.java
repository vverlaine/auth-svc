package com.proyecto.auth.repo;

import com.proyecto.auth.model.TeamMember;
import com.proyecto.auth.model.TeamMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TeamMemberRepository extends JpaRepository<TeamMember, TeamMemberId> {
    Optional<TeamMember> findFirstByIdUserId(UUID userId);
}
