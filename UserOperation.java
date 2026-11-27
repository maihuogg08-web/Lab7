import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.*;

public class UserOperation {
    public static final String USER_FILE_PATH = "data/users.txt";
    private static UserOperation instance;
    private UserOperation() {
        initDefaultAdmin();
    }

    /**
     * 2.6.1. getInstance()
     * Returns the single instance of UserOperation.
     * @return UserOperation instance
     */
    public static synchronized UserOperation getInstance() {
        if (instance == null) {
            instance = new UserOperation();
        }
        return instance;
    }

    /**
     * 2.6.2. generateUniqueUserId()
     * Generates and returns a 10-digit unique user id starting with 'u_'
     * every time when a new user is registered.
     * @return A string value in the format 'u_10digits', e.g., 'u_1234567890'
     */
    public String generateUniqueUserId() {
        // 1. Gom các ID hiện có vào Set
        Set<String> takenIds = new HashSet<>();
        for (User user : loadAllUsers()) {
            takenIds.add(user.getUserId());
        }

        Random rand = new Random();
        String generatedId;

        // 2. Dùng do - while: sinh thử, nếu ID đã bị lấy (taken) thì lặp lại
        do {
            long randomNum = (long) (rand.nextDouble() * 10_000_000_000L);
            generatedId = "u_" + String.format("%010d", randomNum);
        } while (takenIds.contains(generatedId));

        return generatedId;
    }

    /**
     * 2.6.3. encryptPassword()
     * Encryption steps:
     * 1. Generate a random string with length = 2 * password length (a-zA-Z0-9).
     * 2. Select two letters sequentially from random string and append one letter
     *    from input password.
     * 3. Add "^^" at the beginning and "$$" at the end.
     *
     * @param userPassword The password to encrypt
     * @return Encrypted password
     */
    public String encryptPassword(String userPassword) {
        if (userPassword == null || userPassword.isEmpty()) {
            return "";
        }

        String chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();

        int randLength = 2 * userPassword.length();
        StringBuilder randBuilder = new StringBuilder(randLength);
        for (int i = 0; i < randLength; i++) {
            randBuilder.append(chars.charAt(random.nextInt(chars.length())));
        }
        String randomStr = randBuilder.toString();

        StringBuilder encrypted = new StringBuilder("^^");
        int randIndex = 0;
        for (int i = 0; i < userPassword.length(); i++) {
            encrypted.append(randomStr.charAt(randIndex++));
            encrypted.append(randomStr.charAt(randIndex++));
            encrypted.append(userPassword.charAt(i));
        }

        encrypted.append("$$");
        return encrypted.toString();
    }

    /**
     * 2.6.4. decryptPassword()
     * Decode the encrypted password with a similar rule as the encryption method.
     * Bỏ "^^" và "$$", sau đó cứ mỗi block 3 ký tự (2 ký tự random + 1 ký tự gốc),
     * lấy ký tự thứ 3 (index 2, 5, 8, ...).
     *
     * @param encryptedPassword The encrypted password to decrypt
     * @return Original user-provided password
     */
    public String decryptPassword(String encryptedPassword) {
        if (encryptedPassword == null || !encryptedPassword.startsWith("^^") || !encryptedPassword.endsWith("$$")) {
            return encryptedPassword;
        }


        String core = encryptedPassword.substring(2, encryptedPassword.length() - 2);
        StringBuilder originalPassword = new StringBuilder();


        for (int i = 2; i < core.length(); i += 3) {
            originalPassword.append(core.charAt(i));
        }

        return originalPassword.toString();
    }

