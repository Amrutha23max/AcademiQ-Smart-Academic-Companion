-- Optional sample data. Register an account in the app first, then set your email below and run:
--   mysql -u root -p academiq < database/sample_data.sql
SET @email = 'you@example.com';

SET @u = (SELECT id FROM users WHERE email = @email);

INSERT INTO subjects(user_id, name, semester) VALUES (@u, 'Advanced Java', 5), (@u, 'Database Systems', 5);
SET @s1 = (SELECT id FROM subjects WHERE user_id = @u AND name = 'Advanced Java' ORDER BY id DESC LIMIT 1);
SET @s2 = (SELECT id FROM subjects WHERE user_id = @u AND name = 'Database Systems' ORDER BY id DESC LIMIT 1);

INSERT INTO assignments(user_id, subject_id, title, description, due_date, status) VALUES
    (@u, @s1, 'JDBC lab record', 'Write the record', DATE_ADD(CURDATE(), INTERVAL 1 DAY), 'PENDING'),
    (@u, @s2, 'ER diagram', 'Library system', DATE_SUB(CURDATE(), INTERVAL 2 DAY), 'PENDING'),
    (@u, @s2, 'Normalization sheet', '', DATE_ADD(CURDATE(), INTERVAL 9 DAY), 'PENDING');

INSERT INTO attendance(user_id, subject_id, classes_conducted, classes_attended) VALUES
    (@u, @s1, 30, 21), (@u, @s2, 24, 22)
ON DUPLICATE KEY UPDATE classes_conducted = VALUES(classes_conducted), classes_attended = VALUES(classes_attended);

INSERT INTO marks(user_id, subject_id, assessment, obtained_marks, max_marks) VALUES
    (@u, @s1, 'Internal 1', 42, 50), (@u, @s2, 'Internal 1', 31, 50);
