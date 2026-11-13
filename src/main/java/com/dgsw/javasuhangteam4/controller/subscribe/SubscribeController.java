package com.dgsw.javasuhangteam4.controller.subscribe;

import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import com.dgsw.javasuhangteam4.service.SubService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscribeController {

    private final SubService subService;

    @GetMapping
    public ResponseEntity<List<SubResponseDto>> getSubs() {
        return ResponseEntity.ok(subService.getSubs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubResponseDto> getSub(@PathVariable Long id) {
        return ResponseEntity.ok(subService.getSub(id));
    }
}