    /**
     * 2.6.5. checkUsernameExist()
     * Verify whether a user is already registered or exists in the system.
     * @param userName The username to check
     * @return true if exists, false otherwise
     */
    public boolean checkUsernameExist(String userName) {
        if (userName == null) return false;
        List<User> userList = loadAllUsers();
        for (User u : userList) {
            if (userName.equals(u.getUserName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 2.6.6. validateUsername()
     * Validate the user's name: only letters or underscores, length >= 5.
     * @param userName The username to validate
     * @return true if valid, false otherwise
     */
    public boolean validateUsername(String userName) {
        if (userName == null || userName.length() < 5) {
            return false;
        }
        // Biểu thức chính quy: chỉ gồm chữ cái (a-z, A-Z) hoặc dấu gạch dưới (_)
        return userName.matches("^[a-zA-Z_]+$");
    }

    /**
     * 2.6.7. validatePassword()
     * Validate password: contains at least one letter, at least one number, length >= 5.
     * @param userPassword The password to validate
     * @return true if valid, false otherwise
     */
    public boolean validatePassword(String userPassword) {
        if (userPassword == null || userPassword.length() < 5) {
            return false;
        }

        boolean hasLetter = false;
        boolean hasDigit = false;

        for (char c : userPassword.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
            if (hasLetter && hasDigit) {
                return true;
            }
        }
        return false;
    }

    /**
     * 2.6.8. login()
     * Verify the username and password combination against stored user data.
     * @param userName The username for login
     * @param userPassword The password for login
     * @return A User object (Customer or Admin) if successful, null otherwise
     */
    public User login(String userName, String userPassword) {
        if (userName == null || userPassword == null) {
            return null;
        }

        List<User> userList = loadAllUsers();
        for (User user : userList) {
            if (user.getUserName().equals(userName)) {
                String plainStoredPass = decryptPassword(user.getUserPassword());

                if (userPassword.equals(plainStoredPass) || userPassword.equals(user.getUserPassword())) {
                    return user;
                }
            }
        }
        return null;
    }

    // =========================================================================
    // HỖ TRỢ ĐỌC/GHI FILE VÀ KHỞI TẠO ADMIN (ĐÃ TINH CHỈNH ĐƠN GIẢN)
    // =========================================================================

    /**
     * Tự động tạo thư mục và admin mặc định nếu file chưa có
     */
    public void initDefaultAdmin() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdir(); // Tạo thư mục data nếu chưa có
        }

        List<User> list = loadAllUsers();
        boolean hasAdmin = false;

        // Kiểm tra xem đã có admin trong danh sách chưa
        for (User u : list) {
            if ("admin".equalsIgnoreCase(u.getUserRole())) {
                hasAdmin = true;
                break;
            }
        }

        // Nếu chưa có bất kỳ admin nào thì mới tạo tài khoản mặc định
        if (!hasAdmin) {
            String currentTime = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss").format(new Date());
            String encryptedPass = encryptPassword("admin123");
            Admin defaultAdmin = new Admin("u_0000000001", "admin", encryptedPass, currentTime, "admin");
            appendUserToFile(defaultAdmin);
        }
    }

    /**
     * Đọc toàn bộ danh sách User từ file data/users.txt
     */
    public List<User> loadAllUsers() {
        List<User> list = new ArrayList<>();
        File file = new File(USER_FILE_PATH);
        if (!file.exists()) {
            return list;
        }

        try (Scanner sc = new Scanner(file, StandardCharsets.UTF_8.name())) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (!line.isEmpty()) {
                    User u = parseUserFromJsonLine(line);
                    if (u != null) {
                        list.add(u);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Ghi thêm 1 user mới vào cuối file (append = true)
     */
    public void appendUserToFile(User user) {
        try (FileWriter fw = new FileWriter(USER_FILE_PATH, StandardCharsets.UTF_8, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(user.toString());
            bw.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Ghi đè toàn bộ danh sách user vào file (khi cập nhật hoặc xóa)
     */
    public void saveAllUsers(List<User> users) {
        try (FileWriter fw = new FileWriter(USER_FILE_PATH, StandardCharsets.UTF_8, false);
             BufferedWriter bw = new BufferedWriter(fw)) {
            for (User u : users) {
                bw.write(users.toString());
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Bóc tách 1 dòng JSON thủ công bằng cách cắt chuỗi (KHÔNG dùng Regex cao siêu)
     */
    private User parseUserFromJsonLine(String line) {
        // Ví dụ dòng: {"user_id":"u_123", "user_name":"abc", "user_role":"customer"}
        // Bỏ 2 dấu ngoặc nhọn ở đầu và cuối chuỗi
        String clean = line.replace("{", "").replace("}", "").trim();

        // Tách chuỗi theo dấu phẩy để lấy từng cặp key:value
        String[] parts = clean.split(",");
        Map<String, String> data = new HashMap<>();

        for (String part : parts) {
            // Tách theo dấu hai chấm :
            String[] kv = part.split(":", 2);
            if (kv.length == 2) {
                // Xóa hết dấu ngoặc kép " và khoảng trắng thừa
                String key = kv[0].replace("\"", "").trim();
                String val = kv[1].replace("\"", "").trim();
                data.put(key, val);
            }
        }

        String role = data.getOrDefault("user_role", "customer");
        String id = data.getOrDefault("user_id", "");
        String name = data.getOrDefault("user_name", "");
        String pass = data.getOrDefault("user_password", "");
        String time = data.getOrDefault("user_register_time", "");

        if ("admin".equalsIgnoreCase(role)) {
            return new Admin(id, name, pass, time, role);
        } else {
            String email = data.getOrDefault("user_email", "");
            String mobile = data.getOrDefault("user_mobile", "");
            return new Customer(id, name, pass, time, role, email, mobile);
        }
    }}