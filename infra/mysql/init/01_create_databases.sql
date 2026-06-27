-- ============================================================
-- EasyTicket – MySQL Init Script
-- Tạo các databases cho từng service (Database per Service pattern)
-- File này chạy tự động khi MySQL container khởi tạo lần đầu
-- ============================================================

-- AuthService database (hiện đang dùng tên pro_be_auth theo application.yaml)
CREATE DATABASE IF NOT EXISTS `pro_be_auth`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Keycloak database
CREATE DATABASE IF NOT EXISTS `keycloak_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Event Service database
CREATE DATABASE IF NOT EXISTS `event_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Order Service database
CREATE DATABASE IF NOT EXISTS `order_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Ticket Service database
CREATE DATABASE IF NOT EXISTS `ticket_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Payment Service database
CREATE DATABASE IF NOT EXISTS `payment_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Notification Service database
CREATE DATABASE IF NOT EXISTS `notification_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- user_db alias (theo yêu cầu ban đầu)
CREATE DATABASE IF NOT EXISTS `user_db`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- ============================================================
-- Grant permissions cho easyticket user trên tất cả databases
-- ============================================================
GRANT ALL PRIVILEGES ON `pro_be_auth`.* TO 'easyticket'@'%';
GRANT ALL PRIVILEGES ON `event_db`.* TO 'easyticket'@'%';
GRANT ALL PRIVILEGES ON `order_db`.* TO 'easyticket'@'%';
GRANT ALL PRIVILEGES ON `ticket_db`.* TO 'easyticket'@'%';
GRANT ALL PRIVILEGES ON `payment_db`.* TO 'easyticket'@'%';
GRANT ALL PRIVILEGES ON `notification_db`.* TO 'easyticket'@'%';
GRANT ALL PRIVILEGES ON `user_db`.* TO 'easyticket'@'%';
-- Keycloak cần root vì nó tạo schema phức tạp
GRANT ALL PRIVILEGES ON `keycloak_db`.* TO 'easyticket'@'%';

FLUSH PRIVILEGES;
