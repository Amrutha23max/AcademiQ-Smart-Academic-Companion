package com.academiq.account.service;

import com.academiq.common.*;
import com.academiq.account.dao.UserDAO;
import com.academiq.account.model.User;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/** Registration, login, profile and password rules. Passwords are stored as salted PBKDF2 hashes, never as plain text. */
public class AuthService {
    private static final int ITERATIONS = 65_536;
    private final UserDAO dao = new UserDAO();

    public User register(String name, String email, String course, String password, String confirm) throws AcademiqException {
        name = Validators.requireText("Name", name, 100);
        email = Validators.requireEmail(email);
        course = Validators.optionalText(course, 100);
        checkPasswordRules(password);
        if (!password.equals(confirm)) throw new ValidationException("Passwords do not match.");
        if (dao.findByEmail(email) != null) throw new ValidationException("An account with this email already exists.");
        int id = dao.insert(name, email, course, hash(password));
        return new User(id, name, email, course, null);
    }

    public User login(String email, String password) throws AcademiqException {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty())
            throw new ValidationException("Enter your email and password.");
        User u = dao.findByEmail(email.trim().toLowerCase());
        if (u == null || !verify(password, u.getPasswordHash())) throw new AuthenticationException("Invalid email or password.");
        Session.login(u.getId(), u.getFullName());
        return u;
    }

    public User currentUser() throws DatabaseException { return dao.findById(Session.userId()); }

    public void updateProfile(String name, String course) throws AcademiqException {
        name = Validators.requireText("Name", name, 100);
        course = Validators.optionalText(course, 100);
        dao.updateProfile(Session.userId(), name, course);
        Session.login(Session.userId(), name);
    }

    public void changePassword(String oldPwd, String newPwd, String confirm) throws AcademiqException {
        User u = dao.findById(Session.userId());
        if (u == null || !verify(oldPwd, u.getPasswordHash())) throw new AuthenticationException("Current password is incorrect.");
        checkPasswordRules(newPwd);
        if (!newPwd.equals(confirm)) throw new ValidationException("New passwords do not match.");
        dao.updatePassword(u.getId(), hash(newPwd));
    }

    private void checkPasswordRules(String p) throws ValidationException {
        if (p == null || p.length() < 6) throw new ValidationException("Password must be at least 6 characters.");
    }

    // ---- hashing helpers ----
    static String hash(String password) throws AcademiqException {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(pbkdf2(password, salt));
    }

    static boolean verify(String password, String stored) {
        try {
            if (stored == null || !stored.contains(":")) return false;
            String[] parts = stored.split(":");
            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expected = Base64.getDecoder().decode(parts[1]);
            byte[] actual = pbkdf2(password, salt);
            return java.security.MessageDigest.isEqual(expected, actual);   // constant-time comparison
        } catch (Exception e) { return false; }
    }

    private static byte[] pbkdf2(String password, byte[] salt) throws AcademiqException {
        try {
            PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, 256);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AcademiqException("Password hashing is not available on this JVM.", e);
        }
    }
}
