package com.dgsw.javasuhangteam4.controller.subscribe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.dgsw.javasuhangteam4.enitity.Member;
import com.dgsw.javasuhangteam4.repository.MemberRepository;
import com.dgsw.javasuhangteam4.repository.SubRepository;
import com.jayway.jsonpath.JsonPath;
import java.io.ByteArrayInputStream;
import java.util.stream.Stream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:subscription-create-delete-export")
@AutoConfigureMockMvc
class SubscribeCreateDeleteExportTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SubRepository subRepository;

    // 이 클래스의 테스트들은 같은 DB를 쓰므로, 앞 테스트의 구독이 엑셀 결과에 섞이지 않게 비운다
    @BeforeEach
    void cleanSubs() {
        subRepository.deleteAll();
    }

    @Test
    void createsThenDeletesSubscription() throws Exception {
        Long memberId = saveMember();

        String body = mvc.perform(create(memberId, "유튜브 뮤직", 14900, 1, "2026-11-01"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.serviceName").value("유튜브 뮤직"))
                .andReturn().getResponse().getContentAsString();
        long subId = ((Number) JsonPath.read(body, "$.id")).longValue();

        mvc.perform(delete("/api/subs/{id}", subId)).andExpect(status().isNoContent());
        mvc.perform(delete("/api/subs/{id}", subId)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsUnknownMember() throws Exception {
        mvc.perform(create(99999L, "넷플릭스", 13500, 5, "2026-11-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("회원 없음"));
    }

    @Test
    void exportsMonthlyExcel() throws Exception {
        Long memberId = saveMember();
        mvc.perform(create(memberId, "월말 결제", 10000, 31, "2026-01-01")).andExpect(status().isCreated());
        mvc.perform(create(memberId, "3월 시작", 5000, 1, "2026-03-01")).andExpect(status().isCreated());

        byte[] file = mvc.perform(get("/api/subs/export").param("month", "2026-02"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();

        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(file))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("월말 결제");
            assertThat(sheet.getRow(1).getCell(1).getStringCellValue()).isEqualTo("2026-02-28");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("합계");
            assertThat(sheet.getRow(2).getCell(2).getNumericCellValue()).isEqualTo(10000);
        }
    }

    @Test
    void createReturnsAndSavesRequestValues() throws Exception {
        Long memberId = saveMember();

        String body = mvc.perform(create(memberId, "  넷플릭스  ", 13500, 31, "2026-11-15"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.serviceName").value("넷플릭스"))
                .andExpect(jsonPath("$.monthlyPrice").value(13500))
                .andExpect(jsonPath("$.billingDay").value(31))
                .andExpect(jsonPath("$.startDate").value("2026-11-15"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();
        long subId = ((Number) JsonPath.read(body, "$.id")).longValue();

        var saved = subRepository.findById(subId).orElseThrow();
        assertThat(saved.getServiceName()).isEqualTo("넷플릭스");
        assertThat(saved.getStartDate()).hasToString("2026-11-15");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCreateBodies")
    void rejectsInvalidCreateRequest(String reason, String json) throws Exception {
        mvc.perform(post("/api/subs").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("입력값 오류"));

        assertThat(subRepository.count()).isZero();
    }

    static Stream<Arguments> invalidCreateBodies() {
        return Stream.of(
                Arguments.of("서비스명 공백", json("{'memberId':1,'serviceName':'  ','monthlyPrice':10000,'billingDay':1,'startDate':'2026-11-01'}")),
                Arguments.of("금액 음수", json("{'memberId':1,'serviceName':'넷플릭스','monthlyPrice':-1,'billingDay':1,'startDate':'2026-11-01'}")),
                Arguments.of("결제일 0", json("{'memberId':1,'serviceName':'넷플릭스','monthlyPrice':10000,'billingDay':0,'startDate':'2026-11-01'}")),
                Arguments.of("결제일 32", json("{'memberId':1,'serviceName':'넷플릭스','monthlyPrice':10000,'billingDay':32,'startDate':'2026-11-01'}")),
                Arguments.of("회원 ID 누락", json("{'serviceName':'넷플릭스','monthlyPrice':10000,'billingDay':1,'startDate':'2026-11-01'}")),
                Arguments.of("시작일 누락", json("{'memberId':1,'serviceName':'넷플릭스','monthlyPrice':10000,'billingDay':1}")));
    }

    @Test
    void deleteUnknownSubscriptionReturnsNotFound() throws Exception {
        mvc.perform(delete("/api/subs/{id}", 99999))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("구독 없음"));
    }

    @Test
    void deleteRejectsNonNumericId() throws Exception {
        mvc.perform(delete("/api/subs/{id}", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 오류"));
    }

    @Test
    void exportSetsExcelDownloadHeaders() throws Exception {
        mvc.perform(get("/api/subs/export").param("month", "2026-02"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=subs-2026-02.xlsx"));
    }

    @Test
    void exportsHeaderAndZeroTotalWhenNoSubscription() throws Exception {
        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(export("2026-02")))) {
            Sheet sheet = workbook.getSheet("2026-02");
            assertThat(sheet).isNotNull();

            Row header = sheet.getRow(0);
            assertThat(header.getCell(0).getStringCellValue()).isEqualTo("서비스명");
            assertThat(header.getCell(1).getStringCellValue()).isEqualTo("결제일");
            assertThat(header.getCell(2).getStringCellValue()).isEqualTo("금액");

            assertThat(sheet.getLastRowNum()).isEqualTo(1);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("합계");
            assertThat(sheet.getRow(1).getCell(2).getNumericCellValue()).isZero();
        }
    }

    @Test
    void exportExcludesDeletedSubscription() throws Exception {
        Long memberId = saveMember();
        createAndGetId(memberId, "넷플릭스", 13500, 5, "2026-01-01");
        long removedId = createAndGetId(memberId, "디즈니플러스", 9900, 10, "2026-01-01");

        mvc.perform(delete("/api/subs/{id}", removedId)).andExpect(status().isNoContent());
        assertThat(subRepository.existsById(removedId)).isFalse();

        try (var workbook = new XSSFWorkbook(new ByteArrayInputStream(export("2026-02")))) {
            Sheet sheet = workbook.getSheetAt(0);
            assertThat(sheet.getLastRowNum()).isEqualTo(2);
            assertThat(sheet.getRow(1).getCell(0).getStringCellValue()).isEqualTo("넷플릭스");
            assertThat(sheet.getRow(2).getCell(0).getStringCellValue()).isEqualTo("합계");
            assertThat(sheet.getRow(2).getCell(2).getNumericCellValue()).isEqualTo(13500);
        }
    }

    @Test
    void exportRequiresMonth() throws Exception {
        mvc.perform(get("/api/subs/export")).andExpect(status().isBadRequest());
    }

    @Test
    void exportRejectsInvalidMonth() throws Exception {
        mvc.perform(get("/api/subs/export").param("month", "2026-13"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("입력값 오류"));
    }

    private long createAndGetId(Long memberId, String name, int price, int billingDay, String startDate)
            throws Exception {
        String body = mvc.perform(create(memberId, name, price, billingDay, startDate))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.id")).longValue();
    }

    private byte[] export(String month) throws Exception {
        return mvc.perform(get("/api/subs/export").param("month", month))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsByteArray();
    }

    // 작은따옴표로 쓴 JSON을 큰따옴표로 바꾼다
    private static String json(String singleQuoted) {
        return singleQuoted.replace('\'', '"');
    }

    private Long saveMember() {
        return memberRepository.save(Member.builder().name("테스트 회원").email("member@example.com").build()).getId();
    }

    private static org.springframework.test.web.servlet.RequestBuilder create(
            Long memberId, String name, int price, int billingDay, String startDate) {
        String json = """
                {"memberId":%d,"serviceName":"%s","monthlyPrice":%d,"billingDay":%d,"startDate":"%s"}
                """.formatted(memberId, name, price, billingDay, startDate);
        return post("/api/subs").contentType(MediaType.APPLICATION_JSON).content(json);
    }
}
