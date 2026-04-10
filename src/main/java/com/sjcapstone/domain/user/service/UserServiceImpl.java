package com.sjcapstone.domain.user.service;

import com.sjcapstone.domain.shift.entity.Shift;
import com.sjcapstone.domain.shift.exception.ShiftNotFoundException;
import com.sjcapstone.domain.shift.repository.ShiftRepository;
import com.sjcapstone.domain.user.dto.UserCreateRequest;
import com.sjcapstone.domain.user.dto.UserListResponse;
import com.sjcapstone.domain.user.dto.UserResponse;
import com.sjcapstone.domain.user.dto.UserUpdateRequest;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.exception.UserNotFoundException;
import com.sjcapstone.domain.user.exception.DuplicateEmailException;
import com.sjcapstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;

    @Transactional
    @Override
    public UserResponse createUser(UserCreateRequest request) {
        validateDuplicateEmail(request.getEmail());

        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(ShiftNotFoundException::new);

        User user = User.builder()
                .employeeId(UUID.randomUUID())
                .userName(request.getUserName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .shift(shift)
                .status(request.getStatus())
                .build();

        User savedUser = userRepository.save(user);
        return UserResponse.from(savedUser);
    }

    @Override
    public UserResponse getUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(UserNotFoundException::new);

        return UserResponse.from(user);
    }

    @Override
    public List<UserListResponse> getUsers() {
        return userRepository.findAllByDeletedAtIsNull()
                .stream()
                .map(UserListResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(UserNotFoundException::new);

        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(ShiftNotFoundException::new);

        user.update(
                request.getUserName(),
                request.getPhone(),
                request.getRole(),
                shift,
                request.getStatus()
        );

        return UserResponse.from(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(UserNotFoundException::new);

        user.softDelete();
    }

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new DuplicateEmailException();
        }
    }
}
