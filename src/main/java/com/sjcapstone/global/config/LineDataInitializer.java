package com.sjcapstone.global.config;

import com.sjcapstone.domain.line.entity.Line;
import com.sjcapstone.domain.line.entity.LineCode;
import com.sjcapstone.domain.line.repository.LineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class LineDataInitializer {

    private final LineRepository lineRepository;

    @Bean
    public ApplicationRunner initializeLines() {
        return args -> {
            seedLine(LineCode.A, "A라인");
            seedLine(LineCode.B, "B라인");
            seedLine(LineCode.C, "C라인");
        };
    }

    private void seedLine(LineCode lineCode, String lineName) {
        if (lineRepository.existsByLineCode(lineCode)) {
            return;
        }

        lineRepository.save(Line.builder()
                .lineCode(lineCode)
                .lineName(lineName)
                .isActive(true)
                .build());
    }
}
