package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserDto;
import com.example.vinyltrackerapi.api.enums.FriendshipStatus;
import com.example.vinyltrackerapi.api.models.Friendship;
import com.example.vinyltrackerapi.api.models.User;
import com.example.vinyltrackerapi.api.repositories.FriendshipRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FriendshipService {
    private final FriendshipRepository repository;
    private final UserService userService;

    public FriendshipService(FriendshipRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    public void sendRequest(Integer fromId, Integer toId) {
        if (fromId.equals(toId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя добавить самого себя");
        }

        User from = userService.getUser(fromId);
        User to = userService.getUser(toId);

        repository.findByUserAndFriend(from, to).ifPresent(f -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Запрос уже существует");
        });

        Friendship request = new Friendship(null, from, to, FriendshipStatus.PENDING, LocalDateTime.now());
        repository.save(request);
    }

    public void acceptRequest(Integer fromId, Integer toId) {
        User from = userService.getUser(fromId);
        User to = userService.getUser(toId);

        Friendship request = repository.findByUserAndFriend(from, to)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Запрос не найден"));

        if (request.getStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Нельзя принять этот запрос");
        }

        request.setStatus(FriendshipStatus.ACCEPTED);
        repository.save(request);
    }

    public void removeFriend(Integer userId, Integer friendId) {
        User user = userService.getUser(userId);
        User friend = userService.getUser(friendId);

        repository.findByUserAndFriend(user, friend)
                .ifPresent(repository::delete);

        repository.findByUserAndFriend(friend, user)
                .ifPresent(repository::delete);
    }

    public List<UserDto> getFriends(Integer userId) {
        User user = userService.getUser(userId);
        List<Friendship> direct = repository.findAllByUserAndStatus(user, FriendshipStatus.ACCEPTED);
        List<Friendship> reverse = repository.findAllByFriendAndStatus(user, FriendshipStatus.ACCEPTED);

        return Stream.concat(
                direct.stream().map(f -> new UserDto(f.getFriend())),
                reverse.stream().map(f -> new UserDto(f.getUser()))
        ).toList();
    }

    public List<UserDto> getIncomingRequests(Integer userId) {
        User user = userService.getUser(userId);
        return repository.findAllByFriendAndStatus(user, FriendshipStatus.PENDING)
                .stream()
                .map(f -> new UserDto(f.getUser()))
                .toList();
    }

    public FriendshipStatus getFriendshipStatus(Integer userId, Integer otherUserId) {
        User user = userService.getUser(userId);
        User other = userService.getUser(otherUserId);

        return repository.findByUserAndFriend(user, other)
                .or(() -> repository.findByUserAndFriend(other, user))
                .map(Friendship::getStatus)
                .orElse(null);
    }
}