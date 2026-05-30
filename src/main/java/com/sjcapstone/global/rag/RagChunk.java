package com.sjcapstone.global.rag;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RagChunk {

    public enum ChunkType {
        LINE_STATS, SHIFT_STATS, HOUR_STATS, DEFECT_TYPE_STATS, WEEKLY_TREND
    }

    private String chunkId;
    private ChunkType type;
    private String content;
    private double relevanceScore;
}