package enums;

public enum UserRole {
    NORMAL("Normal Student"),
    IT("IT Student");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}