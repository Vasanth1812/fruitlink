package com.fruitlink.users.service;

import com.fruitlink.auth.entity.User;
import com.fruitlink.auth.repository.UserRepository;
import com.fruitlink.users.dto.UserDto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<UserResponse> getUsersByRole(String roleName) {
        List<User> users = userRepository.findByRoleName(roleName);
        return users.stream().map(user -> {
            UserResponse response = new UserResponse();
            response.setId(user.getId().toString());
            response.setFullName(user.getFullName());
            response.setPhone(user.getPhone());
            response.setRole(user.getRole().getName());
            return response;
        }).collect(Collectors.toList());
    }
}
