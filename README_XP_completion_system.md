# README - Hướng dẫn AI code cơ chế nhận kinh nghiệm (XP) sau khi hoàn thành bài học

## 1. Mục tiêu

Tài liệu này mô tả yêu cầu để AI/coder triển khai cơ chế nhận điểm kinh nghiệm (XP) sau khi người dùng hoàn thành một bài học hoặc bài luyện tập.

Cơ chế cần đảm bảo:

- Tự động ghi nhận kết quả học tập sau khi hoàn thành bài.
- Phân loại bài học là bài mới hay bài luyện tập lại.
- Cộng XP theo đúng quy tắc.
- Cập nhật tổng XP của người dùng.
- Hiển thị hiệu ứng nhận XP.
- Kiểm tra điều kiện lên cấp/badge sau khi cộng XP.
- Lưu dữ liệu bền vững vào hồ sơ người dùng.

---

## 2. Quy tắc nhận XP

Hệ thống sử dụng 2 mức XP cố định:

| Loại hoạt động | XP nhận được |
|---|---|
| Hoàn thành bài học mới | 200 XP |
| Luyện tập lại bài cũ | 50 XP |

Không có ngoại lệ ngoài hai mức trên.

---

## 3. Flow tổng quát

```text
User hoàn thành bài học
        ↓
Hệ thống nhận lessonResult
        ↓
Kiểm tra loại bài học
        ↓
Tính XP nhận được
        ↓
Cộng XP vào totalXP
        ↓
Lưu totalXP mới vào database
        ↓
Hiển thị popup "+XP"
        ↓
Animate XP Bar
        ↓
Kiểm tra có đủ điều kiện lên cấp không
        ↓
Nếu đủ điều kiện → kích hoạt badge/level up
```

---

## 4. Input cần có

Khi người dùng hoàn thành bài học, hệ thống cần nhận được object kết quả:

```ts
type LessonResult = {
    userId: string
    lessonId: string
    score: number

    isNewLesson: boolean
    completedAt: Date
}
```

Ý nghĩa:

- `userId`: ID người dùng.
- `lessonId`: ID bài học vừa hoàn thành.
- `score`: điểm số hoặc phần trăm hoàn thành.
- `isNewLesson`: xác định đây là bài mới hay bài luyện tập lại.
- `completedAt`: thời điểm hoàn thành bài học.

---

## 5. Data model người dùng

```ts
type UserProgress = {
    userId: string

    totalXP: number
    currentLevel: number
    currentBadge: string

    updatedAt: Date
}
```

---

## 6. Hàm tính XP

AI/coder cần triển khai một hàm riêng để tính XP.

```ts
function calculateXP(isNewLesson: boolean): number {
    if (isNewLesson) {
        return 200
    }

    return 50
}
```

Quy tắc:

```text
isNewLesson = true  → 200 XP
isNewLesson = false → 50 XP
```

---

## 7. Hàm xử lý chính

```ts
async function handleLessonCompleted(result: LessonResult) {
    const userProgress = await getUserProgress(result.userId)

    const xpReward = calculateXP(result.isNewLesson)

    const oldXP = userProgress.totalXP
    const newXP = oldXP + xpReward

    await updateUserXP(result.userId, newXP)

    const levelResult = checkLevelUp(oldXP, newXP)

    return {
        xpReward,
        oldXP,
        newXP,
        levelResult
    }
}
```

---

## 8. Kiểm tra lên cấp sau khi nhận XP

Sau khi cộng XP, hệ thống phải kiểm tra xem người dùng có đạt badge/level mới không.

Ví dụ bảng level:

```ts
const LEVELS = [
    { level: 1, badge: "Bronze", xpRequired: 0 },
    { level: 2, badge: "Silver", xpRequired: 1000 },
    { level: 3, badge: "Gold", xpRequired: 3000 },
    { level: 4, badge: "Platinum", xpRequired: 7000 }
]
```

Hàm kiểm tra:

