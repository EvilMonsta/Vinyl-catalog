package com.example.vinyltrackerapi.api.controllers;

import com.example.vinyltrackerapi.api.dto.UserDto;
import com.example.vinyltrackerapi.api.enums.FriendshipStatus;
import com.example.vinyltrackerapi.service.FriendshipService;
import com.example.vinyltrackerapi.service.UserService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendshipController {
    private final FriendshipService friendshipService;
    private final UserService userService;

    @PostMapping("/request")
    public ResponseEntity<Void> sendRequest(Principal principal, @RequestParam Integer toId) {
        Integer fromId = userService.getUserByEmail(principal.getName()).orElseThrow().getId();
        friendshipService.sendRequest(fromId, toId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/accept")
    public ResponseEntity<Void> acceptRequest(Principal principal, @RequestParam Integer fromId) {
        Integer toId = userService.getUserByEmail(principal.getName()).orElseThrow().getId();
        friendshipService.acceptRequest(fromId, toId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/remove")
    public ResponseEntity<Void> removeFriend(Principal principal, @RequestParam Integer friendId) {
        Integer userId = userService.getUserByEmail(principal.getName()).orElseThrow().getId();
        friendshipService.removeFriend(userId, friendId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<UserDto> getFriends(Principal principal) {
        Integer userId = userService.getUserByEmail(principal.getName()).orElseThrow().getId();
        return friendshipService.getFriends(userId);
    }

    @GetMapping("/requests")
    public List<UserDto> getIncomingRequests(Principal principal) {
        Integer userId = userService.getUserByEmail(principal.getName()).orElseThrow().getId();
        return friendshipService.getIncomingRequests(userId);
    }

    @GetMapping("/status")
    public ResponseEntity<FriendshipStatus> getStatus(Principal principal,
                                                      @RequestParam Integer userId) {
        Integer currentId = userService.getUserByEmail(principal.getName()).orElseThrow().getId();
        FriendshipStatus status = friendshipService.getFriendshipStatus(currentId, userId);
        return ResponseEntity.ok(status);
    }
}