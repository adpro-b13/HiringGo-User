package id.ac.ui.cs.advprog.b13.hiringgo.user.enums;

public enum Role {
    ADMIN,
    MAHASISWA,
    DOSEN;

    public static boolean contains(String value) {
        for (UserRole role : UserRole.values()) {
            if (role.name().equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
