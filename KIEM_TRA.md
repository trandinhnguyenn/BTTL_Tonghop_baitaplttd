# Kết quả kiểm tra bản bàn giao

Sinh viên: Trần Đình Nguyên – 2415053122225.

Đã thực hiện ngày 17/11/2026:

| Kiểm tra | Kết quả |
| --- | --- |
| Gradle assembleDebug | Thành công, APK có trong thư mục APK |
| Gradle testDebugUnitTest | 3 bài kiểm tra đạt, 0 lỗi |
| Gradle lintDebug | Đạt, không có lỗi nghiêm trọng; còn cảnh báo trình bày/chuỗi giao diện |
| Python API GET/POST và lưu dữ liệu | 2 bài kiểm tra đạt |
| XML, ViewBinding và Gradle Wrapper | Đã kiểm tra |

Môi trường build: JDK 17, Android SDK 34, Gradle 8.7. Chưa chạy giao diện trực tiếp trên máy ảo/điện thoại trong môi trường tạo file. Test Room trên thiết bị được cung cấp ở app/src/androidTest; chạy bằng connectedDebugAndroidTest khi có thiết bị kết nối.

Xem README.md để mở dự án và khởi động REST API mẫu.
