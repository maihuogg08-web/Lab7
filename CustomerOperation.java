import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerOperation {
    private static CustomerOperation instance;
    private CustomerOperation() {
    }

    public static CustomerOperation getInstance() {
        if (instance == null) {
            instance = new CustomerOperation();
        }
        return instance;
    }

    /**
     * 2.7.2. validateEmail()
     * Kiểm tra định dạng username@domain.extension mà không cần Regex phức tạp
     */
    public boolean validateEmail(String userEmail) {
        if (userEmail == null || userEmail.trim().isEmpty()) {
            return false;
        }

        int atIndex = userEmail.indexOf('@');
        int dotIndex = userEmail.lastIndexOf('.');

        // Điều kiện:
        // 1. Phải có '@' và không nằm ở đầu (atIndex > 0)
        // 2. Phải có dấu '.' nằm sau dấu '@' ít nhất 1 ký tự (dotIndex > atIndex + 1)
        // 3. Dấu '.' không được nằm ở cuối cùng (dotIndex < userEmail.length() - 1)
        // 4. Chỉ được có duy nhất 1 dấu '@' (không tìm thấy dấu @ thứ 2)
        if (atIndex > 0 && dotIndex > atIndex + 1 && dotIndex < userEmail.length() - 1) {
            return userEmail.indexOf('@', atIndex + 1) == -1;
        }

        return false;
    }

    /**
     * 2.7.3. validateMobile()
     * 10 chữ số, chỉ gồm số, bắt đầu bằng "04" hoặc "03"
     */
    public boolean validateMobile(String userMobile) {
        if (userMobile == null || userMobile.length() != 10) {
            return false;
        }

        // Bắt buộc bắt đầu bằng "04" hoặc "03"
        if (!userMobile.startsWith("04") && !userMobile.startsWith("03")) {
            return false;
        }

        // Kiểm tra tất cả các ký tự phải là chữ số
        for (int i = 0; i < userMobile.length(); i++) {
            if (!Character.isDigit(userMobile.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    /**
     * 2.7.4. registerCustomer()
     * Đăng ký khách hàng mới và ghi vào file data/users.txt
     */
    public boolean registerCustomer(String userName, String userPassword,
                                    String userEmail, String userMobile) {
        UserOperation userOp = UserOperation.getInstance();

        // 1. Kiểm tra tính hợp lệ của tất cả thông tin
        if (!userOp.validateUsername(userName)) return false;
        if (!userOp.validatePassword(userPassword)) return false;
        if (!validateEmail(userEmail)) return false;
        if (!validateMobile(userMobile)) return false;

        // 2. Kiểm tra trùng tên đăng nhập
        if (userOp.checkUsernameExist(userName)) {
            return false;
        }

        // 3. Tạo ID duy nhất và lấy thời gian đăng ký hiện tại
        String userId = userOp.generateUniqueUserId();
        String registerTime = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").format(new Date());

        // 4. Mã hóa mật khẩu
        String encryptedPassword = userOp.encryptPassword(userPassword);

        // 5. Tạo đối tượng Customer và ghi vào file
        Customer newCustomer = new Customer(userId, userName, encryptedPassword, registerTime, "customer", userEmail, userMobile);
        userOp.appendUserToFile(newCustomer);

        return true;
    }

    /**
     * 2.7.5. updateProfile()
     * Cập nhật thông tin cho khách hàng và lưu bền vững vào file
     */
    public boolean updateProfile(String attributeName, String value, Customer customerObject) {
        if (attributeName == null || value == null || customerObject == null) {
            return false;
        }

        UserOperation userOp = UserOperation.getInstance();
        boolean isValid = false;
        String attr = attributeName.toLowerCase().trim();

        // Kiểm tra và cập nhật theo từng thuộc tính
        if (attr.equals("user_password") || attr.equals("userpassword")) {
            if (userOp.validatePassword(value)) {
                customerObject.setUserPassword(userOp.encryptPassword(value));
                isValid = true;
            }
        } else if (attr.equals("user_email") || attr.equals("useremail")) {
            if (validateEmail(value)) {
                customerObject.setUserEmail(value);
                isValid = true;
            }
        } else if (attr.equals("user_mobile") || attr.equals("usermobile")) {
            if (validateMobile(value)) {
                customerObject.setUserMobile(value);
                isValid = true;
            }
        } else if (attr.equals("user_name") || attr.equals("username")) {
            // Nếu đổi username: phải đúng định dạng và không trùng với người khác
            if (userOp.validateUsername(value) && !userOp.checkUsernameExist(value)) {
                customerObject.setUserName(value);
                isValid = true;
            }
        }

        // Nếu cập nhật hợp lệ, đồng bộ lại danh sách và ghi đè vào file users.txt
        if (isValid) {
            List<User> allUsers = userOp.loadAllUsers();
            for (int i = 0; i < allUsers.size(); i++) {
                if (allUsers.get(i).getUserId().equals(customerObject.getUserId())) {
                    allUsers.set(i, customerObject); // Thay thế bằng object đã cập nhật
                    break;
                }
            }
            userOp.saveAllUsers(allUsers); // Lưu đè xuống ổ cứng
            return true;
        }

        return false;
    }

    /**
     * 2.7.6. deleteCustomer()
     * Xóa 1 khách hàng theo customerId
     */
    public boolean deleteCustomer(String customerId) {
        if (customerId == null || customerId.isEmpty()) {
            return false;
        }

        UserOperation userOp = UserOperation.getInstance();
        List<User> allUsers = userOp.loadAllUsers();
        boolean isRemoved = false;

        for (int i = 0; i < allUsers.size(); i++) {
            User u = allUsers.get(i);
            // Chỉ xóa nếu trùng ID và vai trò là customer
            if (u.getUserId().equals(customerId) && "customer".equalsIgnoreCase(u.getUserRole())) {
                allUsers.remove(i);
                isRemoved = true;
                break;
            }
        }

        if (isRemoved) {
            userOp.saveAllUsers(allUsers);
            return true;
        }
        return false;
    }

    /**
     * 2.7.7. getCustomerList()
     * Phân trang danh sách khách hàng (Tối đa 10 khách / trang)
     */
    public CustomerListResult getCustomerList(int pageNumber) {
        UserOperation userOp = UserOperation.getInstance();
        List<User> allUsers = userOp.loadAllUsers();
        List<Customer> customerList = new ArrayList<>();

        // 1. Chỉ lọc lấy những người dùng có vai trò Customer
        for (User u : allUsers) {
            if (u instanceof Customer) {
                customerList.add((Customer) u);
            }
        }

        // 2. Tính tổng số trang (pageSize = 10)
        int pageSize = 10;
        int totalRecords = customerList.size();
        int totalPage = (int) Math.ceil((double) totalRecords / pageSize);
        if (totalPage == 0) {
            totalPage = 1;
        }

        // 3. Đảm bảo số trang hợp lệ
        if (pageNumber < 1) pageNumber = 1;
        if (pageNumber > totalPage) pageNumber = totalPage;

        // 4. Cắt danh sách khách hàng của trang hiện tại
        int startIndex = (pageNumber - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, totalRecords);

        List<Customer> pagedCustomers = new ArrayList<>();
        for (int i = startIndex; i < endIndex; i++) {
            pagedCustomers.add(customerList.get(i));
        }

        return new CustomerListResult(pagedCustomers, totalPage, pageNumber);
    }

    /**
     * 2.7.8. deleteAllCustomers()
     * Xóa toàn bộ khách hàng nhưng PHẢI GIỮ LẠI ADMIN
     */
    public void deleteAllCustomers() {
        UserOperation userOp = UserOperation.getInstance();
        List<User> allUsers = userOp.loadAllUsers();
        List<User> keepUsers = new ArrayList<>();

        // Giữ lại tất cả những ai không phải là customer (ví dụ admin)
        for (User u : allUsers) {
            if (!"customer".equalsIgnoreCase(u.getUserRole())) {
                keepUsers.add(u);
            }
        }

        userOp.saveAllUsers(keepUsers);
    }
}