```ts
function getLevelByXP(totalXP: number) {
    let current = LEVELS[0]

    for (const level of LEVELS) {
        if (totalXP >= level.xpRequired) {
            current = level
        }
    }

    return current
}
```

Hàm kiểm tra có lên cấp không:

```ts
function checkLevelUp(oldXP: number, newXP: number) {
    const oldLevel = getLevelByXP(oldXP)
    const newLevel = getLevelByXP(newXP)

    return {
        isLevelUp: newLevel.level > oldLevel.level,
        oldLevel,
        newLevel
    }
}
```

---

## 9. Thứ tự hiển thị trên giao diện

Sau khi hoàn thành bài học, UI phải hiển thị theo đúng thứ tự:

```text
1. Popup thông báo XP, ví dụ: "+200 XP"
2. Hiệu ứng đồng xu/ngôi sao rơi xuống
3. Thanh XP Bar tăng từ oldXP lên newXP
4. Nếu đủ điều kiện, hiển thị popup Level Up / Badge mới
5. Cập nhật thông tin XP trên hồ sơ người dùng
```

Không nên hiển thị tất cả hiệu ứng cùng lúc vì sẽ gây rối trải nghiệm người dùng.

---

## 10. XP Bar

XP Bar cần thể hiện tiến trình XP hiện tại trong level.

Ví dụ:

```ts
type XPBarData = {
    currentLevel: number
    currentBadge: string

    xpInCurrentLevel: number
    xpNeededForNextLevel: number

    progressPercent: number
}
```

Ví dụ hàm tính phần trăm:

```ts
function calculateXPBar(totalXP: number) {
    const currentLevel = getLevelByXP(totalXP)
    const nextLevel = LEVELS.find(level => level.level === currentLevel.level + 1)

    if (!nextLevel) {
        return {
            currentLevel: currentLevel.level,
            currentBadge: currentLevel.badge,
            xpInCurrentLevel: totalXP,
            xpNeededForNextLevel: 0,
            progressPercent: 100
        }
    }

    const xpStart = currentLevel.xpRequired
    const xpEnd = nextLevel.xpRequired

    const xpInCurrentLevel = totalXP - xpStart
    const xpNeededForNextLevel = xpEnd - xpStart

    const progressPercent = (xpInCurrentLevel / xpNeededForNextLevel) * 100

    return {
        currentLevel: currentLevel.level,
        currentBadge: currentLevel.badge,
        xpInCurrentLevel,
        xpNeededForNextLevel,
        progressPercent
    }
}
```

---

## 11. Lưu dữ liệu

Sau khi cộng XP, hệ thống phải lưu ngay vào database.

Không được chỉ cập nhật ở frontend.

```ts
async function updateUserXP(userId: string, newXP: number) {
    await db.users.update({
        where: { id: userId },
        data: {
            totalXP: newXP,
            updatedAt: new Date()
        }
    })
}
```

---

## 12. Yêu cầu về tính bền vững dữ liệu

Nếu người dùng thoát app ngay sau khi hoàn thành bài học:

```text
XP vẫn phải được lưu.
```

Vì vậy, thứ tự xử lý nên là:

```text
1. Tính XP
2. Lưu XP vào database
3. Sau đó mới chạy animation
```

Không làm ngược lại.

---

## 13. Chống cộng XP nhiều lần

Cần tránh trường hợp người dùng spam nút hoàn thành hoặc gửi request nhiều lần.

Nên tạo bảng lịch sử nhận XP:

```ts
type XPTransaction = {
    id: string
    userId: string
    lessonId: string

    xpAmount: number
    reason: "NEW_LESSON" | "REVIEW"

    createdAt: Date
}
```

Trước khi cộng XP, kiểm tra transaction đã tồn tại chưa:

```ts
async function hasReceivedXP(userId: string, lessonId: string) {
    const transaction = await db.xpTransactions.findFirst({
        where: {
            userId,
            lessonId
        }
    })

    return !!transaction
}
```

