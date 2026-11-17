# StudentMVVM – Room Database & REST API

**Sinh viên: Trần Đình Nguyên**  
**MSSV: 2415053122225**  
Học phần: Lập trình thiết bị di động · UTE Đà Nẵng  
Tài liệu thực hiện: Lecture_9_Room_REST_API_Guide.pdf (15 trang).

Có sẵn APK thử tại `APK/StudentMVVM_debug.apk`. APK dùng API mặc định dành cho Android Emulator; nếu dùng máy thật và cần kết nối API, hãy sửa địa chỉ theo hướng dẫn bên dưới rồi build lại. Các chức năng Room dùng ngoại tuyến vẫn hoạt động trên máy thật.

## 1. Mở và chạy bằng Android Studio

1. Giải nén ZIP vào đường dẫn ngắn, ví dụ `D:/Android/StudentMVVM`.
2. Trong Android Studio chọn **Open**, mở thư mục **StudentMVVM** có `settings.gradle.kts`. Không mở riêng thư mục `app`.
3. Chờ Gradle Sync tải thư viện (cần Internet lần đầu). Nếu Android Studio hỏi có tin cậy dự án, chọn Trust Project.
4. Vào **Settings > Build, Execution, Deployment > Build Tools > Gradle**, chọn **Gradle JDK 17** (JDK 21 cũng dùng được với Gradle 8.7). Nếu đang dùng JDK mới hơn, tải/chọn JDK 17.
5. Trong **SDK Manager**, cài **Android SDK Platform 34** và **Build-Tools 34.0.0** nếu thiếu. Android Studio sẽ tự tạo `local.properties` theo máy của bạn.
6. Mở **Device Manager**, tạo máy ảo điện thoại API 24 trở lên (khuyên dùng API 34), chọn cấu hình `app`, nhấn **Run ▶**.
7. Ứng dụng mở danh sách trống. Chọn **+ Thêm sinh viên** để thử lưu ngoại tuyến, hoặc chạy server theo mục 2 rồi chọn **Nạp API**.

Dự án dùng XML + ViewBinding, Kotlin 1.9.22, KSP 1.9.22-1.0.17, Room 2.6.1, Retrofit 2.9.0, AGP 8.5.2 và Gradle 8.7. Đã có `gradlew`, `gradlew.bat` và `gradle-wrapper.jar`; không cần tạo project mới, không cần cài Gradle riêng. Không cần Firebase, tài khoản hay API key.

## 2. Chạy REST API mẫu

Cài Python 3.9 trở lên trên máy tính (Windows: chọn Add Python to PATH khi cài).

Trên Windows, nhấp đúp **server/start_server.bat** và giữ cửa sổ đó mở. Hoặc mở Terminal ở thư mục StudentMVVM và chạy:

```bash
python server/server.py
```

Trên macOS/Linux dùng `python3 server/server.py`.

Mở trình duyệt máy tính tại `http://127.0.0.1:8000/api/v1/students` để kiểm tra. Sau đó vào app trên **Android Emulator** và chọn **Nạp API**, xác nhận **Nạp**. Có ba sinh viên mẫu, trong đó có Trần Đình Nguyên – 2415053122225.

**Email `nguyen.demo@example.com` và GPA 3.2 là dữ liệu minh họa, không phải thông tin cá nhân do sinh viên cung cấp.** Bạn có thể sửa chúng trong app. Họ tên và MSSV dùng đúng thông tin bạn cung cấp.

API lưu tại `server/students.json`, tự tạo khi chạy lần đầu; khởi động lại server vẫn giữ dữ liệu đã POST. Muốn đặt lại mẫu, dừng server và xóa file JSON này rồi chạy lại.

Địa chỉ app mặc định là `http://10.0.2.2:8000/`, trỏ tới máy tính từ Android Emulator. `localhost` trên máy ảo là máy ảo, không phải máy tính. Máy ảo cần bật kết nối mạng.

