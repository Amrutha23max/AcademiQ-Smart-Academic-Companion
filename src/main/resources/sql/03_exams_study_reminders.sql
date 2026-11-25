-- Exams, study planner and reminders (reminders reference assignments).
CREATE TABLE IF NOT EXISTS exams (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    user_id    INT NOT NULL,
    subject_id INT NOT NULL,
    exam_name  VARCHAR(100) NOT NULL,
    exam_date  DATE NOT NULL,
    exam_time  TIME NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS study_goals (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    description VARCHAR(200) NOT NULL,
    target_date DATE NOT NULL,
    completed   TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS study_sessions (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    goal_id          INT NOT NULL,
    session_date     DATE NOT NULL,
    duration_minutes INT NOT NULL,
    completed        TINYINT(1) NOT NULL DEFAULT 0,
    FOREIGN KEY (goal_id) REFERENCES study_goals(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reminders (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    user_id       INT NOT NULL,
    assignment_id INT NOT NULL,
    notified_at   TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (assignment_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (assignment_id) REFERENCES assignments(id) ON DELETE CASCADE
);
