package com.modle.global.init;

import com.modle.domain.application.entity.Application;
import com.modle.domain.application.entity.type.ApplicationStatus;
import com.modle.domain.application.repository.ApplicationRepository;
import com.modle.domain.contract.entity.Contract;
import com.modle.domain.contract.entity.ContractTemplate;
import com.modle.domain.contract.repository.ContractRepository;
import com.modle.domain.contract.repository.ContractTemplateRepository;
import com.modle.domain.profile.entity.Career;
import com.modle.domain.profile.repository.CareerRepository;
import com.modle.domain.jobposting.entity.JobPosting;
import com.modle.domain.jobposting.entity.type.Category;
import com.modle.domain.jobposting.entity.type.JobPostingStatus;
import com.modle.domain.jobposting.entity.type.PayType;
import com.modle.domain.jobposting.entity.type.RequiredSex;
import com.modle.domain.jobposting.repository.JobPostingRepository;
import com.modle.domain.profile.service.ModelService;
import com.modle.domain.user.entity.Client;
import com.modle.domain.user.entity.Model;
import com.modle.domain.user.entity.User;
import com.modle.domain.user.entity.type.ClientType;
import com.modle.domain.user.entity.type.Role;
import com.modle.domain.user.entity.type.Sex;
import com.modle.domain.user.entity.type.UserStatus;
import com.modle.domain.user.repository.ClientRepository;
import com.modle.domain.user.repository.ModelRepository;
import com.modle.domain.user.repository.UserRepository;
import com.modle.global.entity.type.Region;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.modle.domain.user.entity.type.Sex.F;
import static com.modle.domain.user.entity.type.Sex.M;

@Configuration
@RequiredArgsConstructor
public class InitData {
        @Autowired
        @Lazy
        private InitData self;

        private final UserRepository userRepository;
        private final ModelRepository modelRepository;
        private final ClientRepository clientRepository;
        private final BCryptPasswordEncoder passwordEncoder;
        private final ModelService modelService;
        private final JobPostingRepository jobPostingRepository;
        private final ApplicationRepository applicationRepository;
        private final CareerRepository careerRepository;
        private final ContractRepository contractRepository;

        @Bean
        public ApplicationRunner initDataApplicationRunner() {
                return args -> {
                        self.work1(); // 관리자
//                        self.work2(); // 테스트 모델
//                        self.work3(); // 테스트 의뢰인
//                        self.work4(); // 테스트 모델프로필
                        self.work5(); // 테스트 클라이언트프로필
                        self.work6(); // 계약서 템플릿
                        self.work8(); // 추천 테스트용 모델 500개
                        self.work9(); // 시연용 연예인 모델 10명
                        self.work10(); // 시연용 공고 상태/지원 시나리오
                        self.work11(); // 시연용 계약 목록 데이터
                };
        }

        // 관리자 계정 생성
        @Transactional
        public void work1() {
                if (userRepository.existsByEmail("admin@modle.com"))
                        return;

                User admin = User.createLocal(
                                "admin@modle.com",
                                passwordEncoder.encode("admin1234"),
                                "SEOUL",
                                Role.ADMIN);
                admin.updateStatus(UserStatus.ACTIVE);
                userRepository.save(admin);
        }

        // 테스트 모델 계정 생성
        @Transactional
        public void work2() {
                if (userRepository.existsByEmail("model@modle.com"))
                        return;

                User user = User.createLocal(
                                "model@modle.com",
                                passwordEncoder.encode("model1234"),
                                "SEOUL",
                                Role.MODEL);
                userRepository.save(user);

                Model model = Model.create(user, "테스트모델", 170, 55, M, 25);
                modelRepository.save(model);
        }

        // 테스트 의뢰인 계정 생성 (승인된 상태)
        @Transactional
        public void work3() {
                if (userRepository.existsByEmail("client@modle.com"))
                        return;

                User user = User.createLocal(
                                "client@modle.com",
                                passwordEncoder.encode("client1234"),
                                "SEOUL",
                                Role.CLIENT);
                user.updateStatus(UserStatus.ACTIVE);
                userRepository.save(user);

                Client client = Client.create(
                                user,
                                ClientType.INDIVIDUAL,
                                "테스트기업",
                                "123-45-67890");
                clientRepository.save(client);
        }

