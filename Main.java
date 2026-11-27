public class Main {
    public static void main(String[] args) {
        // Đúng yêu cầu: Gọi khi hệ thống khởi động để đảm bảo luôn có admin
        AdminOperation.getInstance().registerAdmin();

        System.out.println("Hệ thống đã khởi động thành công!");
        // Tiếp tục hiển thị Menu hoặc các chức năng tiếp theo...
    }
}