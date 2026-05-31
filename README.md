# BuddyTalk

**BuddyTalk** là ứng dụng học tiếng Việt dành cho trẻ em, được xây dựng bằng Jetpack Compose trên nền tảng Android. Ứng dụng kết hợp giữa phương pháp học qua hình ảnh, flashcard, trò chơi và công nghệ nhận dạng giọng nói NVIDIA Parakeet ASR để giúp trẻ luyện phát âm.

## Tính năng chính

- **Học theo chủ đề** — Động vật, Nghề nghiệp, Gia đình, Đồ ăn, Quần áo với flashcard hình ảnh/chữ/văn bản
- **Luyện phát âm** — Ghi âm giọng nói, gửi đến NVIDIA Parakeet ASR qua gRPC, so sánh với từ cần học
- **Trò chơi (Quiz)** — 3 chế độ: Ghép hình với chữ, Nghe chữ đoán hình, Nghe hình đoán chữ
- **Hệ thống điểm & cấp độ** — 30 cấp độ với danh hiệu tiếng Việt (Tập sự → Thần tượng), tích lũy XP
- **Streak & thống kê** — Theo dõi chuỗi ngày học liên tiếp, biểu đồ học tập theo tuần
- **Cá nhân hóa** — Đổi tên, chọn ảnh đại diện từ thư viện

## Công nghệ sử dụng

| Công nghệ | Mục đích |
|-----------|----------|
| **Kotlin** | Ngôn ngữ lập trình chính |
| **Jetpack Compose** + Material 3 | Giao diện người dùng hiện đại |
| **Navigation Compose** | Điều hướng một Activity |
| **Room** | Cơ sở dữ liệu cục bộ (4 entity: người dùng, chủ đề, bài học, phiên học) |
| **Hilt** | Dependency Injection |
| **gRPC + Protobuf** | Kết nối NVIDIA Riva ASR |
| **NVIDIA Parakeet-CTC** | Nhận dạng giọng nói tiếng Việt qua API cloud |
| **Coil** | Tải ảnh đại diện |
| **MVVM** | Kiến trúc ViewModel + Repository + DAO |

## Yêu cầu hệ thống

- **Android** — Min SDK 24 (Android 7.0), Target SDK 36 (Android 16)
- **Kotlin** 2.0.21
- **Gradle** 8.13+

## Cài đặt

1. Clone repository
2. Mở bằng Android Studio
3. Tạo file `local.properties` trong thư mục gốc và thêm:
   ```
   NVIDIA_API_KEY=khóa_api_của_bạn
   ```
4. Build và chạy

## Cấu trúc thư mục

```
app/src/main/java/com/example/buddytalk/
├── MainActivity.kt
├── data/
│   ├── dao/          — Room DAOs
│   ├── database/     — AppDatabase
│   ├── entity/       — Room entities
│   ├── repository/   — Repositories
│   ├── speech/       — NVIDIA Parakeet gRPC client + audio recorder
│   └── viewModel/    — ViewModels
└── ui/
    ├── component/    — Bottom bar, StreakDialog, CommonComponents
    ├── navigation/   — Routes & NavGraph
    ├── screen/       — 9 màn hình chính
    └── theme/        — Color, Theme, Typography
```

## API Key

BuddyTalk sử dụng **NVIDIA Parakeet ASR** thông qua NVIDIA Cloud Functions (gRPC). API key được đọc từ `local.properties` và injected vào `BuildConfig` tại thời điểm biên dịch. File `local.properties` đã được liệt kê trong `.gitignore` để bảo vệ khóa bí mật.

## Giấy phép

Dự án được phát triển với mục đích học tập.