        @Transactional
        public void work4() {
                if (userRepository.existsByEmail("model1@modle.com")) {
                        return;
                }
                if (modelService.count() > 1) {
                        return;
                }
                User user1 = User.createLocal(
                                "model1@modle.com",
                                passwordEncoder.encode("model1234"),
                                "SEOUL",
                                Role.MODEL);
                userRepository.save(user1);
                Model model1 = modelService.create(user1, "홍길동", 180, 75, M, 25);
                modelService.update(model1, "홍길동", 180, 75, M, 25,
                                List.of("FITTING"), List.of("tag1"),
                                "안녕하세요, 홍길동입니다.",
                                "SEOUL", "", List.of("SEOUL"),
                                0, "L", "M", 270, "월,수,금");

                User user2 = User.createLocal(
                                "model2@modle.com",
                                passwordEncoder.encode("model1234"),
                                "BUSAN",
                                Role.MODEL);
                userRepository.save(user2);
                Model model2 = modelService.create(user2, "김철수", 175, 68, M, 30);
                modelService.update(model2, "김철수", 175, 68, M, 30,
                                List.of("HAIR"), List.of("tag2"),
                                "안녕하세요, 김철수입니다.",
                                "BUSAN", "", List.of("BUSAN"),
                                3, "M", "S", 260, "화,목");

                User user3 = User.createLocal(
                                "model3@modle.com",
                                passwordEncoder.encode("model1234"),
                                "DAEJEON",
                                Role.MODEL);
                userRepository.save(user3);
                Model model3 = modelService.create(user3, "이영희", 165, 55, F, 28);
                modelService.update(model3, "이영희", 165, 55, F, 28,
                                List.of("MAKEUP"), List.of("tag3"),
                                "안녕하세요, 이영희입니다.",
                                "DAEJEON", "", List.of("DAEJEON"),
                                5, "S", "S", 240, "주말");
        }

        @Transactional
        public void work5() {
                if (userRepository.existsByEmail("client1@modle.com")) {
                        return;
                }

                User user1 = User.createLocal(
                                "client1@modle.com",
                                passwordEncoder.encode("client1234"),
                                "SEOUL",
                                Role.CLIENT);
                user1.updateStatus(UserStatus.ACTIVE);
                userRepository.save(user1);
                Client client1 = Client.create(user1, ClientType.ORGANIZATION, "무신사", "111-22-33333");
                client1.update("무신사", "111-22-33333", ClientType.ORGANIZATION, "대한민국 No.1 패션 플랫폼 무신사입니다.",
                                "");
                clientRepository.save(client1);

                User user2 = User.createLocal(
                                "client2@modle.com",
                                passwordEncoder.encode("client1234"),
                                "SEOUL",
                                Role.CLIENT);
                user2.updateStatus(UserStatus.ACTIVE);
                userRepository.save(user2);
                Client client2 = Client.create(user2, ClientType.ORGANIZATION, "지그재그", "222-33-44444");
                client2.update("지그재그", "222-33-44444", ClientType.ORGANIZATION, "나를 찾는 1020 여성 쇼핑앱 지그재그입니다.",
                                "");
                clientRepository.save(client2);

                User user3 = User.createLocal(
                                "client3@modle.com",
                                passwordEncoder.encode("client1234"),
                                "GYEONGGI",
                                Role.CLIENT);
                user3.updateStatus(UserStatus.ACTIVE);
                userRepository.save(user3);
                Client client3 = Client.create(user3, ClientType.INDIVIDUAL, "에이블리", "333-44-55555");
                client3.update("에이블리", "333-44-55555", ClientType.INDIVIDUAL, "내 스타일을 가장 잘 아는 쇼핑몰 에이블리입니다.",
                                "");
                clientRepository.save(client3);
        }


        // 테스트 공고 데이터 생성
        @Transactional
        public void work6() {
                if (jobPostingRepository.count() > 0)
                        return;

                Long client1Id = userRepository.findByEmail("client1@modle.com").map(User::getId).orElse(null);
                Long client2Id = userRepository.findByEmail("client2@modle.com").map(User::getId).orElse(null);
                Long client3Id = userRepository.findByEmail("client3@modle.com").map(User::getId).orElse(null);

                if (client1Id == null || client2Id == null || client3Id == null)
                        return;

                JobPosting job1 = JobPosting.builder()
                                .clientId(client1Id)
                                .title("2025 여름 헤어 화보 모델 모집")
                                .content("무신사 여름 화보 촬영을 위한 헤어 모델을 모집합니다.")
                                .category(Category.HAIR)
                                .region(Region.SEOUL)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.ANY)
                                .requiredCount(2)
                                .ageMin(20)
                                .ageMax(30)
                                .payment(new BigDecimal("500000"))
                                .payType(PayType.CASH)
                                .shootDate(LocalDateTime.of(2025, 8, 10, 10, 0))
                                .build();

