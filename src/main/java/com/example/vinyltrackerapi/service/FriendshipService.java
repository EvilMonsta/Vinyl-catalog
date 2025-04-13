package com.example.vinyltrackerapi.service;

import com.example.vinyltrackerapi.api.dto.UserDto;
import com.example.vinyltrackerapi.api.enums.FriendshipStatus;
import java.util.List;

public interface FriendshipService {
    void sendRequest(Integer fromId, Integer toId);
    void acceptRequest(Integer fromId, Integer toId);
    void removeFriend(Integer userId, Integer friendId);
    List<UserDto> getFriends(Integer userId);
    List<UserDto> getIncomingRequests(Integer userId);
    FriendshipStatus getFriendshipStatus(Integer userId, Integer otherUserId);
}