package name.modid.client.config;

import name.modid.client.TntCartOptimizerClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class TntCartOptimizerConfigScreen extends Screen {
    private final Screen parent;
    private boolean enabled;
    private boolean logging;
    private boolean changed;

    public TntCartOptimizerConfigScreen(Screen parent) {
        super(Component.literal("TntCartOptimizer Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        TntCartOptimizerConfig cfg = TntCartOptimizerClient.getConfig();
        enabled = cfg.isEnabled();
        logging = cfg.isLogging();
        changed = false;

        int cx = this.width / 2;

        addRenderableWidget(Button.builder(
                toggleText("Enabled: ", enabled),
                button -> {
                    enabled = !enabled;
                    button.setMessage(toggleText("Enabled: ", enabled));
                    changed = true;
                }
        ).bounds(cx - 100, 40, 200, 20).build());

        addRenderableWidget(Button.builder(
                toggleText("Logging: ", logging),
                button -> {
                    logging = !logging;
                    button.setMessage(toggleText("Logging: ", logging));
                    changed = true;
                }
        ).bounds(cx - 100, 65, 200, 20).build());

        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> saveAndClose()
        ).bounds(cx - 100, 100, 200, 20).build());
    }

    private void saveAndClose() {
        if (changed) {
            TntCartOptimizerConfig cfg = TntCartOptimizerClient.getConfig();
            cfg.setEnabled(enabled);
            cfg.setLog(logging);
            cfg.save();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }

    @Override
    public void onClose() {
        if (changed) {
            TntCartOptimizerConfig cfg = TntCartOptimizerClient.getConfig();
            cfg.setEnabled(enabled);
            cfg.setLog(logging);
            cfg.save();
        }
        if (this.minecraft != null) {
            this.minecraft.setScreenAndShow(parent);
        }
    }

    private static Component toggleText(String label, boolean value) {
        return Component.literal(label + (value ? "ON" : "OFF"));
    }
}