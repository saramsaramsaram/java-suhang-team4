package com.dgsw.javasuhangteam4.controller.subscribe;

import com.dgsw.javasuhangteam4.dto.request.SubCreateRequest;
import com.dgsw.javasuhangteam4.dto.request.SubUpdateRequest;
import com.dgsw.javasuhangteam4.dto.response.MonthlySummaryResponseDto;
import com.dgsw.javasuhangteam4.dto.response.SubDetailResponseDto;
import com.dgsw.javasuhangteam4.dto.response.SubResponseDto;
import com.dgsw.javasuhangteam4.service.SubService;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/subs", "/subscriptions"})
@RequiredArgsConstructor
public class SubscribeController {

    private static final MediaType XLSX =
            MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    private final SubService subService;

    @PostMapping
    public ResponseEntity<SubResponseDto> createSub(@Valid @RequestBody SubCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(subService.createSub(request));
    }

    @GetMapping
    public ResponseEntity<List<SubResponseDto>> getSubs() {
        return ResponseEntity.ok(subService.getSubs());
    }

    @GetMapping("/summary")
    public ResponseEntity<MonthlySummaryResponseDto> getMonthlySummary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return ResponseEntity.ok(subService.getMonthlySummary(month));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubDetailResponseDto> getSub(@PathVariable Long id) {
        return ResponseEntity.ok(subService.getSub(id));
    }

    @PutMapping("/{id}")
    public SubResponseDto updateSub(
            @PathVariable Long id, @Valid @RequestBody SubUpdateRequest request) {
        return subService.updateSub(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSub(@PathVariable Long id) {
        subService.deleteSub(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportExcel(@RequestParam YearMonth month) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=subs-" + month + ".xlsx")
                .contentType(XLSX)
                .body(subService.exportExcel(month));
    }
}
