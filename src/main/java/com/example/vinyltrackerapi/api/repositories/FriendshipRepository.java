package com.example.vinyltrackerapi.api.repositories;

import com.example.vinyltrackerapi.api.enums.FriendshipStatus;
import com.example.vinyltrackerapi.api.models.Friendship;
import com.example.vinyltrackerapi.api.models.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, Integer> {
    Optional<Friendship> findByUserAndFriend(User user, User friend);

    List<Friendship> findAllByUserAndStatus(User user, FriendshipStatus status);

    List<Friendship> findAllByFriendAndStatus(User friend, FriendshipStatus status);

    List<Friendship> findAllByUserAndStatusOrFriendAndStatus(User user1, FriendshipStatus status1, User user2,
                                                             FriendshipStatus status2);
}