### Nếu chạy điện thoại thật

Cách A – USB: bật USB debugging, kết nối máy tính, chạy `adb reverse tcp:8000 tcp:8000`. Đổi API_BASE_URL trong `app/build.gradle.kts` thành `http://127.0.0.1:8000/`, Sync và Run lại. Lệnh adb nằm trong thư mục Android SDK `platform-tools`.

Cách B – Wi-Fi: cho điện thoại và máy tính cùng mạng, chạy `python server/server.py --host 0.0.0.0`, đổi API_BASE_URL thành `http://IP_LAN_MAY_TINH:8000/` (ví dụ `http://192.168.1.10:8000/`), cho phép Python qua tường lửa mạng riêng, rồi Sync và Run lại.

Luôn giữ dấu `/` cuối base URL. Đây là server thực hành nội bộ, không có đăng nhập; không đưa ra Internet công khai. Bản **debug** cho phép HTTP nội bộ; muốn dùng bản release, cấu hình API HTTPS phù hợp.

## 3. Chức năng và quy tắc dữ liệu

**Thêm / sửa:** nhập họ tên, MSSV, email, GPA 0–4. GPA cho phép dấu chấm hoặc dấu phẩy. Trường bắt buộc, email sai định dạng, MSSV trùng và GPA ngoài phạm vi đều được chặn; dialog giữ nguyên để sửa.

**Xóa:** xác nhận rồi xóa khỏi Room. Thao tác này không xóa dữ liệu máy chủ.

**Tìm kiếm:** lọc họ tên hoặc MSSV; giao diện cập nhật tự động từ Flow/LiveData.

**Nạp API (GET):** lấy toàn bộ sinh viên API, kiểm tra dữ liệu và lưu theo transaction vào Room. Cùng MSSV sẽ cập nhật dữ liệu cục bộ, giữ nguyên khóa chính; sinh viên chỉ có trên máy được giữ. Có thông báo xác nhận vì việc nạp có thể ghi đè những chỉnh sửa cục bộ cùng MSSV.

**Gửi API (POST):** bấm ở từng thẻ sinh viên và xác nhận. Server mẫu tạo mới hoặc cập nhật theo MSSV. Thao tác sửa/xóa trên máy không tự động gửi lên server. Đây là mô hình nhập/xuất tường minh để minh họa GET/POST và SSOT, chưa có hàng đợi đồng bộ hai chiều hay xử lý xung đột nhiều thiết bị. Xóa trên máy rồi Nạp API có thể khiến sinh viên đó xuất hiện lại.

**Mất mạng / tắt server:** thêm, sửa, xóa, tìm kiếm vẫn dùng Room bình thường. Nạp/Gửi báo lỗi kết nối và giữ nguyên dữ liệu cục bộ. Tắt mở app vẫn giữ dữ liệu; xóa dữ liệu ứng dụng hoặc gỡ cài đặt sẽ mất Room trên thiết bị.

## 4. Đối chiếu tài liệu bài giảng

| Nội dung tài liệu | Tệp thực hiện |
| --- | --- |
| Bước 1: Dependencies | app/build.gradle.kts |
| Bước 2: Student Entity | model/Student.kt |
| Bước 3: DAO CRUD và Flow | data/StudentDao.kt |
| Bước 4: Database Singleton | data/AppDatabase.kt |
| Bước 5: Repository | repository/StudentRepository.kt |
| Bước 6: ViewModel và Factory | viewmodel/StudentViewModel.kt, StudentViewModelFactory.kt |
| Bước 7: ListAdapter + DiffUtil | ui/StudentAdapter.kt, res/layout/item_student_room.xml |
| Bước 8: Fragment + vòng đời ViewBinding | ui/StudentListFragment.kt |
| Bước 9: Dialog thêm sinh viên | ui/AddStudentDialogFragment.kt (có thêm sửa và kiểm tra dữ liệu) |
| Bước 10: Single-Activity Host | ui/MainActivity.kt, res/layout/activity_main.xml |
| Phần 4: Retrofit + DTO + SSOT | network/ và repository/StudentRepository.kt |

