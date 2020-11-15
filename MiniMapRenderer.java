import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.map.*;

import java.lang.reflect.Field;

/**
 * @author Comfortable_Andy
 * 
 * Please remove all renderer including this one before adding this to your map
 */

public class MiniMapRenderer extends MapRenderer {

    private final BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private final BlockFace[] radial = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

    public MiniMapRenderer() {
        // So that every player gets a different view for the map
        super(true);
    }

    @Override
    public void render(MapView map, MapCanvas canvas, Player player) {
        MapCursorCollection cursors = new MapCursorCollection();

        map.setUnlimitedTracking(true);
        map.setTrackingPosition(false);

        map.setCenterX(player.getLocation().getBlockX());
        map.setCenterZ(player.getLocation().getBlockZ());

        Field worldMap = null;

        try {
            worldMap = getCraftBukkitClass("map.CraftMapView").getDeclaredField("worldMap");
            worldMap.setAccessible(true);
        } catch (NoSuchFieldException | ClassNotFoundException | ClassCastException e) {
            e.printStackTrace();
        }

        if (worldMap == null) {
            canvas.drawText(0, 0, MinecraftFont.Font, "Unable To Render");
            return;
        }

        byte[] colors = new byte[16384];

        try {
            colors = (byte[]) getNMSClass("WorldMap").getDeclaredField("colors").get(worldMap.get(map));
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        /* worldMap.get(map) returns a WorldMap */

        for (int x = 0; x < 128; ++x) {
            for (int y = 0; y < 128; ++y) {
                /* Render Pixels */
                int index = y * 128 + x;

                if (colors[index] == canvas.getPixel(x, y)) {
                    continue;
                }

                canvas.setPixel(x, y, colors[index]);
            }
        }

        MapCursor playerMapCursor = new MapCursor((byte) 0, (byte) 0, byteFromFace(yawToFace(player.getLocation().getYaw(), true)), MapCursor.Type.BLUE_POINTER, true, player.getDisplayName());

        cursors.addCursor(playerMapCursor);
        canvas.setCursors(cursors);
    }

    private Class<?> getNMSClass(String nmsClassString) throws ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";

        if (version.contains("1_16_R4")) {
            version.replace("1_16_R4", "1_16_R2");
        }

        String name = "net.minecraft.server." + version + nmsClassString;
        Class<?> nmsClass = Class.forName(name);
        return nmsClass;
    }

    private Class<?> getCraftBukkitClass(String classString) throws ClassCastException, ClassNotFoundException {
        String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";

        String name = "org.bukkit.craftbukkit." + version + classString;
        Class<?> craftBukkitClass = Class.forName(name);
        return craftBukkitClass;
    }

    /**
     * Huge thanks to https://www.spigotmc.org/threads/map-location.356640/#post-3289946
     */

    private BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections) {
            return radial[Math.round(yaw / 45f) & 0x7].getOppositeFace();
        }

        return axis[Math.round(yaw / 90f) & 0x3].getOppositeFace();
    }

    private byte byteFromFace(BlockFace face) {
        switch (face) {
            case SOUTH:
                return (byte) 0;
            case SOUTH_WEST:
                return (byte) 2;
            case WEST:
                return (byte) 4;
            case NORTH_WEST:
                return (byte) 6;
            case NORTH_EAST:
                return (byte) 10;
            case EAST:
                return (byte) 12;
            case SOUTH_EAST:
                return (byte) 14;
            default:
                return (byte) 8;
        }
    }
}
