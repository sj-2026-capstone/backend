package com.sjcapstone.domain.user.service;

import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.exception.LineNotFoundException;
import com.sjcapstone.domain.line.repository.LineRepository;
import com.sjcapstone.domain.shift.entity.Shift;
import com.sjcapstone.domain.shift.exception.ShiftNotFoundException;
import com.sjcapstone.domain.shift.repository.ShiftRepository;
import com.sjcapstone.domain.user.dto.UserListResponse;
import com.sjcapstone.domain.user.dto.UserResponse;
import com.sjcapstone.domain.user.dto.UserUpdateRequest;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.domain.user.exception.LineRequiredForWorkerException;
import com.sjcapstone.domain.user.exception.ShiftRequiredForWorkerException;
import com.sjcapstone.domain.user.exception.UserNotFoundException;
import com.sjcapstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ShiftRepository shiftRepository;
    private final LineRepository lineRepository;

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

        Shift shift = resolveShift(request.getRole(), request.getShiftId());
        Line line = resolveLine(request.getRole(), request.getLineId());

        user.update(
                request.getUserName(),
                user.getEmail(),
                request.getPhone(),
                request.getRole(),
                shift,
                line,
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

    private Shift resolveShift(UserRole role, Long shiftId) {
        if (shiftId == null) {
            if (role == UserRole.WORKER) {
                throw new ShiftRequiredForWorkerException();
            }
            return null;
        }

        return shiftRepository.findById(shiftId)
                .orElseThrow(ShiftNotFoundException::new);
    }

    private Line resolveLine(UserRole role, Long lineId) {
        if (lineId == null) {
            if (role == UserRole.WORKER) {
                throw new LineRequiredForWorkerException();
            }
            return null;
        }

        return lineRepository.findByIdAndIsActiveTrue(lineId)
                .orElseThrow(LineNotFoundException::new);
    }
}
