package com.example.jsontoxml2.service;

import com.example.jsontoxml2.model.dto.user.UserDTO;
import com.example.jsontoxml2.model.entity.User;
import com.example.jsontoxml2.repository.UserRepository;
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

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public UserDTO addUser(UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }

    public void updateUser(Long id, UserDTO updatedUserDTO) {
        User existingUser = getUserById(id);

        if (updatedUserDTO.getEmail() != null) {
            existingUser.setEmail(updatedUserDTO.getEmail());
        }
        if (updatedUserDTO.getUsername() != null) {
            existingUser.setUsername(updatedUserDTO.getUsername());
        }
        userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        getUserById(id);
        userRepository.deleteById(id);
    }

}
