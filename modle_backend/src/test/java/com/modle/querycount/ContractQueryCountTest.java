package com.modle.querycount;

import com.modle.domain.application.entity.Application;
import com.modle.domain.application.entity.type.ApplicationStatus;
import com.modle.domain.contract.dto.response.ContractListItemResponse;
import com.modle.domain.contract.entity.Contract;
import com.modle.domain.contract.entity.type.ContractListStatus;
import com.modle.domain.contract.entity.type.ContractType;
import com.modle.domain.contract.entity.type.PayType;
import com.modle.domain.contract.service.ContractQueryService;
import com.modle.domain.jobposting.entity.JobPosting;
import com.modle.domain.jobposting.entity.type.Category;
import com.modle.domain.jobposting.entity.type.JobPostingStatus;
import com.modle.domain.jobposting.service.AiRecommendService;
import com.modle.domain.jobposting.service.JobPostingService;
import com.modle.domain.message.service.MessageService;
import com.modle.domain.profile.service.ClientService;
import com.modle.domain.user.entity.Client;
import com.modle.domain.user.entity.Model;
import com.modle.domain.user.entity.User;
import com.modle.domain.user.entity.type.ClientType;
import com.modle.domain.user.entity.type.Role;
import com.modle.domain.user.entity.type.Sex;
import com.modle.domain.user.service.AuthTokenService;
import com.modle.domain.user.service.EmailVerifyService;
import com.modle.domain.user.service.UserService;
import com.modle.global.auth.JwtTokenProvider;
import com.modle.global.entity.type.Region;
import com.modle.global.gcs.GcsService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 모델 계약 목록 조회(getContracts(MODEL))가 실제로 내보내는 SQL 개수와 소요 시간을 측정한다.
 * Hibernate StatementInspector로 SQL을 한 줄씩 수집하므로 로그 눈대중이 아니라 정확한 개수다.
 */
@DataJpaTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:qc;MODE=MySQL;NON_KEYWORDS=USER,VALUE;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "logging.level.org.hibernate=WARN",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector=com.modle.querycount.SqlCollector"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ContractQueryService.class, JobPostingService.class, UserService.class, ClientService.class})
class ContractQueryCountTest {

    @MockitoBean MessageService messageService;
    @MockitoBean GcsService gcsService;
    @MockitoBean BCryptPasswordEncoder passwordEncoder;
    @MockitoBean AuthTokenService authTokenService;
    @MockitoBean EmailVerifyService emailVerifyService;
    @MockitoBean JwtTokenProvider jwtTokenProvider;
    @MockitoBean AiRecommendService aiRecommendService;

    @Autowired ContractQueryService contractQueryService;
    @Autowired EntityManager em;

    private static final Pattern FROM = Pattern.compile("\\bfrom\\s+(\\w+)", Pattern.CASE_INSENSITIVE);
    private static final int WARMUP = 3;
    private static final int RUNS = 10;

    // n = 계약 수, clients = 계약에 걸린 서로 다른 기업 수 (n이면 전부 다른 기업 = 최악, 1이면 전부 같은 기업 = 최선)
    @ParameterizedTest(name = "contracts={0}, distinctClients={1}")
    @CsvSource({"1,1", "10,10", "10,1", "100,100", "100,1"})
    void measureModelContractList(int n, int clients) throws IOException {
        LocalDateTime now = LocalDateTime.now();

        User modelUser = User.createLocal("model@x.com", "pw", "SEOUL", Role.MODEL);
        em.persist(modelUser);
        Model model = Model.create(modelUser, "모델", 170, 50, Sex.F, 25);
        em.persist(model);

        List<User> clientUsers = new ArrayList<>();
        for (int i = 0; i < clients; i++) {
            User cu = User.createLocal("client" + i + "@x.com", "pw", "SEOUL", Role.CLIENT);
            em.persist(cu);
            em.persist(Client.create(cu, ClientType.ORGANIZATION, "회사" + i, "123-45-6789" + i));
            clientUsers.add(cu);
        }

        for (int i = 0; i < n; i++) {
            JobPosting jp = JobPosting.builder()
                    .clientId(clientUsers.get(i % clients).getId())
                    .title("공고" + i).content("내용").category(Category.HAIR)
                    .region(Region.SEOUL).status(JobPostingStatus.RECRUITING)
                    .build();
            em.persist(jp);
            Application a = Application.builder()
                    .jobPostingId(jp.getId()).modelId(model.getId())
                    .status(ApplicationStatus.CONTRACT_SENT)
                    .build();
            em.persist(a);
            Contract c = Contract.createDraft(a.getId(), ContractType.TEMPLATE, now, now.plusHours(2),
                    "서울", BigDecimal.TEN, PayType.CASH, "SNS", null, null);
            c.notifyModel(now);
            em.persist(c);
        }
        em.flush();

        // 워밍업 (JIT, 커넥션, 쿼리플랜 캐시) — 매 호출 전 em.clear()로 새 요청처럼 빈 영속성 컨텍스트에서 시작
        for (int i = 0; i < WARMUP; i++) {
            em.clear();
            contractQueryService.getContracts(modelUser.getId(), "MODEL", ContractListStatus.ONGOING);
        }

        // 본 측정: 쿼리 개수는 첫 회에서 수집, 시간은 RUNS회 중앙값
        long[] elapsedMs = new long[RUNS];
        List<String> sqls = null;
        List<ContractListItemResponse> result = null;
        for (int i = 0; i < RUNS; i++) {
            em.clear();
            SqlCollector.SQL.clear();
            long t0 = System.nanoTime();
            result = contractQueryService.getContracts(modelUser.getId(), "MODEL", ContractListStatus.ONGOING);
            elapsedMs[i] = (System.nanoTime() - t0) / 1_000_000;
            if (sqls == null) sqls = new ArrayList<>(SqlCollector.SQL);
        }
        assertThat(result).hasSize(n);
        Arrays.sort(elapsedMs);
        long median = elapsedMs[RUNS / 2];

        Map<String, Integer> byTable = new LinkedHashMap<>();
        for (String sql : sqls) {
            Matcher m = FROM.matcher(sql);
            byTable.merge(m.find() ? m.group(1) : "?", 1, Integer::sum);
        }

        String label = System.getProperty("qc.label", "?");
        String line = String.format("%s | N=%d distinctClients=%d | queries=%d | medianMs=%d | minMs=%d | maxMs=%d | byTable=%s%n",
                label, n, clients, sqls.size(), median, elapsedMs[0], elapsedMs[RUNS - 1], byTable);
        Files.writeString(Path.of("qc-result.txt"), line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        // 실행된 SQL 전문 (근거용)
        StringBuilder sb = new StringBuilder();
        sb.append("### ").append(label).append(" N=").append(n).append(" distinctClients=").append(clients)
          .append(" -> ").append(sqls.size()).append(" queries\n");
        int i = 1;
        for (String sql : sqls) sb.append(String.format("%3d. %s%n", i++, sql.replaceAll("\\s+", " ")));
        sb.append('\n');
        Files.writeString(Path.of("qc-sql-" + label + ".log"), sb.toString(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
