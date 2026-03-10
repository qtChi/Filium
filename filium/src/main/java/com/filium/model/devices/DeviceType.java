package com.filium.model.devices;

/**
 * Enumerates all supported network device types in Filium.
 * Each type carries a human-readable display name and an icon resource path.
 */
public enum DeviceType {

    PC("PC", "icons/pc.png"),
    ROUTER("Router", "icons/router.png"),
    SWITCH("Switch", "icons/switch.png"),
    DNS_SERVER("DNS Server", "icons/dns.png"),
    DHCP_SERVER("DHCP Server", "icons/dhcp.png"),
    FIREWALL("Firewall", "icons/firewall.png");

    private final String displayName;
    private final String iconPath;

    DeviceType(String displayName, String iconPath) {
        this.displayName = displayName;
        this.iconPath = iconPath;
    }

    /**
     * Returns the human-readable label shown in the UI.
     *
     * @return display name string
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the relative path to the icon resource for this device type.
     *
     * @return icon path string
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * Reverse-lookup a DeviceType by its display name.
     *
     * @param displayName the display name to search for
     * @return the matching DeviceType
     * @throws IllegalArgumentException if no DeviceType matches the given name
     */
    public static DeviceType fromDisplayName(String displayName) {
        if (displayName == null) {
            throw new IllegalArgumentException("Display name must not be null");
        }
        for (DeviceType type : values()) {
            if (type.displayName.equals(displayName)) {
                return type;
            }
        }
        throw new IllegalArgumentException(
            "No DeviceType with display name: '" + displayName + "'");
    }
}