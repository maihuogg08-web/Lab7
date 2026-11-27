import java.io.File;

public class Demo1 {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   BẮT ĐẦU KIỂM TRA NGHIỆM THU NHIỆM VỤ NGƯỜI 1   ");
        System.out.println("==================================================");

        UserOperation userOp = UserOperation.getInstance();
        CustomerOperation custOp = CustomerOperation.getInstance();
        AdminOperation adminOp = AdminOperation.getInstance();

        // ------------------------------------------------------------------
        // TIÊU CHÍ 1: KHỞI ĐỘNG NHIỀU LẦN CHỈ TẠO 1 ADMIN MẶC ĐỊNH
        // ------------------------------------------------------------------
        System.out.println("\n[TEST 1] Kiểm tra khởi tạo Admin mặc định:");
        adminOp.registerAdmin(); // Thử gọi lại lần nữa xem có bị duplicate không

        int adminCount = 0;
        for (User u : userOp.loadAllUsers()) {
            if ("admin".equalsIgnoreCase(u.getUserRole())) {
                adminCount++;
            }
        }
        System.out.println("-> Số lượng admin hiện có trong file: " + adminCount);
        if (adminCount == 1) {
            System.out.println("=> ĐẠT: Chỉ có duy nhất 1 admin mặc định!");
        } else {
            System.out.println("=> CHƯA ĐẠT: Bị trùng lặp Admin!");
        }

        // ------------------------------------------------------------------
        // TIÊU CHÍ 2: ĐĂNG KÝ VÀ ĐĂNG NHẬP CẢ 2 VAI TRÒ
        // ------------------------------------------------------------------
        System.out.println("\n[TEST 2] Kiểm tra Đăng nhập cả 2 vai trò:");

        // 2.1. Đăng nhập Admin
        User adminUser = userOp.login("admin", "admin123");
        if (adminUser != null && "admin".equalsIgnoreCase(adminUser.getUserRole())) {
            System.out.println("-> Đăng nhập vai trò ADMIN thành công: " + adminUser.getUserName());
        } else {
            System.out.println("-> Đăng nhập vai trò ADMIN thất bại!");
        }

        // 2.2. Đăng ký thử 1 Customer
        String testUser = "customer_demo";
        if (!userOp.checkUsernameExist(testUser)) {
            boolean regOk = custOp.registerCustomer(testUser, "pass12345", "demo@gmail.com", "0412345678");
            System.out.println("-> Đăng ký khách hàng mới: " + (regOk ? "Thành công" : "Thất bại"));
        }

        // 2.3. Đăng nhập Customer
        User custUser = userOp.login(testUser, "pass12345");
        if (custUser != null && "customer".equalsIgnoreCase(custUser.getUserRole())) {
            System.out.println("-> Đăng nhập vai trò CUSTOMER thành công: " + custUser.getUserName());
        } else {
            System.out.println("-> Đăng nhập vai trò CUSTOMER thất bại!");
        }

        // ------------------------------------------------------------------
        // TIÊU CHÍ 3: CẬP NHẬT HỒ SƠ LƯU BỀN VỮNG
        // ------------------------------------------------------------------
        System.out.println("\n[TEST 3] Kiểm tra cập nhật hồ sơ khách hàng:");
        if (custUser instanceof Customer) {
            Customer c = (Customer) custUser;
            String newEmail = "updated_" + System.currentTimeMillis() % 1000 + "@gmail.com";
            boolean updateOk = custOp.updateProfile("user_email", newEmail, c);

            System.out.println("-> Cập nhật email thành: " + newEmail + " -> " + (updateOk ? "Thành công" : "Thất bại"));
            System.out.println("=> Hãy CHẠY LẠI file này lần 2 để kiểm chứng email mới vẫn còn lưu trong data/users.txt!");
        }

        // ------------------------------------------------------------------
        // TIÊU CHÍ 4: PHÂN TRANG VÀ XÓA KHÁCH HÀNG
        // ------------------------------------------------------------------
        System.out.println("\n[TEST 4] Kiểm tra phân trang khách hàng:");
        CustomerListResult page1 = custOp.getCustomerList(1);
        System.out.println("-> Tổng số trang: " + page1.getTotalPage());
        System.out.println("-> Số khách hàng ở trang 1: " + page1.getCustomerList().size());

        System.out.println("\n==================================================");
        System.out.println("   KẾT THÚC TEST NGHIỆM THU BÀN GIAO CHO NHÓM     ");
        System.out.println("==================================================");
    }
}