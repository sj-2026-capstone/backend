package com.sjcapstone.domain.inspection.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DefectType {
    SCRATCH("스크래치"),
    DENT("찌그러짐"),
    CRACK("균열"),
    CONTAMINATION("오염"),
    MISSING_PART("부품 누락"),
    DIMENSION_ERROR("치수 불량");

    private final String displayName;
}