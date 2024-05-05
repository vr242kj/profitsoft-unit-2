package com.example.jsontoxml2.service;

import com.example.jsontoxml2.model.dto.user.UserSaveDto;
import com.example.jsontoxml2.model.dto.user.UserInfoDto;
import com.example.jsontoxml2.model.entity.User;
import com.example.jsontoxml2.repository.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    public List<UserInfoDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> modelMapper.map(user, UserInfoDto.class))
                .collect(Collectors.toList());
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public UserInfoDto addUser(UserSaveDto newUserSaveDto) {
        User user = modelMapper.map(newUserSaveDto, User.class);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserInfoDto.class);
    }

    public void updateUser(Long id, UserSaveDto updatedUserSaveDto) {
        getUserById(id);
        User user = modelMapper.map(updatedUserSaveDto, User.class);
        user.setId(id);
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        getUserById(id);
        userRepository.deleteById(id);
    }

}
