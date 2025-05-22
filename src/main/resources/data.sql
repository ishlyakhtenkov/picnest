DELETE FROM photos;
DELETE FROM albums;
DELETE FROM change_email_tokens;
DELETE FROM password_reset_tokens;
DELETE FROM register_tokens;
DELETE FROM user_roles;
DELETE FROM users;
ALTER SEQUENCE global_seq RESTART WITH 100000;

INSERT INTO users (email, name, information, password, enabled, avatar_file_name, avatar_file_link)
VALUES ('user@gmail.com','John Doe', 'Some info', '{noop}password', true, 'cool_user.jpg', './content/avatars/100000/cool_user.jpg'),
       ('admin@gmail.com','Jack', 'Java developer with 10 years of production experience.', '{noop}admin', true, 'admin.jpg', './content/avatars/100001/admin.jpg'),
       ('user2@gmail.com','Alice Key', null, '{noop}somePassword', true, 'cat.jpg', './content/avatars/100002/cat.jpg'),
       ('userDisabled@gmail.com','Freeman25', null, '{noop}password', false, null, null);

INSERT INTO user_roles (role, user_id)
VALUES ('USER', 100000),
       ('USER', 100001),
       ('ADMIN', 100001),
       ('USER', 100002),
       ('USER', 100003);

INSERT INTO register_tokens (token, expiry_timestamp, email, name, password)
VALUES ('5a99dd09-d23f-44bb-8d41-b6ff44275d01', '2024-08-06 19:35:56', 'some@gmail.com', 'someName', '{noop}somePassword'),
       ('52bde839-9779-4005-b81c-9131c9590d79', '2052-05-24 16:42:03', 'new@gmail.com', 'newName', '{noop}newPassword');

INSERT INTO password_reset_tokens (token, expiry_timestamp, user_id)
VALUES ('5a99dd09-d23f-44bb-8d41-b6ff44275x97', '2052-02-05 12:10:00', 100000),
       ('52bde839-9779-4005-b81c-9131c9590b41', '2022-02-06 19:35:56', 100002),
       ('54ghh534-9778-4005-b81c-9131c9590c63', '2052-04-25 13:48:14', 100003);

INSERT INTO change_email_tokens (token, expiry_timestamp, new_email, user_id)
VALUES ('5a49dd09-g23f-44bb-8d41-b6ff44275s56', '2024-08-05 21:49:01', 'some@gmail.com', 100001),
       ('1a43dx02-x23x-42xx-8r42-x6ff44275y67', '2052-01-22 06:17:32', 'someNew@gmail.com', 100002);

INSERT INTO albums(name, created, user_id)
VALUES ('user album 1', '2025-05-22 12:28:01', 100000),
       ('user album 2', '2025-04-18 21:13:14', 100000),
       ('admin album', '2025-03-17 16:22:48', 100001);

INSERT INTO photos(created, description, file_name, file_link, album_id)
VALUES ('2025-05-22 12:28:03', 'photo 1 user alb 1 desc', 'ph1.jpg', './content/photos/100000/100011/ph1.jpg', 100011),
       ('2025-05-22 12:28:07', 'photo 2 user alb 1 desc', 'ph2.jpg', './content/photos/100000/100011/ph2.jpg', 100011),
       ('2025-05-22 12:28:07', 'photo 3 user alb 1 desc', 'ph3.jpg', './content/photos/100000/100011/ph3.jpg', 100011),
       ('2025-04-19 17:46:15', 'photo 1 user alb 2 desc', 'ph1.jpg', './content/photos/100000/100012/ph1.jpg', 100012),
       ('2025-04-19 18:12:32', 'photo 2 user alb 2 desc', 'ph2.jpg', './content/photos/100000/100012/ph2.jpg', 100012),
       ('2025-03-17 16:28:14', 'photo 1 admin alb 1 desc', 'ph1.jpg', './content/photos/100001/100013/ph1.jpg', 100013),
       ('2025-03-17 16:34:59', 'photo 2 admin alb 1 desc', 'ph2.jpg', './content/photos/100001/100013/ph2.jpg', 100013);
