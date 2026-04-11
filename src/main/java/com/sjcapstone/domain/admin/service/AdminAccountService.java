package com.sjcapstone.domain.admin.service;

import com.sjcapstone.domain.admin.dto.AdminAccountCreateRequest;
import com.sjcapstone.domain.admin.dto.AdminAccountPageResponse;
import com.sjcapstone.domain.admin.dto.AdminAccountResponse;
import com.sjcapstone.domain.admin.dto.AdminAccountStatusUpdateRequest;
import com.sjcapstone.domain.admin.dto.AdminAccountSummaryResponse;
import com.sjcapstone.domain.admin.dto.AdminAccountUpdateRequest;
import com.sjcapstone.domain.admin.dto.LoginIdAvailabilityResponse;
import com.sjcapstone.domain.user.entity.UserStatus;

public interface AdminAccountService {

    AdminAccountResponse createAccount(AdminAccountCreateRequest request);

    LoginIdAvailabilityResponse checkLoginIdAvailability(String loginId);

    AdminAccountSummaryResponse getAccountSummary();

    AdminAccountPageResponse getAccounts(String keyword, UserStatus status, int page, int size);

    AdminAccountResponse getAccount(Long userId);

    AdminAccountResponse updateAccount(Long userId, AdminAccountUpdateRequest request);

    AdminAccountResponse updateAccountStatus(Long userId, AdminAccountStatusUpdateRequest request);
}
