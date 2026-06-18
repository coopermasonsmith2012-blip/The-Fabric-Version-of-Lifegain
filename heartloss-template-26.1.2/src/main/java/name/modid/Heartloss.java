package name.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;

public class Heartloss implements ModInitializer {

    public static final String MOD_ID = "heartloss";

    @Override
    public void onInitialize() {
        // Register the /withdraw command
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            WithdrawCommand.register(dispatcher);
        });

        // Listen for right-clicks (Geyser-friendly approach!)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack itemStack = player.getStackInHand(hand);

            // Check if it's a Nether Star and has our custom name
            if (itemStack.isOf(Items.NETHER_STAR) && itemStack.getName().getString().contains("Custom Heart")) {
                
                if (!world.isClient) {
                    EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
                    
                    if (maxHealthAttr != null) {
                        double currentMax = maxHealthAttr.getBaseValue();

                        if (currentMax >= 30.0) {
                            player.sendMessage(Text.literal("§cYou cannot use hearts if you are at or above 15 hearts!"), true);
                            return TypedActionResult.fail(itemStack);
                        }

                        // Add a heart
                        maxHealthAttr.setBaseValue(currentMax + 2.0);
                        player.heal(2.0f);
                        player.sendMessage(Text.literal("§aSuccessfully consumed a heart!"), true);

                        // Consume item if not in creative
                        if (!player.isCreative()) {
                            itemStack.decrement(1);
                        }
                    }
                }
                // Stop the normal Nether Star click behavior
                return TypedActionResult.success(itemStack, world.isClient());
            }

            return TypedActionResult.pass(itemStack);
        });

        System.out.println("Heartloss Mod has initialized completely!");
    }
}