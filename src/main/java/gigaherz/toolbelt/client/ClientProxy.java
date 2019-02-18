package gigaherz.toolbelt.client;

import gigaherz.toolbelt.BeltFinder;
import gigaherz.toolbelt.ISideProxy;
import gigaherz.toolbelt.ToolBelt;
import gigaherz.toolbelt.network.BeltContentsChange;
import gigaherz.toolbelt.network.ContainerSlotsHack;
import gigaherz.toolbelt.network.SyncBeltSlotContents;
import gigaherz.toolbelt.slot.ExtensionSlotBelt;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.glfw.GLFW;

import java.util.Map;

public class ClientProxy implements ISideProxy
{
    public static KeyBinding keyOpenToolMenu;
    public static KeyBinding keyCycleToolMenuL;
    public static KeyBinding keyCycleToolMenuR;

    public static KeyBinding keyOpenBeltSlot;

    public static void wipeOpen()
    {
        while (keyOpenToolMenu.isPressed())
        {
        }
    }

    @Override
    public void init()
    {
        ClientRegistry.registerKeyBinding(keyOpenToolMenu =
                new KeyBinding("key.toolbelt.open", GLFW.GLFW_KEY_R, "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(keyCycleToolMenuL =
                new KeyBinding("key.toolbelt.cycle.left", -1, "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(keyCycleToolMenuR =
                new KeyBinding("key.toolbelt.cycle.right", -1, "key.toolbelt.category"));

        ClientRegistry.registerKeyBinding(keyOpenBeltSlot =
                new KeyBinding("key.toolbelt.slot", GLFW.GLFW_KEY_V, "key.toolbelt.category"));

        Map<String, RenderPlayer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();

        RenderPlayer render = skinMap.get("default");
        render.addLayer(new LayerToolBelt(render));

        render = skinMap.get("slim");
        render.addLayer(new LayerToolBelt(render));
    }

    @Override
    public void handleBeltContentsChange(final BeltContentsChange message)
    {
        Minecraft.getInstance().addScheduledTask(() -> {
            Entity entity = Minecraft.getInstance().world.getEntityByID(message.player);
            if (!(entity instanceof EntityPlayer))
                return;
            EntityPlayer player = (EntityPlayer) entity;
            switch (message.where)
            {
                case MAIN:
                    player.inventory.setInventorySlotContents(message.slot, message.stack);
                    break;
                case BELT_SLOT:
                    BeltFinder.instances.forEach((i) -> i.setToBeltSlot(player, message.stack));
                    break;
                case BAUBLES:
                    BeltFinder.instances.forEach((i) -> i.setToBaubles(player, message.slot, message.stack));
                    break;
            }
        });
    }

    @Override
    public void handleBeltSlotContents(SyncBeltSlotContents message)
    {
        Minecraft.getInstance().addScheduledTask(() -> {
            Entity entity = Minecraft.getInstance().world.getEntityByID(message.entityId);
            if (entity instanceof EntityPlayer)
            {
                ExtensionSlotBelt.get((EntityLivingBase) entity).setAll(message.stacks);
            }
        });
    }
}
