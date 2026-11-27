import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminOperation {
    private static AdminOperation instance;
    private AdminOperation() {
    }

    public static AdminOperation getInstance() {
        if (instance == null) {
            instance = new AdminOperation();
        }
        return instance;
    }


    public void registerAdmin() {
        UserOperation userOp = UserOperation.getInstance();
        List<User> userList = userOp.loadAllUsers();

        boolean adminExisted = false;
        for (User u : userList) {
            if ("admin".equalsIgnoreCase(u.getUserRole())) {
                adminExisted = true;
                break;
            }
        }

        if (!adminExisted) {
            String adminId = "u_0000000001";
            String adminUsername = "admin";
            String encryptedPass = userOp.encryptPassword("admin123"); // Mật khẩu mặc định: admin123
            String registerTime = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").format(new Date());

            Admin defaultAdmin = new Admin(adminId, adminUsername, encryptedPass, registerTime, "admin");
            userOp.appendUserToFile(defaultAdmin);
        }
    }
}