package x7k2m9;

import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceType;
import x7k2m9.base.utils.input.KeybindManager;
import x7k2m9.base.utils.input.KeyboardHandler;
import x7k2m9.base.utils.draggables.HudManager;
import x7k2m9.base.utils.events.HudRenderHandler;
import x7k2m9.manager.Manager;
import x7k2m9.modules.ModuleManager;
import x7k2m9.rendersystem.render2d.blur.WorldFramebufferCapture;

public class p3nv8q implements ClientModInitializer {
    @Getter
    private static p3nv8q instance;

    public static final MinecraftClient mc = MinecraftClient.getInstance();

    @Getter
    private Manager manager;
    
    private void preloadCriticalClasses() {
        try {
            System.out.println("[p3nv8q] Preloading critical classes for DLL injection compatibility...");
            
            ClassLoader classLoader = this.getClass().getClassLoader();
            
            Class.forName("x7k2m9.base.utils.hook.CustomCamera", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.CustomInGameHud", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.HookType", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.Hook", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.CameraHook", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.InGameHudHook", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.GameRendererInitHook", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.EntityRenderDispatcherHook", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.EntityHitboxHook", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.ProfilerHook", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.ProfilerHookInjector", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.CustomTickTimeTracker", true, classLoader);
            Class.forName("x7k2m9.base.utils.hook.WorldRendererHook", true, classLoader);
            Class.forName("x7k2m9.base.render.CustomWorldRenderer", true, classLoader);
            
            Class.forName("x7k2m9.base.utils.events.EventManager", true, classLoader);
            Class.forName("x7k2m9.base.utils.events.Event", true, classLoader);
            
            System.out.println("[p3nv8q] Critical classes preloaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("[p3nv8q] Failed to preload class: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void onInitializeClient() {
        instance = this;
        
        manager = new Manager();
        manager.init();
        System.setProperty("java.awt.headless", "false");

        ModuleManager.initialize();
        KeyboardHandler.initialize();
        KeybindManager.initialize();
        x7k2m9.base.utils.events.PacketHandler.initialize();
        x7k2m9.base.utils.events.PacketHandler.initialize();

        System.out.println("[p3nv8q] Render optimization will be initialized on first render");

        HudManager.getInstance().initElements();

        HudRenderHandler.initialize();
        x7k2m9.base.utils.events.WorldRenderHandler.initialize();
        x7k2m9.base.utils.events.PacketHandler.initialize();

        x7k2m9.base.utils.friendsystem.FriendSystem.getInstance();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            manager.getEventManager().callEvent(new x7k2m9.base.utils.events.TickEvent());

            for (x7k2m9.modules.Module module : x7k2m9.modules.ModuleManager.getModules()) {
                if (module.isEnabled()) {
                    try {
                        module.onEndTick();
                    } catch (Exception e) {
                        System.err.println("[ClientTick] Error in module " + module.getName() + ": " + e.getMessage());
                    }
                }
            }

            if (client.getNetworkHandler() != null && client.getNetworkHandler().getConnection() != null) {
                x7k2m9.base.utils.hook.ClientConnectionHook hook = x7k2m9.base.utils.hook.HookManager.getClientConnectionHook();
                if (hook != null) {
                    hook.hookConnection(client.getNetworkHandler().getConnection());
                }
            }
        });

        net.fabricmc.fabric.api.event.player.AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient) {
                manager.getEventManager().callEvent(new x7k2m9.base.utils.events.AttackEvent(entity));

                for (x7k2m9.modules.Module module : x7k2m9.modules.ModuleManager.getModules()) {
                    if (module.isEnabled() && hitResult != null) {
                        module.onAttackEntity(player, world, hand, entity, hitResult);
                    }
                }
            }
            return net.minecraft.util.ActionResult.PASS;
        });
    }
}