Nếu đã nhận XP cho lần hoàn thành đó thì không cộng lại.

---

## 14. API đề xuất

### Endpoint

```http
POST /api/lessons/complete
```

### Request body

```json
{
    "userId": "user_001",
    "lessonId": "lesson_001",
    "score": 85,
    "isNewLesson": true
}
```

### Response

```json
{
    "success": true,
    "xpReward": 200,
    "oldXP": 800,
    "newXP": 1000,
    "isLevelUp": true,
    "newLevel": {
        "level": 2,
        "badge": "Silver"
    }
}
```

---

## 15. Pseudocode backend hoàn chỉnh

```ts
async function completeLesson(req, res) {
    const { userId, lessonId, score, isNewLesson } = req.body

    const alreadyReceived = await hasReceivedXP(userId, lessonId)

    if (alreadyReceived) {
        return res.status(400).json({
            success: false,
            message: "XP has already been granted for this lesson."
        })
    }

    const userProgress = await getUserProgress(userId)

    const xpReward = calculateXP(isNewLesson)

    const oldXP = userProgress.totalXP
    const newXP = oldXP + xpReward

    await createXPTransaction({
        userId,
        lessonId,
        xpAmount: xpReward,
        reason: isNewLesson ? "NEW_LESSON" : "REVIEW"
    })

    await updateUserXP(userId, newXP)

    const levelResult = checkLevelUp(oldXP, newXP)

    if (levelResult.isLevelUp) {
        await updateUserLevel(userId, levelResult.newLevel)
    }

    return res.json({
        success: true,
        xpReward,
        oldXP,
        newXP,
        isLevelUp: levelResult.isLevelUp,
        newLevel: levelResult.newLevel
    })
}
```

---

## 16. Frontend behavior

Khi nhận response từ API:

```ts
const result = await completeLessonApi(payload)

showXPPopup(result.xpReward)

await animateXPBar(result.oldXP, result.newXP)

if (result.isLevelUp) {
    showLevelUpModal(result.newLevel)
}
```

---

## 17. Các edge cases cần xử lý

### Case 1: User spam submit

```text
Không được cộng XP nhiều lần.
```

Giải pháp:

```text
Dùng XPTransaction để kiểm tra.
```

---

### Case 2: App crash sau khi học xong

```text
XP không được mất.
```

Giải pháp:

```text
Lưu database trước, chạy animation sau.
```

---

### Case 3: User học lại bài cũ

```text
Chỉ cộng 50 XP.
```

---

### Case 4: User nhận XP đủ để lên nhiều cấp

```text
Phải tính level cuối cùng đạt được.
```

Không chỉ tăng từng cấp một cách thủ công.

---

## 18. Checklist cho AI/coder

- [ ] Tạo hàm `calculateXP`
- [ ] Tạo hàm `handleLessonCompleted`
- [ ] Tạo bảng/object `UserProgress`
- [ ] Tạo bảng/object `XPTransaction`
- [ ] Cộng 200 XP cho bài mới
- [ ] Cộng 50 XP cho bài luyện tập lại
- [ ] Cập nhật `totalXP`
- [ ] Lưu dữ liệu trước khi chạy animation
- [ ] Kiểm tra level up sau khi cộng XP
- [ ] Chặn cộng XP trùng
- [ ] Trả về dữ liệu đủ cho frontend animate XP Bar
- [ ] Hiển thị popup `+XP`
- [ ] Animate XP Bar
- [ ] Hiển thị Level Up nếu có

---

## 19. Tóm tắt ngắn gọn

Cơ chế nhận XP hoạt động như sau:

```text
Hoàn thành bài học
        ↓
Xác định bài mới hay bài cũ
        ↓
Bài mới: +200 XP
Bài cũ: +50 XP
        ↓
Cộng vào tổng XP
        ↓
Lưu vào database
        ↓
Cập nhật XP Bar
        ↓
Kiểm tra lên cấp/badge
```

