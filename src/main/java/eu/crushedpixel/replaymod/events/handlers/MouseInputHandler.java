package eu.crushedpixel.replaymod.events.handlers;

import eu.crushedpixel.replaymod.entities.CameraEntity;
import eu.crushedpixel.replaymod.replay.ReplayHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class MouseInputHandler {

    private final Minecraft mc = Minecraft.getMinecraft();
    private boolean rightDown = false;

    @SubscribeEvent
    public void mouseEvent(MouseEvent event) {
        if(!ReplayHandler.isInReplay()) {
            return;
        }

        if(event.dwheel != 0 && mc.currentScreen == null) {
            boolean increase = event.dwheel > 0;
            CameraEntity.modifyCameraSpeed(increase);
        }

        if(Mouse.isButtonDown(1)) {
            if(!rightDown) {
                rightDown = true;
                if(mc.pointedEntity != null && ReplayHandler.isCamera() && mc.currentScreen == null) {
                    if(mc.pointedEntity instanceof EntityLiving || mc.pointedEntity instanceof EntityItemFrame)
                        ReplayHandler.spectateEntity(mc.pointedEntity);
                }
            }
        } else {
            rightDown = false;
        }
    }
}