package dev.emi.trinkets.mixin;

import dev.emi.trinkets.api.TrinketsApi;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"),
            method = "chooseEquipmentWith(Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/entity/LivingEntity;Ljava/util/function/Predicate;)Ljava/util/Map$Entry;",
            locals = LocalCapture.CAPTURE_FAILHARD)
    private static void chooseEquipmentWith(Enchantment enchantment, LivingEntity entity, Predicate<ItemStack> condition,
                                            CallbackInfoReturnable<Map.Entry<EquipmentSlot, ItemStack>> info,
                                            Map<EquipmentSlot, ItemStack> map, List<Map.Entry<EquipmentSlot, ItemStack>> list) {
        if (enchantment == Enchantments.MENDING) {
            TrinketsApi.getTrinketComponent(entity).ifPresent(t -> t.getAllEquipped().stream()
                    .map(Pair::getRight)
                    .filter(stack -> EnchantmentHelper.getLevel(enchantment, stack) > 0)
                    .filter(condition)
                    .map(StrayEntry::new)
                    .forEachOrdered(list::add));
        }
    }

    private static class StrayEntry implements Map.Entry<EquipmentSlot, ItemStack> {

        private ItemStack stack;

        public StrayEntry(ItemStack stack) {
            this.stack = stack;
        }

        @Override
        public EquipmentSlot getKey() {
            return null;
        }

        @Override
        public ItemStack getValue() {
            return this.stack;
        }

        @Override
        public ItemStack setValue(ItemStack value) {
            ItemStack oldStack = this.stack;
            this.stack = value;
            return oldStack;
        }
    }
}
