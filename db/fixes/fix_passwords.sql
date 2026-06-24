SET client_encoding TO 'UTF8';

-- ============================================================
-- 修复测试用户密码
-- 原因：SQL 中的 BCrypt 哈希值不正确（长度58，应为60）
-- 正确的 "123456" BCrypt hash（jBCrypt 0.4, cost=10）：
--   $2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty
-- ============================================================

UPDATE public.users
SET password_hash = '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty'
WHERE user_id BETWEEN 230 AND 259;

SELECT '密码已修复！影响行数: ' || count(*) AS result FROM users
WHERE user_id BETWEEN 230 AND 259
AND password_hash = '$2a$10$0hxFVF2Tb77NKlAiNII5O.7NfJNQSl.FQHSs.aYpW1pZe7mtwJjty';
