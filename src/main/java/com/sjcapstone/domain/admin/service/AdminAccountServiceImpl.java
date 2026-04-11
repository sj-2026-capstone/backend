package com.sjcapstone.domain.admin.service;

import com.sjcapstone.domain.admin.dto.AdminAccountCreateRequest;
import com.sjcapstone.domain.admin.dto.AdminAccountListItemResponse;
import com.sjcapstone.domain.admin.dto.AdminAccountPageResponse;
import com.sjcapstone.domain.admin.dto.AdminAccountResponse;
import com.sjcapstone.domain.admin.dto.AdminAccountStatusUpdateRequest;
import com.sjcapstone.domain.admin.dto.AdminAccountSummaryResponse;
import com.sjcapstone.domain.admin.dto.AdminAccountUpdateRequest;
import com.sjcapstone.domain.admin.dto.LoginIdAvailabilityResponse;
import com.sjcapstone.domain.auth.entity.Auth;
import com.sjcapstone.domain.auth.exception.AuthNotFoundException;
import com.sjcapstone.domain.auth.exception.DuplicateLoginIdException;
import com.sjcapstone.domain.auth.exception.PasswordConfirmMismatchException;
import com.sjcapstone.domain.auth.repository.AuthRepository;
import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.exception.LineNotFoundException;
import com.sjcapstone.domain.line.repository.LineRepository;
import com.sjcapstone.domain.shift.entity.Shift;
import com.sjcapstone.domain.shift.exception.ShiftNotFoundException;
import com.sjcapstone.domain.shift.repository.ShiftRepository;
import com.sjcapstone.domain.user.entity.User;
import com.sjcapstone.domain.user.entity.UserRole;
import com.sjcapstone.domain.user.entity.UserStatus;
import com.sjcapstone.domain.user.exception.DuplicateEmailException;
import com.sjcapstone.domain.user.exception.LineRequiredForWorkerException;
import com.sjcapstone.domain.user.exception.ShiftRequiredForWorkerException;
import com.sjcapstone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminAccountServiceImpl implements AdminAccountService {

    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    private final ShiftRepository shiftRepository;
    private final LineRepository lineRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminAccountResponse createAccount(AdminAccountCreateRequest request) {
        validateDuplicateLoginId(request.getLoginId());
        validateDuplicateEmail(request.getEmail());
        validatePasswordConfirmation(request.getPassword(), request.getConfirmPassword());

        Shift shift = resolveShift(request.getRole(), request.getShiftId());
        Line line = resolveLine(request.getRole(), request.getLineId());

        User user = User.builder()
                .employeeId(UUID.randomUUID())
                .userName(request.getUserName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .role(request.getRole())
                .shift(shift)
                .line(line)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);

        Auth auth = Auth.builder()
                .user(savedUser)
                .loginId(request.getLoginId())
                .password(passwordEncoder.encode(request.getPassword()))
                .passwordChangeRequired(true)
                .build();

        Auth savedAuth = authRepository.save(auth);

        return AdminAccountResponse.from(savedUser, savedAuth);
    }

    @Override
    @Transactional(readOnly = true)
    public LoginIdAvailabilityResponse checkLoginIdAvailability(String loginId) {
        return LoginIdAvailabilityResponse.of(loginId, !authRepository.existsByLoginId(loginId));
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAccountSummaryResponse getAccountSummary() {
        return AdminAccountSummaryResponse.builder()
                .totalCount(userRepository.countByDeletedAtIsNull())
                .activeCount(userRepository.countByStatusAndDeletedAtIsNull(UserStatus.ACTIVE))
                .inactiveCount(userRepository.countByStatusAndDeletedAtIsNull(UserStatus.INACTIVE))
                .pendingCount(userRepository.countByStatusAndDeletedAtIsNull(UserStatus.PENDING))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAccountPageResponse getAccounts(String keyword, UserStatus status, int page, int size) {
        String normalizedKeyword = normalizeKeyword(keyword);
        Page<Auth> accounts = authRepository.searchAccounts(
                normalizedKeyword,
                status,
                PageRequest.of(page, size)
        );

        Page<AdminAccountListItemResponse> responsePage = accounts.map(AdminAccountListItemResponse::from);
        return AdminAccountPageResponse.from(responsePage);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminAccountResponse getAccount(Long userId) {
        Auth auth = getAuthByUserId(userId);
        return AdminAccountResponse.from(auth.getUser(), auth);
    }

    @Override
    public AdminAccountResponse updateAccount(Long userId, AdminAccountUpdateRequest request) {
        Auth auth = getAuthByUserId(userId);
        User user = auth.getUser();

        validateDuplicateLoginId(userId, request.getLoginId());
        validateDuplicateEmail(userId, request.getEmail());

        Shift shift = resolveShift(request.getRole(), request.getShiftId());
        Line line = resolveLine(request.getRole(), request.getLineId());

        user.update(
                request.getUserName(),
                request.getEmail(),
                request.getPhone(),
                request.getRole(),
                shift,
                line,
                request.getStatus()
        );
        auth.updateLoginId(request.getLoginId());

        return AdminAccountResponse.from(user, auth);
    }

    @Override
    public AdminAccountResponse updateAccountStatus(Long userId, AdminAccountStatusUpdateRequest request) {
        Auth auth = getAuthByUserId(userId);
        User user = auth.getUser();

        Shift shift = resolveShift(user.getRole(), user.getShift() != null ? user.getShift().getId() : null);
        Line line = resolveLine(user.getRole(), user.getLine() != null ? user.getLine().getId() : null);
        user.update(
                user.getUserName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                shift,
                line,
                request.getStatus()
        );

        return AdminAccountResponse.from(user, auth);
    }

    private void validateDuplicateLoginId(String loginId) {
        if (authRepository.existsByLoginId(loginId)) {
            throw new DuplicateLoginIdException();
        }
    }

    private void validateDuplicateLoginId(Long userId, String loginId) {
        Auth auth = getAuthByUserId(userId);
        if (!auth.getLoginId().equals(loginId) && authRepository.existsByLoginId(loginId)) {
            throw new DuplicateLoginIdException();
        }
    }

    private void validateDuplicateEmail(String email) {
        if (email != null && userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new DuplicateEmailException();
        }
    }

    private void validateDuplicateEmail(Long userId, String email) {
        if (email != null && userRepository.existsByEmailAndDeletedAtIsNullAndIdNot(email, userId)) {
            throw new DuplicateEmailException();
        }
    }

    private void validatePasswordConfirmation(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new PasswordConfirmMismatchException();
        }
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

    private Auth getAuthByUserId(Long userId) {
        return authRepository.findByUserId(userId)
                .orElseThrow(AuthNotFoundException::new);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
