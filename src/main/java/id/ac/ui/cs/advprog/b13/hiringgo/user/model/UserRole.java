package id.ac.ui.cs.advprog.b13.hiringgo.user.model;

public enum UserRole {
    ADMIN,
    CUSTOMER;

    public static boolean contains(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.name().equals(value)) return true;
        }
        return false;
    }
}
