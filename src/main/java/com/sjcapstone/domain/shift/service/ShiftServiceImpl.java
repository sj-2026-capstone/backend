package com.sjcapstone.domain.shift.service;

import com.sjcapstone.domain.shift.dto.ShiftAssignmentRequest;
import com.sjcapstone.domain.shift.dto.ShiftAssignmentResponse;
import com.sjcapstone.domain.shift.dto.ShiftCreateRequest;
import com.sjcapstone.domain.shift.dto.ShiftResponse;
import com.sjcapstone.domain.shift.dto.ShiftUpdateRequest;
import com.sjcapstone.domain.shift.entity.Shift;
import com.sjcapstone.domain.shift.entity.ShiftAssignment;
import com.sjcapstone.domain.shift.exception.ShiftAlreadyAssignedException;
import com.sjcapstone.domain.shift.exception.ShiftInactiveException;
import com.sjcapstone.domain.shift.exception.ShiftNotFoundException;
import com.sjcapstone.domain.shift.repository.ShiftAssignmentRepository;
import com.sjcapstone.domain.shift.repository.ShiftRepository;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.exception.UserNotFoundException;
import com.sjcapstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ShiftServiceImpl implements ShiftService {

    private final ShiftRepository shiftRepository;
    private final ShiftAssignmentRepository shiftAssignmentRepository;
    private final UserRepository userRepository;

    @Override
    public ShiftResponse createShift(ShiftCreateRequest request) {
        Shift shift = Shift.builder()
                .shiftType(request.getShiftType())
                .shiftName(request.getShiftName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .shiftOrder(request.getShiftOrder())
                .build();

        return ShiftResponse.from(shiftRepository.save(shift));
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponse getShift(Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(ShiftNotFoundException::new);

        return ShiftResponse.from(shift);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getAllShifts() {
        return shiftRepository.findAllByIsActiveTrue()
                .stream()
                .map(ShiftResponse::from)
                .toList();
    }

    @Override
    public ShiftResponse updateShift(Long shiftId, ShiftUpdateRequest request) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(ShiftNotFoundException::new);

        shift.update(
                request.getShiftName(),
                request.getStartTime(),
                request.getEndTime(),
                request.getShiftOrder()
        );

        return ShiftResponse.from(shift);
    }

    @Override
    public void deactivateShift(Long shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(ShiftNotFoundException::new);

        shift.deactivate();
    }

    @Override
    public ShiftAssignmentResponse assignUserToShift(ShiftAssignmentRequest request) {
        if (shiftAssignmentRepository.existsByUserIdAndWorkDate(request.getUserId(), request.getWorkDate())) {
            throw new ShiftAlreadyAssignedException();
        }

        User user = userRepository.findByIdAndDeletedAtIsNull(request.getUserId())
                .orElseThrow(UserNotFoundException::new);

        Shift shift = shiftRepository.findById(request.getShiftId())
                .orElseThrow(ShiftNotFoundException::new);

        if (!shift.getIsActive()) {
            throw new ShiftInactiveException();
        }

        ShiftAssignment assignment = ShiftAssignment.builder()
                .user(user)
                .shift(shift)
                .workDate(request.getWorkDate())
                .build();

        return ShiftAssignmentResponse.from(shiftAssignmentRepository.save(assignment));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignmentResponse> getAssignmentsByDate(LocalDate date) {
        return shiftAssignmentRepository.findAllByWorkDate(date)
                .stream()
                .map(ShiftAssignmentResponse::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftAssignmentResponse> getAssignmentsByUser(Long userId) {
        if (!userRepository.existsByIdAndDeletedAtIsNull(userId)) {
            throw new UserNotFoundException();
        }

        return shiftAssignmentRepository.findAllByUserId(userId)
                .stream()
                .map(ShiftAssignmentResponse::from)
                .toList();
    }
}