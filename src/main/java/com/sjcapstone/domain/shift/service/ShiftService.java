package com.sjcapstone.domain.shift.service;

import com.sjcapstone.domain.shift.dto.ShiftAssignmentRequest;
import com.sjcapstone.domain.shift.dto.ShiftAssignmentResponse;
import com.sjcapstone.domain.shift.dto.ShiftCreateRequest;
import com.sjcapstone.domain.shift.dto.ShiftResponse;
import com.sjcapstone.domain.shift.dto.ShiftUpdateRequest;

import java.time.LocalDate;
import java.util.List;

public interface ShiftService {

    ShiftResponse createShift(ShiftCreateRequest request);

    ShiftResponse getShift(Long shiftId);

    List<ShiftResponse> getAllShifts();

    ShiftResponse updateShift(Long shiftId, ShiftUpdateRequest request);

    void deactivateShift(Long shiftId);

    ShiftAssignmentResponse assignUserToShift(ShiftAssignmentRequest request);

    List<ShiftAssignmentResponse> getAssignmentsByDate(LocalDate date);

    List<ShiftAssignmentResponse> getAssignmentsByUser(Long userId);
}