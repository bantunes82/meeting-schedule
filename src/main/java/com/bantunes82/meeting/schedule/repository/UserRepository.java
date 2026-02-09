package com.bantunes82.meeting.schedule.repository;

import com.bantunes82.meeting.schedule.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repository for User entity operations.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

}
