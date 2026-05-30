package com.sjcapstone.global.rag;

import java.util.List;

/**
 * RAG 문서 청크 검색 전략 인터페이스.
 * 현재: SQL 집계 기반 통계 청크 (SimpleRagChunkRetriever)
 * 추후: pgvector / Qdrant / Redis Vector 등으로 구현체 교체 가능
 */
public interface RagChunkRetriever {

    List<RagChunk> retrieve(RagQuery query);
}