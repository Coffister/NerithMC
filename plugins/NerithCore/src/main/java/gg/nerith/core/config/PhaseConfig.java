package gg.nerith.core.config;

import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class PhaseConfig {

    private final int phase;
    private final String name;
    private final long blocksRequired;
    private final String loreLine;
    private final String particleName;
    private final List<ItemStack> rewardKit;

    public PhaseConfig(int phase, String name, long blocksRequired, String loreLine, String particleName, List<String> rewardKitRaw) {
        this.phase = phase;
        this.name = name;
        this.blocksRequired = blocksRequired;
        this.loreLine = loreLine;
        this.particleName = particleName;
        this.rewardKit = parseItems(rewardKitRaw);
    }

    private List<ItemStack> parseItems(List<String> raw) {
        List<ItemStack> items = new ArrayList<>();
        for (String entry : raw) {
            String[] parts = entry.split(":");
            try {
                Material mat = Material.valueOf(parts[0]);
                int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
                items.add(new ItemStack(mat, amount));
            } catch (Exception ignored) {}
        }
        return items;
    }

    public int getPhase() { return phase; }
    public String getName() { return name; }
    public long getBlocksRequired() { return blocksRequired; }
    public String getLoreLine() { return loreLine; }
    public List<ItemStack> getRewardKit() { return rewardKit; }

    public Particle getParticle() {
        try {
            return Particle.valueOf(particleName);
        } catch (Exception e) {
            return Particle.SMOKE;
        }
    }
}