                JobPosting job2 = JobPosting.builder()
                                .clientId(client2Id)
                                .title("가을 신상 의류 피팅 모델 모집")
                                .content("지그재그 가을 신상 의류 피팅 촬영 모델을 모집합니다.")
                                .category(Category.FITTING)
                                .region(Region.BUSAN)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.F)
                                .requiredCount(3)
                                .ageMin(20)
                                .ageMax(28)
                                .heightMin(160)
                                .heightMax(170)
                                .payment(new BigDecimal("300000"))
                                .payType(PayType.CASH)
                                .shootDate(LocalDateTime.of(2025, 9, 5, 13, 0))
                                .build();

                JobPosting job3 = JobPosting.builder()
                                .clientId(client3Id)
                                .title("뷰티 메이크업 화보 모델 모집")
                                .content("에이블리 메이크업 신제품 화보 촬영 모델을 모집합니다.")
                                .category(Category.MAKEUP)
                                .region(Region.GYEONGGI)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.ANY)
                                .requiredCount(1)
                                .ageMin(22)
                                .payType(PayType.SERVICE)
                                .shootDate(LocalDateTime.of(2025, 10, 1, 11, 0))
                                .build();

                JobPosting job4 = JobPosting.builder()
                                .clientId(client1Id)
                                .title("가을 신상 의류 룩북 모델 모집")
                                .content("무신사 가을 신상 의류 룩북 촬영을 위한 모델을 모집합니다.")
                                .category(Category.FITTING)
                                .region(Region.DAEGU)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.F)
                                .requiredCount(5)
                                .ageMin(18)
                                .ageMax(25)
                                .heightMin(165)
                                .payment(new BigDecimal("400000"))
                                .payType(PayType.CASH)
                                .shootDate(LocalDateTime.of(2025, 7, 20, 14, 0))
                                .build();

                JobPosting job5 = JobPosting.builder()
                                .clientId(client2Id)
                                .title("핸드크림 신제품 핸드 모델 모집")
                                .content("지그재그 핸드크림 신제품 촬영을 위한 핸드 모델을 모집합니다.")
                                .category(Category.HAND)
                                .region(Region.INCHEON)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.ANY)
                                .requiredCount(2)
                                .payType(PayType.FREE)
                                .shootDate(LocalDateTime.of(2025, 11, 3, 10, 0))
                                .build();

                JobPosting job6 = JobPosting.builder()
                                .clientId(client3Id)
                                .title("신메뉴 음식 화보 모델 모집")
                                .content("에이블리 신메뉴 음식 화보 촬영을 위한 모델을 모집합니다.")
                                .category(Category.FOOD)
                                .region(Region.GWANGJU)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.M)
                                .requiredCount(1)
                                .ageMin(25)
                                .ageMax(35)
                                .payment(new BigDecimal("200000"))
                                .payType(PayType.CASH)
                                .shootDate(LocalDateTime.of(2025, 6, 15, 9, 0))
                                .build();

                JobPosting job7 = JobPosting.builder()
                                .clientId(client1Id)
                                .title("가전제품 광고 모델 모집")
                                .content("무신사 가전제품 광고 촬영을 위한 제품 모델을 모집합니다.")
                                .category(Category.PRODUCT)
                                .region(Region.DAEJEON)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.ANY)
                                .requiredCount(2)
                                .heightMin(170)
                                .heightMax(185)
                                .weightMin(60)
                                .weightMax(75)
                                .payment(new BigDecimal("600000"))
                                .payType(PayType.CASH)
                                .shootDate(LocalDateTime.of(2025, 9, 25, 15, 0))
                                .build();

                JobPosting job8 = JobPosting.builder()
                                .clientId(client2Id)
                                .title("브랜드 홍보 영상 출연 모델 모집")
                                .content("지그재그 브랜드 홍보 영상 출연을 위한 모델을 모집합니다.")
                                .category(Category.ETC)
                                .region(Region.ULSAN)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.F)
                                .requiredCount(4)
                                .minCareerMonths(6)
                                .payType(PayType.SERVICE)
                                .shootDate(LocalDateTime.of(2025, 12, 1, 13, 0))
                                .build();

                JobPosting job9 = JobPosting.builder()
                                .clientId(client3Id)
                                .title("제주 화보 헤어 모델 모집")
                                .content("에이블리 제주 로케이션 화보 촬영을 위한 헤어 모델을 모집합니다.")
                                .category(Category.HAIR)
                                .region(Region.JEJU)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.M)
                                .requiredCount(2)
                                .ageMin(23)
                                .ageMax(29)
                                .payment(new BigDecimal("450000"))
                                .payType(PayType.CASH)
                                .shootDate(LocalDateTime.of(2025, 8, 30, 10, 0))
                                .build();

                JobPosting job10 = JobPosting.builder()
                                .clientId(client1Id)
                                .title("강원 워크웨어 룩북 모델 모집")
                                .content("무신사 워크웨어 룩북 촬영을 위한 모델을 모집합니다.")
                                .category(Category.FITTING)
                                .region(Region.GANGWON)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.ANY)
                                .requiredCount(3)
                                .ageMin(19)
                                .ageMax(26)
                                .heightMin(158)
                                .heightMax(168)
                                .payType(PayType.FREE)
                                .shootDate(LocalDateTime.of(2025, 10, 15, 11, 0))
                                .build();

                jobPostingRepository.save(job1);
                jobPostingRepository.save(job2);
                jobPostingRepository.save(job3);
                jobPostingRepository.save(job4);
                jobPostingRepository.save(job5);
                jobPostingRepository.save(job6);
                jobPostingRepository.save(job7);
                jobPostingRepository.save(job8);
                jobPostingRepository.save(job9);
                jobPostingRepository.save(job10);
        }

        // 추천 테스트용 모델 60개 생성
        @Transactional
        public void work8() {
            if (userRepository.count() > 50) return;

            List<User> newUsers = new ArrayList<>();
            List<Model> newModels = new ArrayList<>();

            for (int i = 1; i <= 60; i++) {
                        String email = "testmodel%03d@modle.com".formatted(i);
                        if (userRepository.existsByEmail(email)) {
                                continue;
                        }

                        Category[] categories = Category.values();
                        Region[] regions = Region.values();
                        Category category = categories[(i - 1) % categories.length];
                        Region region = regions[(i - 1) % regions.length];
                        Sex sex = i % 3 == 0 ? M : F;
                        int age = 19 + (i % 17);
                        int height = 155 + (i % 36);
                        int weight = 45 + (i % 36);
                        Integer experience = i % 5 == 0 ? 0 : (i % 7);
                        List<String> categoryNames = distinctNames(List.of(
                                        category.name(),
                                        categories[i % categories.length].name(),
                                        i % 3 == 0 ? Category.HAIR.name() : category.name(),
                                        i % 5 == 0 ? Category.FITTING.name() : category.name()));
                        List<String> activeRegionNames = distinctNames(List.of(
                                        region.name(),
                                        regions[i % regions.length].name(),
                                        i % 3 == 0 ? Region.SEOUL.name() : region.name(),
                                        i % 5 == 0 ? Region.GYEONGGI.name() : region.name()));

                        User user = User.createLocal(
                                        email,
                                        passwordEncoder.encode("model1234"),
                                        region.getDisplayName(),
                                        Role.MODEL);
                        userRepository.save(user);

                        String name = "테스트모델%03d".formatted(i);
                        Model model = modelService.create(user, name, height, weight, sex, age);
                        modelService.update(
                                        model,
                                        name,
                                        height,
                                        weight,
                                        sex,
                                        age,
                                        categoryNames,
                                        List.of(
                                                        "seed",
                                                        "test",
                                                        category.name().toLowerCase(),
                                                        region.name().toLowerCase()),
                                        "%s 지역의 %s 카테고리 추천 테스트용 모델입니다.".formatted(
                                                        region.getDisplayName(),
                                                        category.name()),
                                        region.getDisplayName(),
                                        "",
                                        activeRegionNames,
                                        experience,
                                        "M", "M", 260, "무관");
                }
        }

        // 시연용 연예인 모델 10명 생성 (성별 5:5, 나이대 20대~70대까지 다양화)
        @Transactional
        public void work9() {
                if (userRepository.existsByEmail("celeb01@modle.com")) {
                        return;
                }

                // 남성 5명 (29 / 33 / 45 / 53 / 59)
                createCelebrity("celeb01@modle.com", "차은우", 183, 70, M, 29, "SEOUL",
                                List.of("FITTING", "HAIR"), List.of("배우", "아이돌", "패션"),
                                "안녕하세요, 모델 차은우입니다.", 7, "L", "M", 270, "주말");
                createCelebrity("celeb02@modle.com", "남주혁", 187, 70, M, 32, "BUSAN",
                                List.of("HAIR", "PRODUCT", "FITTING"), List.of("모델", "배우", "런웨이"),
                                "안녕하세요, 모델 출신 배우 남주혁입니다.", 12, "L", "M", 280, "월,수,금");
                createCelebrity("celeb03@modle.com", "조정석", 173, 68, M, 45, "GYEONGGI",
                                List.of("ETC", "FOOD"), List.of("배우", "예능", "친근"),
                                "다양한 콘셉트 소화 가능한 배우 조정석입니다.", 20, "M", "M", 265, "화,목");
                createCelebrity("celeb04@modle.com", "이정재", 178, 70, M, 53, "SEOUL",
                                List.of("PRODUCT", "ETC"), List.of("배우", "럭셔리", "글로벌"),
                                "안녕하세요, 배우 이정재입니다.", 30, "L", "M", 275, "무관");
                createCelebrity("celeb05@modle.com", "송강호", 180, 80, M, 59, "DAEGU",
                                List.of("FOOD", "ETC"), List.of("배우", "중년", "영화"),
                                "푸근한 이미지의 배우 송강호입니다.", 35, "XL", "L", 280, "무관");

                // 여성 5명 (27 / 33 / 43 / 56 / 79)
                createCelebrity("celeb06@modle.com", "김유정", 165, 47, F, 27, "INCHEON",
                                List.of("MAKEUP", "HAIR"), List.of("배우", "청순", "뷰티"),
                                "안녕하세요, 김유정입니다.", 18, "S", "S", 240, "주말");
                createCelebrity("celeb07@modle.com", "아이유", 162, 45, F, 33, "SEOUL",
                                List.of("MAKEUP", "PRODUCT"), List.of("가수", "배우", "광고"),
                                "안녕하세요, 아이유입니다.", 15, "S", "S", 235, "월,수,금");
                createCelebrity("celeb08@modle.com", "한가인", 168, 48, F, 43, "GYEONGGI",
                                List.of("MAKEUP", "FITTING"), List.of("배우", "우아", "뷰티"),
                                "안녕하세요, 한가인입니다.", 22, "S", "M", 245, "화,목");
                createCelebrity("celeb09@modle.com", "김혜수", 170, 52, F, 56, "SEOUL",
                                List.of("FITTING", "ETC"), List.of("배우", "카리스마", "럭셔리"),
                                "안녕하세요, 배우 김혜수입니다.", 35, "M", "M", 250, "무관");
                createCelebrity("celeb10@modle.com", "윤여정", 160, 50, F, 79, "JEJU",
                                List.of("ETC", "FOOD"), List.of("배우", "시니어", "글로벌"),
                                "안녕하세요, 배우 윤여정입니다.", 50, "S", "M", 240, "무관");
        }

        private void createCelebrity(
                        String email, String name, int height, int weight, Sex sex, int age,
                        String region, List<String> categories, List<String> tags,
                        String introduction, int experience,
                        String topSize, String bottomSize, int shoeSize, String availableDays) {
                if (userRepository.existsByEmail(email)) {
                        return;
                }

                User user = User.createLocal(
                                email,
                                passwordEncoder.encode("model1234"),
                                region,
                                Role.MODEL);
                userRepository.save(user);

                Model model = modelService.create(user, name, height, weight, sex, age);
                modelService.update(
                                model, name, height, weight, sex, age,
                                categories, tags, introduction,
                                region, "", List.of(region),
                                experience, topSize, bottomSize, shoeSize, availableDays);
        }

        // 시연용 공고 상태/지원 시나리오 — 공고 상태와 지원 상태를 짝 맞춰 심는다.
        // requiredSex·연령 범위도 지원 모델의 성별·나이와 일치시켜 정합성을 유지한다.
        @Transactional
        public void work10() {
                // 시나리오 지원 데이터가 이미 있으면 중복 생성 방지
                if (applicationRepository.count() > 0) {
                        return;
                }

                Long client1Id = userRepository.findByEmail("client1@modle.com").map(User::getId).orElse(null);
                Long client2Id = userRepository.findByEmail("client2@modle.com").map(User::getId).orElse(null);
                Long client3Id = userRepository.findByEmail("client3@modle.com").map(User::getId).orElse(null);
                if (client1Id == null || client2Id == null || client3Id == null) {
                        return;
                }

                // 연예인 모델 id 조회
                Long mEunwoo   = modelIdByEmail("celeb01@modle.com"); // 차은우 (M,29)
                Long mJuhyuk   = modelIdByEmail("celeb02@modle.com"); // 남주혁 (M,32)
                Long mJungseok = modelIdByEmail("celeb03@modle.com"); // 조정석 (M,45)
                Long mJungjae  = modelIdByEmail("celeb04@modle.com"); // 이정재 (M,53)
                Long mKangho   = modelIdByEmail("celeb05@modle.com"); // 송강호 (M,59)
                Long mYujung   = modelIdByEmail("celeb06@modle.com"); // 김유정 (F,27)
                Long mIU       = modelIdByEmail("celeb07@modle.com"); // 아이유 (F,33)
                Long mGain     = modelIdByEmail("celeb08@modle.com"); // 한가인 (F,43)
                Long mHyesu    = modelIdByEmail("celeb09@modle.com"); // 김혜수 (F,56)
                Long mYeojung  = modelIdByEmail("celeb10@modle.com"); // 윤여정 (F,79)

                // ── SC1) RECRUITING: 모집 중 · 다양한 지원 상태 ──
                JobPosting sc1 = jobPostingRepository.save(scenarioPosting(
                                client1Id, "2026 S/S 캐주얼 룩북 피팅 모델 모집",
                                "서울 성수 스튜디오에서 진행되는 2026 봄/여름 캐주얼 신상 룩북 촬영 피팅 모델을 모집합니다. "
                                                + "성별 무관, 다양한 체형 환영하며 자연스러운 무드를 표현해주실 분을 찾습니다.",
                                Category.FITTING, Region.SEOUL, JobPostingStatus.RECRUITING,
                                RequiredSex.ANY, 3, 20, 40, LocalDateTime.of(2026, 7, 20, 14, 0)));
                saveApplication(sc1.getId(), mEunwoo, ApplicationStatus.APPLIED, "성실히 임하겠습니다. 차은우입니다.");
                saveApplication(sc1.getId(), mIU, ApplicationStatus.CONTACTED, "촬영 참여 희망합니다. 아이유입니다.");
                saveApplication(sc1.getId(), mJuhyuk, ApplicationStatus.CONTRACT_SENT, "런웨이 경험 많습니다. 남주혁입니다.");
                saveApplication(sc1.getId(), mYujung, ApplicationStatus.APPLICATION_CANCELLED, "일정상 지원 취소합니다. 김유정입니다.");

                // ── SC2) SHOOTING: 촬영 진행중 (선택 1 + 미선택 컨택) ──
                JobPosting sc2 = jobPostingRepository.save(scenarioPosting(
                                client2Id, "프리미엄 가전 브랜드 광고 남성 모델 모집",
                                "부산 해운대 로케이션에서 진행되는 프리미엄 가전 브랜드 광고 촬영 모델을 모집합니다. "
                                                + "중후하고 신뢰감 있는 이미지의 40~50대 남성 모델을 찾습니다.",
                                Category.PRODUCT, Region.BUSAN, JobPostingStatus.SHOOTING,
                                RequiredSex.M, 1, 40, 60, LocalDateTime.of(2026, 6, 25, 10, 0)));
                saveApplication(sc2.getId(), mJungjae, ApplicationStatus.SHOOTING, "최선을 다하겠습니다. 이정재입니다.");
                saveApplication(sc2.getId(), mKangho, ApplicationStatus.CONTACTED, "참여 희망합니다. 송강호입니다.");

                // ── SC3) ON_HOLD: 촬영 보류중 ──
                JobPosting sc3 = jobPostingRepository.save(scenarioPosting(
                                client3Id, "뷰티 브랜드 메이크업 화보 모델 모집",
                                "경기 파주 스튜디오에서 진행되는 뷰티 브랜드 신제품 메이크업 화보 촬영 모델을 모집합니다. "
                                                + "우아하고 분위기 있는 여성 모델을 찾습니다.",
                                Category.MAKEUP, Region.GYEONGGI, JobPostingStatus.ON_HOLD,
                                RequiredSex.F, 1, 30, 50, LocalDateTime.of(2026, 7, 5, 11, 0)));
                Application sc3App = Application.builder()
                                .jobPostingId(sc3.getId()).modelId(mGain)
                                .coverLetter("우아한 콘셉트 자신 있습니다. 한가인입니다.")
                                .status(ApplicationStatus.APPLIED).build();
                sc3App.hold("모델 개인 일정으로 촬영 보류");
                applicationRepository.save(sc3App);

                // ── SC4) COMPLETED: 촬영 완료 (+ Career) ──
                JobPosting sc4 = jobPostingRepository.save(scenarioPosting(
                                client1Id, "라이프스타일 브랜드 시즌 캠페인 모델 모집",
                                "서울 강남 스튜디오에서 진행되는 라이프스타일 브랜드 시즌 캠페인 영상·화보 촬영 모델을 모집합니다. "
                                                + "품격 있고 세련된 분위기의 여성 모델을 찾습니다.",
                                Category.ETC, Region.SEOUL, JobPostingStatus.COMPLETED,
                                RequiredSex.F, 1, 40, 70, LocalDateTime.of(2026, 5, 10, 13, 0)));
                saveApplication(sc4.getId(), mHyesu, ApplicationStatus.COMPLETED, "끝까지 책임지고 촬영했습니다. 김혜수입니다.");
                addCareer(mHyesu, sc4);

                // ── SC5) CANCELLED: 촬영 취소 ──
                JobPosting sc5 = jobPostingRepository.save(scenarioPosting(
                                client2Id, "제주 향토음식 브랜드 화보 모델 모집",
                                "제주 서귀포 로케이션에서 진행되는 향토음식 브랜드 화보 촬영 모델을 모집합니다. "
                                                + "따뜻하고 정감 있는 시니어 여성 모델을 찾습니다.",
                                Category.FOOD, Region.JEJU, JobPostingStatus.CANCELLED,
                                RequiredSex.F, 1, 60, 90, LocalDateTime.of(2026, 5, 20, 9, 0)));
                Application sc5App = Application.builder()
                                .jobPostingId(sc5.getId()).modelId(mYeojung)
                                .coverLetter("좋은 작품 만들고 싶습니다. 윤여정입니다.")
                                .status(ApplicationStatus.SHOOTING).build();
                sc5App.cancelShooting("현장 사정으로 촬영이 취소되었습니다.");
                applicationRepository.save(sc5App);

                // ── SC6) CLOSED: 마감(완료 인원 충족) (+ Career) ──
                JobPosting sc6 = jobPostingRepository.save(scenarioPosting(
                                client3Id, "헤어케어 브랜드 광고 남성 모델 모집",
                                "인천 송도 스튜디오에서 진행되는 헤어케어 브랜드 광고 촬영 모델을 모집합니다. "
                                                + "자연스럽고 건강한 이미지의 중년 남성 모델을 찾습니다.",
                                Category.HAIR, Region.INCHEON, JobPostingStatus.CLOSED,
                                RequiredSex.M, 1, 40, 60, LocalDateTime.of(2026, 4, 30, 15, 0)));
                saveApplication(sc6.getId(), mJungseok, ApplicationStatus.COMPLETED, "즐겁게 촬영했습니다. 조정석입니다.");
                addCareer(mJungseok, sc6);
        }

        private JobPosting scenarioPosting(
                        Long clientId, String title, String content,
                        Category category, Region region, JobPostingStatus status,
                        RequiredSex requiredSex, int requiredCount, int ageMin, int ageMax,
                        LocalDateTime shootDate) {
                return JobPosting.builder()
                                .clientId(clientId)
                                .title(title)
                                .content(content)
                                .category(category)
                                .region(region)
                                .status(status)
                                .requiredSex(requiredSex)
                                .requiredCount(requiredCount)
                                .ageMin(ageMin)
                                .ageMax(ageMax)
                                .payment(new BigDecimal("500000"))
                                .payType(PayType.CASH)
                                .shootDate(shootDate)
                                .build();
        }

        private void saveApplication(Long jobPostingId, Long modelId, ApplicationStatus status, String coverLetter) {
                if (jobPostingId == null || modelId == null) {
                        return;
                }
                applicationRepository.save(Application.builder()
                                .jobPostingId(jobPostingId)
                                .modelId(modelId)
                                .coverLetter(coverLetter)
                                .status(status)
                                .build());
        }

        private void addCareer(Long modelId, JobPosting jobPosting) {
                if (modelId == null) {
                        return;
                }
                Career career = Career.createFromJobPosting(
                                modelId,
                                jobPosting.getId(),
                                jobPosting.getTitle(),
                                jobPosting.getCategory().name(),
                                jobPosting.getRegion().name(),
                                jobPosting.getShootDate(),
                                LocalDateTime.now());
                careerRepository.save(career);
        }

        private Long modelIdByEmail(String email) {
                return userRepository.findByEmail(email)
                                .flatMap(user -> modelRepository.findByUserId(user.getId()))
                                .map(Model::getId)
                                .orElse(null);
        }

        private List<String> distinctNames(List<String> names) {
                List<String> result = new ArrayList<>();
                for (String name : names) {
                        if (!result.contains(name)) {
                                result.add(name);
                        }
                }
                return result;
        }

        // 시연용 계약 목록 데이터 — model@modle.com 모델과 client@modle.com 의뢰인에게
        // 계약을 몰아, 양쪽 계약 목록(진행중·완료·취소)을 모두 채운다.
        // 로그인: model@modle.com / model1234 (모델), client@modle.com / client1234 (기업)
        @Transactional
        public void work11() {
                if (contractRepository.count() > 0) {
                        return;
                }

                // 계약 시연용 의뢰인 (client@modle.com)
                Long clientUserId;
                if (userRepository.existsByEmail("client@modle.com")) {
                        clientUserId = userRepository.findByEmail("client@modle.com").map(User::getId).orElse(null);
                } else {
                        User clientUser = User.createLocal(
                                        "client@modle.com",
                                        passwordEncoder.encode("client1234"),
                                        "SEOUL",
                                        Role.CLIENT);
                        clientUser.updateStatus(UserStatus.ACTIVE);
                        userRepository.save(clientUser);
                        clientRepository.save(Client.create(
                                        clientUser, ClientType.ORGANIZATION, "테스트기업", "123-45-67890"));
                        clientUserId = clientUser.getId();
                }

                // 계약 시연용 모델 (model@modle.com)
                Long modelId;
                if (userRepository.existsByEmail("model@modle.com")) {
                        modelId = modelIdByEmail("model@modle.com");
                } else {
                        User modelUser = User.createLocal(
                                        "model@modle.com",
                                        passwordEncoder.encode("model1234"),
                                        "SEOUL",
                                        Role.MODEL);
                        userRepository.save(modelUser);
                        modelId = modelService.create(modelUser, "테스트모델", 178, 68, M, 28).getId();
                }

                if (clientUserId == null || modelId == null) {
                        return;
                }

                // ── 진행중 4 / 완료 2 / 취소 3 = 9건 (전부 client@modle.com 소유 공고) ──
                seedContract(savePosting(clientUserId, "시연 계약 01 - 진행중(컨택)", Category.FITTING, Region.SEOUL),
                                modelId, ApplicationStatus.CONTACTED, "지원합니다 01", "VIEWED");
                seedContract(savePosting(clientUserId, "시연 계약 02 - 진행중(발송)", Category.HAIR, Region.BUSAN),
                                modelId, ApplicationStatus.CONTRACT_SENT, "지원합니다 02", "NOTIFIED");
                seedContract(savePosting(clientUserId, "시연 계약 03 - 진행중(촬영)", Category.PRODUCT, Region.INCHEON),
                                modelId, ApplicationStatus.SHOOTING, "지원합니다 03", "CONFIRMED");
                seedContract(savePosting(clientUserId, "시연 계약 04 - 진행중(보류)", Category.MAKEUP, Region.GYEONGGI),
                                modelId, ApplicationStatus.ON_HOLD, "지원합니다 04", "VIEWED");

                seedContract(savePosting(clientUserId, "시연 계약 05 - 완료", Category.ETC, Region.SEOUL),
                                modelId, ApplicationStatus.COMPLETED, "지원합니다 05", "CONFIRMED");
                seedContract(savePosting(clientUserId, "시연 계약 06 - 완료", Category.FOOD, Region.DAEGU),
                                modelId, ApplicationStatus.COMPLETED, "지원합니다 06", "CONFIRMED");

                seedContract(savePosting(clientUserId, "시연 계약 07 - 취소(반려)", Category.HAND, Region.GWANGJU),
                                modelId, ApplicationStatus.CONTRACT_SENT, "지원합니다 07", "REJECTED");
                seedContract(savePosting(clientUserId, "시연 계약 08 - 취소(반려)", Category.FITTING, Region.JEJU),
                                modelId, ApplicationStatus.CONTRACT_SENT, "지원합니다 08", "REJECTED");
                seedContract(savePosting(clientUserId, "시연 계약 09 - 취소(촬영취소)", Category.PRODUCT, Region.DAEJEON),
                                modelId, ApplicationStatus.SHOOTING_CANCELLED, "지원합니다 09", "CONFIRMED");
        }

        // 시연 계약용 공고 1건 저장 (client@modle.com 소유)
        private JobPosting savePosting(Long clientUserId, String title, Category category, Region region) {
                return jobPostingRepository.save(JobPosting.builder()
                                .clientId(clientUserId)
                                .title(title)
                                .content(title + " 촬영 모델을 모집합니다. (시연용)")
                                .category(category)
                                .region(region)
                                .status(JobPostingStatus.RECRUITING)
                                .requiredSex(RequiredSex.ANY)
                                .requiredCount(1)
                                .ageMin(20)
                                .ageMax(40)
                                .payment(new BigDecimal("500000"))
                                .payType(PayType.CASH)
                                .shootDate(LocalDateTime.of(2026, 8, 1, 10, 0))
                                .build());
        }

        // 지원(Application) 1건 저장 후, 그 지원에 연결된 계약을 phase 상태로 만들어 저장한다.
        private void seedContract(
                        JobPosting jobPosting,
                        Long modelId,
                        ApplicationStatus applicationStatus,
                        String coverLetter,
                        String phase) {
                Application application = applicationRepository.save(Application.builder()
                                .jobPostingId(jobPosting.getId())
                                .modelId(modelId)
                                .coverLetter(coverLetter)
                                .status(applicationStatus)
                                .build());

                LocalDateTime t = LocalDateTime.of(2026, 6, 1, 10, 0);
                Contract contract = Contract.createDraft(
                                application.getId(),
                                com.modle.domain.contract.entity.type.ContractType.TEMPLATE,
                                LocalDateTime.of(2026, 8, 1, 10, 0),
                                LocalDateTime.of(2026, 8, 1, 18, 0),
                                "촬영 스튜디오 (" + jobPosting.getRegion().name() + ")",
                                new BigDecimal("500000"),
                                com.modle.domain.contract.entity.type.PayType.CASH,
                                "온라인/오프라인 광고 3개월",
                                "시연용 더미 계약서",
                                "https://example.com/contract/" + application.getId() + ".pdf");

                switch (phase) {
                        case "NOTIFIED" -> contract.notifyModel(t);
                        case "VIEWED" -> {
                                contract.notifyModel(t);
                                contract.markViewedAt(t);
                        }
                        case "CONFIRMED" -> {
                                contract.notifyModel(t);
                                contract.markViewedAt(t);
                                contract.clientAgree(t, "127.0.0.1");
                                contract.modelAgree(t, "127.0.0.1");
                                contract.confirm(
                                                "https://example.com/contract/" + application.getId() + "-signed.pdf",
                                                t);
                        }
                        case "REJECTED" -> {
                                contract.notifyModel(t);
                                contract.markViewedAt(t);
                                contract.reject("일정이 맞지 않아 반려합니다.");
                        }
                        default -> {
                                // DRAFT: 별도 전환 없음
                        }
                }

                contractRepository.save(contract);
        }

}
