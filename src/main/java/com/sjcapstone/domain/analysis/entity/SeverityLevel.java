package com.sjcapstone.domain.analysis.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SeverityLevel {
    HIGH("높음"), MEDIUM("중간"), LOW("관찰");

    private final String displayName;
}