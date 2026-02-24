# 🏓 Pickleball Court Management & Booking Platform

> Hệ thống quản lý và đặt sân Pickleball trực tuyến với tính năng ghép trận, xếp hạng Elo và quản lý tài chính tự động.

## 📌 Tổng quan

Nền tảng kết nối **Người chơi**, **Chủ sân**, **Trọng tài** và **Quản trị viên** trong một hệ sinh thái Pickleball hoàn chỉnh — từ đặt sân, ghép trận, thi đấu xếp hạng đến thanh toán và phân chia doanh thu.

## 🏗️ Kiến trúc

| Layer | Mô tả |
|-------|--------|
| **Domain** | Entities, Repositories (interface), Business rules |
| **Application** | Use Cases, DTOs, Services |
| **Infrastructure** | JPA adapters, Security (JWT), Payment gateway |
| **Presentation** | REST Controllers, Thymeleaf (Admin UI) |

> Áp dụng **Clean Architecture** — tách biệt business logic khỏi framework, dễ test và mở rộng.

## 🛠️ Tech Stack

- **Backend:** Java 17, Spring Boot 3.5.7
- **Security:** Spring Security, JWT (jjwt)
- **Database:** MySQL 8 + Flyway Migration
- **ORM:** Spring Data JPA / Hibernate
- **Template Engine:** Thymeleaf (Admin dashboard)
- **Algorithm:** JSkills (TrueSkill/Elo rating)
- **Build:** Maven
- **Containerization:** Docker (MySQL)

## ✨ Tính năng chính

### Đã hoàn thành
- ✅ Đăng ký / Đăng nhập (JWT + Refresh Token)
- ✅ Quản lý Venue & Court (CRUD, duyệt, kích hoạt)
- ✅ Đặt sân riêng (Private Booking) — Host trả 100% tiền sân
- ✅ Walk-in Booking — Staff tạo booking tại chỗ cho khách vãng lai
- ✅ Hệ thống Staff cho Venue Owner (phân quyền, quản lý)
- ✅ Quản lý Time Slot & Court Pricing (giá theo khung giờ)
- ✅ Yêu cầu & duyệt vai trò Venue Owner (Admin)
- ✅ Tìm sân gần (nearby search theo tọa độ GPS)
- ✅ Admin Dashboard (Thymeleaf)
- ✅ Payment Interface + Mock Payment Service

### Đang phát triển
- 🔄 Ghép trận Casual (matchmaking chơi thường, chia tiền sân)
- 🔄 Ghép trận Ranked (xếp hạng Elo, trọng tài, deposit)
- 🔄 Hệ thống Elo Rating & Leaderboard
- 🔄 Trọng tài: AI Test, báo cáo kết quả, khiếu nại
- 🔄 Tích hợp thanh toán thực (ZaloPay / VNPay)
- 🔄 Loyalty Program & Ranked Rewards
- 🔄 Notification System

## 📡 API Overview

| Module | Base Path | Endpoints |
|--------|-----------|-----------|
| Auth | `/api/auth` | 4 |
| Users | `/api/users` | 1 |
| Admin | `/api/admin` | 4 |
| Venues | `/api/venues` | 11 |
| Courts | `/api/courts` | 6 |
| Bookings | `/api/bookings` | 4 |
| Time Slots | `/api/courts/{id}/slots` | 2 |
| Venue Staff | `/api/staff` | 7 |
| Health | `/api/health` | 1 |

> **Tổng: 40 REST endpoints** — Chi tiết xem `API_ENDPOINTS.md`

## 🚀 Cài đặt & Chạy

### Yêu cầu
- Java 17+
- Docker (cho MySQL) hoặc MySQL 8 local
- Maven 3.8+

### Chạy nhanh
```bash
# 1. Clone project
git clone <repo-url>
cd Pickleball

# 2. Start MySQL (Docker)
docker run -d --name pickleball-db -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=123456 \
  -e MYSQL_DATABASE=pickleball \
  mysql:8

# 3. Build & Run
./mvnw spring-boot:run
```

Server khởi động tại: `http://localhost:8080`

> Flyway sẽ tự động tạo schema khi ứng dụng khởi động lần đầu.

## 📂 Cấu trúc Project

```
src/main/java/com/pickleball/
├── domain/                 # Business logic thuần
│   ├── entities/           # Booking, Court, User, Venue, VenueStaff...
│   ├── enums/              # BookingType, Role, Status...
│   ├── repositories/       # Repository interfaces
│   ├── services/           # Domain services
│   └── valueobjects/       # Value Objects
├── application/            # Use Cases & DTOs
│   ├── usecases/           # CreateBooking, ApproveVenue...
│   ├── services/           # Application services
│   ├── dtos/               # Request/Response DTOs
│   └── config/             # App configuration
├── infrastructure/         # Framework implementations
│   ├── persistence/        # JPA entities, repositories, adapters
│   ├── security/           # JWT, Spring Security config
│   ├── payment/            # Payment gateway (Mock/Real)
│   └── config/             # Infrastructure config
└── presentation/           # API layer
    ├── controllers/        # REST controllers
    ├── advices/            # Global exception handling
    ├── responses/          # Standard API responses
    └── helpers/            # Controller helpers
```

## 📄 License

This project is for educational and portfolio purposes.
