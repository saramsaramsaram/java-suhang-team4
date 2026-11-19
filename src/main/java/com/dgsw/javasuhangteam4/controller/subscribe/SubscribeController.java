package com.dgsw.javasuhangteam4.controller.subscribe;

import com.dgsw.javasuhangteam4.dto.request.SubCreateRequest;
import com.dgsw.javasuhangteam4.dto.request.SubUpdateRequest;
import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import com.dgsw.javasuhangteam4.service.SubService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/subs", "/subscriptions"})
@RequiredArgsConstructor
public class SubscribeController {

    private final SubService subService;

    @PostMapping
    public ResponseEntity<SubResponseDto> createSub(@Valid @RequestBody SubCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subService.createSub(request));
    }

    @GetMapping
    public ResponseEntity<List<SubResponseDto>> getSubs() {
        return ResponseEntity.ok(subService.getSubs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubResponseDto> getSub(@PathVariable Long id) {
        return ResponseEntity.ok(subService.getSub(id));
    }

    @PutMapping("/{id}")
    public SubResponseDto updateSub(
            @PathVariable Long id, @Valid @RequestBody SubUpdateRequest request) {
        return subService.updateSub(id, request);
    }
}
