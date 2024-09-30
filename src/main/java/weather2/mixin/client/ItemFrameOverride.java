package weather2.mixin.client;

import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import weather2.WeatherItems;

import java.util.OptionalInt;

@Mixin(ItemFrame.class)
public class ItemFrameOverride {

    @Shadow
    public ItemStack getItem() {
        return null;
    }

    @Inject(method = "getFramedMapId", at = @At("HEAD"), cancellable = true)
    public void injectGetFramedMapId(CallbackInfoReturnable<OptionalInt> cir) {
        cir.cancel();
        ItemStack itemstack = this.getItem();
        if (itemstack.is(Items.FILLED_MAP) || itemstack.is(WeatherItems.RADAR_MAP.get())) {
            Integer integer = MapItem.getMapId(itemstack);
            if (integer != null) {
                cir.setReturnValue(OptionalInt.of(integer));
                return;
            }
        }

        cir.setReturnValue(OptionalInt.empty());
    }
}