Đường dẫn Kotlin trong bảng tính từ `app/src/main/java/com/example/studentmvvm/`.

Dữ liệu đi theo luồng **View → ViewModel → Repository → Room → Flow/LiveData → View**. Retrofit được gọi từ Repository; dữ liệu GET được ghi vào Room trước khi giao diện nhận. Truy vấn ghi là suspend và chạy trên Dispatchers.IO. Database dùng applicationContext và Singleton. Fragment giải phóng binding ở onDestroyView. Factory khởi tạo ViewModel có Repository và được dùng chung giữa danh sách và dialog.

## 5. Kiểm tra và trình diễn

1. Chạy app, thêm chính bạn với MSSV `2415053122225`, nhập email và GPA tùy chọn hợp lệ.
2. Thử bỏ trống họ tên, nhập sai email, GPA 5 hoặc tạo lại MSSV vừa có. Ứng dụng phải giữ dialog và báo lỗi.
3. Sửa GPA, tìm theo `Nguyên` hoặc một phần MSSV, xóa thử một sinh viên và kiểm tra xác nhận.
4. Đóng và mở lại app: dữ liệu đã lưu vẫn còn.
5. Khởi động Python server, Nạp API: ba sinh viên mẫu xuất hiện/cập nhật, cùng MSSV không sinh bản ghi trùng.
6. Tạo sinh viên mới, chọn Gửi API, mở URL trong trình duyệt để kiểm tra máy chủ đã nhận.
7. Dừng server, Nạp API: báo lỗi kết nối, danh sách cũ vẫn còn; tiếp tục thêm/sửa/xóa ngoại tuyến.
8. Xoay màn hình khi nhập: thông tin biểu mẫu được giữ.

Các lệnh tại thư mục dự án:

```bash
# Windows
.\gradlew.bat assembleDebug testDebugUnitTest lintDebug
# macOS/Linux
./gradlew assembleDebug testDebugUnitTest lintDebug
# Test Room cần máy ảo/điện thoại đang kết nối
./gradlew connectedDebugAndroidTest
# Test API, cần Python
python -m unittest discover -s server -v
```

APK sau khi build nằm tại `app/build/outputs/apk/debug/app-debug.apk`.

## 6. Lỗi thường gặp

**SDK location not found:** mở bằng Android Studio để tự tạo `local.properties`; không sao chép đường dẫn SDK từ máy người khác.

**Gradle JDK không tương thích:** chọn JDK 17 theo mục 1. Không cần nâng phiên bản Kotlin/KSP tự động; chúng đang là một cặp tương thích.

**Không tải được thư viện:** kiểm tra Internet/proxy, tắt Gradle Offline Mode, Sync lại. Nếu Studio gợi ý nâng AGP, có thể bỏ qua để giữ cấu hình đã đóng gói.

**Không kết nối API:** kiểm tra server chưa đóng, mở URL trên trình duyệt máy tính, kiểm tra địa chỉ 10.0.2.2 cho Emulator hoặc IP LAN/adb reverse cho máy thật. Chờ tối đa khoảng 20 giây cho một yêu cầu lỗi kết nối.

**Cổng 8000 đang được dùng:** tắt tiến trình đang chiếm cổng hoặc dùng `python server/server.py --port 8001`, đồng thời sửa port của API_BASE_URL và chạy lại app.

## 7. Nguồn

Tài liệu giảng viên: Lecture_9_Room_REST_API_Guide.pdf, 15 trang, do người học cung cấp.

Android Developers: https://developer.android.com/build/releases/agp-8-5-0-release-notes (cấu hình AGP 8.5, Gradle 8.7, JDK 17 và API 34).
