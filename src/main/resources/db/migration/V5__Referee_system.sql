-- ======== REFEREE SYSTEM: AI Test + Trust Score + Evidence Deadline ========

-- Bảng câu hỏi cho AI Test trọng tài
CREATE TABLE referee_test_questions (
    id INT PRIMARY KEY AUTO_INCREMENT,
    category ENUM('SCORING', 'FAULT', 'LINE_CALL', 'SERVE', 'NVZ') NOT NULL,
    question_text TEXT NOT NULL,
    option_a VARCHAR(500) NOT NULL,
    option_b VARCHAR(500) NOT NULL,
    option_c VARCHAR(500) NOT NULL,
    option_d VARCHAR(500) NOT NULL,
    correct_answer CHAR(1) NOT NULL, -- 'A', 'B', 'C', 'D'
    difficulty ENUM('EASY', 'MEDIUM', 'HARD') DEFAULT 'MEDIUM',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bảng lịch sử làm test của player
CREATE TABLE referee_test_attempts (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    score INT NOT NULL,
    total_questions INT NOT NULL DEFAULT 10,
    passed BOOLEAN NOT NULL DEFAULT false,
    answers JSON, -- Lưu đáp án của player: {"1":"A","2":"B",...}
    attempted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Thêm cột trust_score và total_matches_refereed vào bảng referees
ALTER TABLE referees ADD COLUMN trust_score DECIMAL(5,2) DEFAULT 100.00;
ALTER TABLE referees ADD COLUMN total_matches_refereed INT DEFAULT 0;

-- Thêm cột evidence_deadline và referee_evidence vào bảng match_disputes
ALTER TABLE match_disputes ADD COLUMN evidence_deadline DATETIME NULL;
ALTER TABLE match_disputes ADD COLUMN referee_response TEXT NULL;
ALTER TABLE match_disputes ADD COLUMN decision_type ENUM('UPHOLD', 'OVERTURN') NULL;

-- Indexes
CREATE INDEX idx_test_questions_category ON referee_test_questions(category, is_active);
CREATE INDEX idx_test_attempts_user ON referee_test_attempts(user_id, attempted_at);
CREATE INDEX idx_disputes_status ON match_disputes(status);
CREATE INDEX idx_disputes_deadline ON match_disputes(evidence_deadline, status);
