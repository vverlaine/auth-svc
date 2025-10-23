package com.proyecto.auth.model;

import java.util.UUID;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "team_members", schema = "app")
public class TeamMember {

    @EmbeddedId
    private TeamMemberId id;

    public TeamMember() {
    }

    public TeamMember(TeamMemberId id) {
        this.id = id;
    }

    public TeamMember(UUID teamId, UUID userId) {
        this.id = new TeamMemberId(teamId, userId);
    }

    public TeamMemberId getId() {
        return id;
    }

    public void setId(TeamMemberId id) {
        this.id = id;
    }

    public UUID getTeamId() {
        return id != null ? id.getTeamId() : null;
    }

    public UUID getUserId() {
        return id != null ? id.getUserId() : null;
    }
}
