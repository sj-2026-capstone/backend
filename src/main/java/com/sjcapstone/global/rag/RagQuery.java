package com.sjcapstone.global.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class RagQuery {

    private LocalDateTime from;
    private LocalDateTime to;
    private Long lineId;
    private int topN;
}