-- Notes, attendance and marks.
CREATE TABLE IF NOT EXISTS notes (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    user_id     INT NOT NULL,
    subject_id  INT NOT NULL,
    title       VARCHAR(150) NOT NULL,
    topic       VARCHAR(100) DEFAULT '',
    file_path   VARCHAR(500) NOT NULL,
    upload_date DATE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attendance (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    user_id           INT NOT NULL,
    subject_id        INT NOT NULL,
    classes_conducted INT NOT NULL DEFAULT 0,
    classes_attended  INT NOT NULL DEFAULT 0,
    UNIQUE (subject_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS marks (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    user_id        INT NOT NULL,
    subject_id     INT NOT NULL,
    assessment     VARCHAR(100) NOT NULL,
    obtained_marks DOUBLE NOT NULL,
    max_marks      DOUBLE NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (subject_id) REFERENCES subjects(id) ON DELETE CASCADE
);
