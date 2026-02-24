
-- ======== I. BẢNG CỐT LÕI: NGƯỜI DÙNG & VAI TRÒ ========

-- Bảng trung tâm lưu trữ thông tin đăng nhập và thông tin cơ bản
CREATE TABLE users (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100),
                       phone_number VARCHAR(20) UNIQUE,
                       profile_picture_url VARCHAR(255),
                       created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng lưu trữ vai trò Admin
CREATE TABLE admins (
                        user_id INT PRIMARY KEY,
                        FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng lưu trữ vai trò Người Chơi (Player)
CREATE TABLE players (
                         user_id INT PRIMARY KEY,
                         current_elo INT DEFAULT 1000,
    -- JSkills rating system
                         rating_mu DOUBLE PRECISION DEFAULT 25.0,
                         rating_sigma DOUBLE PRECISION DEFAULT 8.333,
                         loyalty_points INT DEFAULT 0,
                         loyalty_tier ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM') DEFAULT 'BRONZE',
                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng lưu trữ vai trò Chủ Sân (Venue Owner)
CREATE TABLE venue_owners (
                              user_id INT PRIMARY KEY,
                              tax_code VARCHAR(50), -- MST
                              bank_account_number VARCHAR(100),
                              bank_name VARCHAR(100),
                              FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- ======== II. BẢNG NGHIỆP VỤ SÂN (VENUE) ========

-- Bảng lưu thông tin các Sân Pickleball
CREATE TABLE venues (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        owner_id INT NOT NULL, -- FK trỏ đến user_id của Chủ Sân
                        name VARCHAR(255) NOT NULL,
                        address TEXT NOT NULL,
                        latitude DECIMAL(10, 8),
                        longitude DECIMAL(11, 8),
                        description TEXT,
                        amenities TEXT, -- Tiện ích: wifi, parking, etc. (stored as JSON string)
                        is_active BOOLEAN DEFAULT true,
                        approved_by_admin_id INT, -- Admin duyệt sân
                        approved_at DATETIME,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (owner_id) REFERENCES venue_owners(user_id),
                        FOREIGN KEY (approved_by_admin_id) REFERENCES admins(user_id)
);

-- Bảng lưu thông tin các sân con (court) trong một Sân lớn (venue)
CREATE TABLE courts (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        venue_id INT NOT NULL,
                        court_name VARCHAR(100), -- Ví dụ: "Sân 1", "Sân 2"
                        is_active BOOLEAN DEFAULT true,
                        FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
);

-- Bảng giá linh hoạt cho từng sân (theo giờ, theo ngày)
CREATE TABLE court_pricing (
                               id INT PRIMARY KEY AUTO_INCREMENT,
                               court_id INT NOT NULL,
                               day_of_week INT, -- 0 = Chủ Nhật, 1 = Thứ Hai, ... (NULL nếu áp dụng mọi ngày)
                               start_time TIME NOT NULL,
                               end_time TIME NOT NULL,
                               price_per_hour DECIMAL(10, 2) NOT NULL,
                               FOREIGN KEY (court_id) REFERENCES courts(id) ON DELETE CASCADE
);

-- ======== III. BẢNG TRỌNG TÀI & NHÂN VIÊN ========

-- Bảng lưu trữ vai trò Trọng Tài (Referee) - Hỗ trợ mô hình Hybrid
CREATE TABLE referees (
                          user_id INT PRIMARY KEY,
                          test_passed BOOLEAN DEFAULT false,
                          test_score DECIMAL(5, 2),
    -- 'PLATFORM' (Nền tảng) hoặc 'VENUE' (Trọng tài Sân)
                          referee_type ENUM('PLATFORM', 'VENUE') NOT NULL,
    -- Nếu là 'VENUE', trường này sẽ trỏ đến Sân mà họ làm việc
                          works_at_venue_id INT NULL,
                          approved_by_admin_id INT, -- Admin duyệt trọng tài
                          approved_at DATETIME,
                          is_active BOOLEAN DEFAULT true,
                          FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                          FOREIGN KEY (works_at_venue_id) REFERENCES venues(id) ON DELETE SET NULL,
                          FOREIGN KEY (approved_by_admin_id) REFERENCES admins(user_id)
);

-- Bảng lưu trữ tài khoản Phụ (Nhân viên Lễ tân)
CREATE TABLE venue_staff (
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             venue_id INT NOT NULL, -- Sân mà nhân viên này thuộc về
                             username VARCHAR(100) UNIQUE NOT NULL,
                             password_hash VARCHAR(255) NOT NULL,
                             full_name VARCHAR(100),
                             is_active BOOLEAN DEFAULT true,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE
);

-- Bảng phân quyền cho Nhân viên Lễ tân
CREATE TABLE venue_staff_permissions (
                                         staff_id INT NOT NULL,
                                         permission_key VARCHAR(50) NOT NULL, -- Ví dụ: 'CAN_CREATE_BOOKING', 'CAN_CHECK_IN', 'CANNOT_SEE_REVENUE'
                                         PRIMARY KEY (staff_id, permission_key),
                                         FOREIGN KEY (staff_id) REFERENCES venue_staff(id) ON DELETE CASCADE
);

-- ======== IV. BẢNG ĐĂNG KÝ & XÁC THỰC ========

-- Bảng lưu trạng thái đăng ký chủ sân/trọng tài
CREATE TABLE registration_requests (
                                       id INT PRIMARY KEY AUTO_INCREMENT,
                                       user_id INT NOT NULL,
                                       request_type ENUM('VENUE_OWNER', 'PLATFORM_REFEREE', 'VENUE_REFEREE') NOT NULL,
                                       venue_id INT NULL, -- Cho VENUE_REFEREE
                                       legal_info JSON, -- Lưu MST, tên sân, địa chỉ, bank account...
                                       test_score DECIMAL(5,2),
                                       status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
                                       submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                       processed_by_admin_id INT NULL,
                                       processed_at DATETIME,
                                       notes TEXT, -- Ghi chú từ admin
                                       FOREIGN KEY (user_id) REFERENCES users(id),
                                       FOREIGN KEY (venue_id) REFERENCES venues(id),
                                       FOREIGN KEY (processed_by_admin_id) REFERENCES admins(user_id)
);

-- ======== V. BẢNG MÙA GIẢI (SEASONS) ========

-- Bảng quản lý mùa giải cho ranking
CREATE TABLE seasons (
                         id INT PRIMARY KEY AUTO_INCREMENT,
                         name VARCHAR(100) NOT NULL, -- "Mùa 1 2025"
                         start_date DATE NOT NULL,
                         end_date DATE NOT NULL,
                         is_active BOOLEAN DEFAULT false,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ======== VI. BẢNG ĐẶT SÂN & TRẬN ĐẤU (BOOKING & MATCH) ========

-- Bảng trung tâm lưu trữ mọi lượt đặt sân
CREATE TABLE bookings (
                          id INT PRIMARY KEY AUTO_INCREMENT,
                          court_id INT NOT NULL,
                          start_time DATETIME NOT NULL,
                          end_time DATETIME NOT NULL,
    -- 'PRIVATE', 'CASUAL', 'RANKED', 'WALK_IN' (4 luồng logic)
                          booking_type ENUM('PRIVATE', 'CASUAL', 'RANKED', 'WALK_IN') NOT NULL,
    -- 'PENDING' (Chờ ghép), 'CONFIRMED' (Đủ người), 'COMPLETED', 'CANCELLED'
                          status ENUM('PENDING', 'CONFIRMED', 'COMPLETED', 'CANCELLED') NOT NULL,

    -- Người tạo booking
                          created_by_player_id INT, -- FK trỏ đến users(id) nếu là Player đặt
                          created_by_staff_id INT, -- FK trỏ đến venue_staff(id) nếu là Lễ tân đặt

    -- Lưu trữ chi phí tính toán
                          venue_fee DECIMAL(10, 2), -- Tiền sân
                          referee_fee DECIMAL(10, 2), -- Phí trọng tài (nếu là RANKED)
                          platform_fee DECIMAL(10, 2), -- Phí nền tảng (hoa hồng)
                          total_cost DECIMAL(10, 2), -- Tổng = venue + referee + platform

                          created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                          FOREIGN KEY (court_id) REFERENCES courts(id),
                          FOREIGN KEY (created_by_player_id) REFERENCES users(id),
                          FOREIGN KEY (created_by_staff_id) REFERENCES venue_staff(id),

    -- Constraint đảm bảo có người tạo
                          CONSTRAINT chk_booking_creator CHECK (created_by_player_id IS NOT NULL OR created_by_staff_id IS NOT NULL)
);

-- Bảng "junction" (nhiều-nhiều) lưu ai tham gia booking nào
CREATE TABLE booking_participants (
                                      id INT PRIMARY KEY AUTO_INCREMENT,
                                      booking_id INT NOT NULL,
                                      user_id INT NOT NULL, -- FK trỏ đến users(id) (cho cả Player và Referee)
    -- 'HOST', 'PLAYER' (khách mời), 'REFEREE'
                                      role ENUM('HOST', 'PLAYER', 'REFEREE') NOT NULL,
    -- 'A' hoặc 'B' (cho trận RANKED), NULL cho các loại khác
                                      team CHAR(1) NULL,
    -- 'PENDING', 'PAID' (đã cọc), 'FORFEITED' (bùng kèo), 'CHECKED_IN'
                                      join_status ENUM('PENDING', 'PAID', 'FORFEITED', 'CHECKED_IN') DEFAULT 'PENDING',

    -- Thêm các trường tài chính
                                      deposit_amount DECIMAL(10,2) DEFAULT 0,
                                      actual_payment_amount DECIMAL(10,2),
                                      refund_amount DECIMAL(10,2) DEFAULT 0,
                                      is_match_host BOOLEAN DEFAULT false, -- Xác định ai là host cho trận ghép

                                      UNIQUE (booking_id, user_id), -- Mỗi người chỉ tham gia 1 lần / 1 booking
                                      FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
                                      FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng lưu chi tiết kết quả trận đấu Xếp hạng
CREATE TABLE ranked_matches (
                                id INT PRIMARY KEY AUTO_INCREMENT,
                                booking_id INT NOT NULL UNIQUE, -- 1 booking RANKED chỉ có 1 kết quả
                                referee_id INT NOT NULL, -- Ghi lại ai là người bắt
                                season_id INT, -- Mùa giải của trận đấu

    -- 'PENDING' (chưa có kết quả), 'SUBMITTED' (trọng tài gửi), 'CONFIRMED' (người chơi ok), 'IN_DISPUTE' (khiếu nại)
                                status ENUM('PENDING', 'SUBMITTED', 'CONFIRMED', 'IN_DISPUTE', 'RESOLVED') DEFAULT 'PENDING',

                                team_a_score INT,
                                team_b_score INT,
                                winning_team CHAR(1) NULL, -- 'A' hoặc 'B'

                                submitted_at DATETIME, -- Thời điểm trọng tài gửi kết quả
                                confirmed_at DATETIME, -- Thời điểm người chơi cuối cùng xác nhận

                                FOREIGN KEY (booking_id) REFERENCES bookings(id) ON DELETE CASCADE,
                                FOREIGN KEY (referee_id) REFERENCES referees(user_id),
                                FOREIGN KEY (season_id) REFERENCES seasons(id),

    -- Constraint đảm bảo điểm hợp lệ
                                CONSTRAINT chk_team_scores CHECK (
                                    (team_a_score IS NOT NULL AND team_b_score IS NOT NULL) OR
                                    status = 'PENDING'
                                    )
);

-- ======== VII. BẢNG CHECK-IN & THAM DỰ ========

-- Bảng quản lý check-in
CREATE TABLE check_ins (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           booking_id INT NOT NULL,
                           user_id INT NOT NULL, -- Player hoặc Referee
                           check_in_method ENUM('GPS', 'QR_CODE') NOT NULL,
                           check_in_time DATETIME NOT NULL,
                           latitude DECIMAL(10, 8),
                           longitude DECIMAL(11, 8),
                           FOREIGN KEY (booking_id) REFERENCES bookings(id),
                           FOREIGN KEY (user_id) REFERENCES users(id),
                           UNIQUE KEY unique_booking_user (booking_id, user_id)
);

-- ======== VIII. BẢNG KHIẾU NẠI ========

-- Bảng xử lý Khiếu nại
CREATE TABLE match_disputes (
                                id INT PRIMARY KEY AUTO_INCREMENT,
                                ranked_match_id INT NOT NULL,
                                reporting_player_id INT NOT NULL, -- Người chơi khiếu nại
                                reason TEXT NOT NULL,
                                evidence JSON, -- Multiple evidence files (ảnh, video)
                                referee_evidence_url VARCHAR(255), -- Link bằng chứng trọng tài gửi

    -- 'OPEN', 'AWAITING_EVIDENCE', 'IN_REVIEW', 'RESOLVED'
                                status ENUM('OPEN', 'AWAITING_EVIDENCE', 'IN_REVIEW', 'RESOLVED') DEFAULT 'OPEN',

                                resolved_by_admin_id INT, -- Admin nào xử lý
                                admin_decision TEXT, -- Quyết định cuối cùng
                                resolved_at DATETIME,

                                FOREIGN KEY (ranked_match_id) REFERENCES ranked_matches(id),
                                FOREIGN KEY (reporting_player_id) REFERENCES users(id),
                                FOREIGN KEY (resolved_by_admin_id) REFERENCES admins(user_id)
);

-- ======== IX. BẢNG LỊCH SỬ ELO & SKILL RATING ========

-- Bảng lịch sử thay đổi ELO
CREATE TABLE elo_history (
                             id INT PRIMARY KEY AUTO_INCREMENT,
                             user_id INT NOT NULL,
                             ranked_match_id INT NOT NULL,
                             season_id INT, -- Mùa giải
                             elo_before INT NOT NULL,
                             elo_change INT NOT NULL, -- Có thể là số âm
                             elo_after INT NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                             FOREIGN KEY (user_id) REFERENCES players(user_id),
                             FOREIGN KEY (ranked_match_id) REFERENCES ranked_matches(id),
                             FOREIGN KEY (season_id) REFERENCES seasons(id)
);

-- Bảng lịch sử skill rating chi tiết (cho JSkills)
CREATE TABLE skill_rating_history (
                                      id INT PRIMARY KEY AUTO_INCREMENT,
                                      player_id INT NOT NULL,
                                      match_id INT NOT NULL,
                                      season_id INT,
                                      mu_before DOUBLE PRECISION,
                                      sigma_before DOUBLE PRECISION,
                                      mu_after DOUBLE PRECISION,
                                      sigma_after DOUBLE PRECISION,
                                      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                      FOREIGN KEY (player_id) REFERENCES players(user_id),
                                      FOREIGN KEY (match_id) REFERENCES ranked_matches(id),
                                      FOREIGN KEY (season_id) REFERENCES seasons(id)
);

-- ======== X. BẢNG TÀI CHÍNH (FINANCIAL) ========

-- Bảng quản lý Ví của người dùng
CREATE TABLE wallets (
                         user_id INT PRIMARY KEY,
                         balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
                         updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Bảng ghi lại mọi giao dịch (cọc, thanh toán, nạp tiền...)
CREATE TABLE transactions (
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              user_id INT NOT NULL, -- Người thực hiện giao dịch
                              booking_id INT, -- Giao dịch này liên quan đến booking nào (nếu có)
                              amount DECIMAL(10, 2) NOT NULL, -- Số tiền

    -- 'DEPOSIT' (Tiền cọc), 'BOOKING_PAYMENT', 'TOP_UP' (Nạp tiền vào ví), 'WITHDRAWAL', 'REFUND', 'PENALTY' (Tiền phạt bùng kèo)
                              type ENUM('DEPOSIT', 'BOOKING_PAYMENT', 'TOP_UP', 'WITHDRAWAL', 'REFUND', 'PENALTY') NOT NULL,
    -- 'PENDING', 'SUCCESS', 'FAILED'
                              status ENUM('PENDING', 'SUCCESS', 'FAILED') NOT NULL,
                              payment_method VARCHAR(50), -- Ví dụ: 'VNPAY', 'MOMO', 'WALLET'
                              transaction_code VARCHAR(255), -- Mã giao dịch từ bên thứ 3
                              description VARCHAR(255),
                              metadata JSON, -- Lưu thông tin bổ sung
                              created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (user_id) REFERENCES users(id),
                              FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Bảng quản lý việc thanh toán (Payout) cho Chủ sân và Trọng tài
CREATE TABLE payouts (
                         id INT PRIMARY KEY AUTO_INCREMENT,
                         recipient_user_id INT NOT NULL, -- ID của Chủ sân hoặc Trọng tài
                         amount DECIMAL(10, 2) NOT NULL,
    -- 'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'
                         status ENUM('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED') DEFAULT 'PENDING',
                         requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         processed_at DATETIME,
                         payment_method VARCHAR(50),
                         transaction_code VARCHAR(255),
                         FOREIGN KEY (recipient_user_id) REFERENCES users(id)
);

-- ======== XI. BẢNG KHUYẾN KHÍCH & PHẦN THƯỞNG (REWARDS) ========

-- Bảng lịch sử điểm thân thiết (Loyalty)
CREATE TABLE loyalty_history (
                                 id INT PRIMARY KEY AUTO_INCREMENT,
                                 user_id INT NOT NULL,
                                 points_change INT NOT NULL, -- Có thể là số âm
    -- Ví dụ: 'BOOKING_COMPLETED', 'VOUCHER_USED'
                                 reason VARCHAR(255),
                                 booking_id INT,
                                 created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                 FOREIGN KEY (user_id) REFERENCES players(user_id),
                                 FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Bảng định nghĩa các loại phần thưởng (Rank & Loyalty)
CREATE TABLE rewards (
                         id INT PRIMARY KEY AUTO_INCREMENT,
                         name VARCHAR(255) NOT NULL,
                         description TEXT,
    -- 'BADGE' (Huy hiệu Rank), 'FRAME' (Khung Avatar Rank), 'VOUCHER' (Phiếu giảm giá)
                         type ENUM('BADGE', 'FRAME', 'VOUCHER') NOT NULL,
                         image_url VARCHAR(255), -- Cho BADGE và FRAME
    -- 'RANK' (Phần thưởng Xếp hạng) hoặc 'LOYALTY' (Phần thưởng Thân thiết)
                         source ENUM('RANK', 'LOYALTY') NOT NULL,
                         conditions JSON, -- Điều kiện nhận thưởng
                         tier ENUM('BRONZE', 'SILVER', 'GOLD', 'PLATINUM') -- Cho loyalty rewards
);

-- Bảng ghi nhận phần thưởng người dùng đã đạt được
CREATE TABLE user_rewards (
                              id INT PRIMARY KEY AUTO_INCREMENT,
                              user_id INT NOT NULL,
                              reward_id INT NOT NULL,
                              season_id INT, -- Cho phần thưởng theo mùa
                              earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              expires_at DATETIME, -- Cho VOUCHER
                              is_used BOOLEAN DEFAULT false, -- Cho VOUCHER
                              used_at DATETIME,
                              FOREIGN KEY (user_id) REFERENCES users(id),
                              FOREIGN KEY (reward_id) REFERENCES rewards(id),
                              FOREIGN KEY (season_id) REFERENCES seasons(id)
);

-- ======== XII. INDEXES CHO HIỆU SUẤT ========

-- Indexes cho query performance
CREATE INDEX idx_bookings_court_time ON bookings(court_id, start_time, end_time);
CREATE INDEX idx_bookings_status_type ON bookings(status, booking_type);
CREATE INDEX idx_bookings_created_by ON bookings(created_by_player_id, created_by_staff_id);
CREATE INDEX idx_players_elo ON players(current_elo);
CREATE INDEX idx_players_loyalty ON players(loyalty_points, loyalty_tier);
CREATE INDEX idx_transactions_user_status ON transactions(user_id, status);
CREATE INDEX idx_transactions_booking ON transactions(booking_id);
CREATE INDEX idx_booking_participants_booking ON booking_participants(booking_id, join_status);
CREATE INDEX idx_booking_participants_user ON booking_participants(user_id);
CREATE INDEX idx_referees_type_venue ON referees(referee_type, works_at_venue_id);
CREATE INDEX idx_venues_location ON venues(latitude, longitude);
CREATE INDEX idx_court_pricing_court ON court_pricing(court_id, day_of_week);
CREATE INDEX idx_ranked_matches_season ON ranked_matches(season_id, status);
CREATE INDEX idx_elo_history_season ON elo_history(season_id, user_id);
CREATE INDEX idx_check_ins_booking_user ON check_ins(booking_id, user_id);
CREATE INDEX idx_registration_requests_status ON registration_requests(status, request_type);

-- ======== XIII. RÀNG BUỘC TOÀN VẸN BỔ SUNG ========

-- Ràng buộc để đảm bảo tính toàn vẹn dữ liệu
ALTER TABLE bookings
    ADD CONSTRAINT chk_booking_times CHECK (start_time < end_time);

ALTER TABLE court_pricing
    ADD CONSTRAINT chk_pricing_times CHECK (start_time < end_time);

ALTER TABLE ranked_matches
    ADD CONSTRAINT chk_winning_team CHECK (winning_team IN ('A', 'B') OR winning_team IS NULL);

ALTER TABLE booking_participants
    ADD CONSTRAINT chk_team_value CHECK (team IN ('A', 'B') OR team IS NULL);