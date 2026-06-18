package name.modid;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class WithdrawCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("withdraw")
            .then(CommandManager.argument("amount", IntegerArgumentType.integer(1))
            .executes(context -> execute(context, IntegerArgumentType.getInteger(context, "amount")))));
    }

    private static int execute(CommandContext<ServerCommandSource> context, int amount) {
        ServerCommandSource source = context.getSource();
        
        if (!source.isExecutedByPlayer()) {
            source.sendError(Text.literal("Only players can run this command!"));
            return 0;
        }

        ServerPlayerEntity player = source.getPlayer();
        EntityAttributeInstance maxHealthAttr = player.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);

        if (maxHealthAttr != null) {
            double currentMax = maxHealthAttr.getBaseValue();
            double healthToDraft = amount * 2.0;

            if (currentMax - healthToDraft <= 0) {
                source.sendError(Text.literal("You don't have enough hearts to withdraw that many!"));
                return 0;
            }

            double newMax = currentMax - healthToDraft;
            maxHealthAttr.setBaseValue(newMax);

            if (player.getHealth() > newMax) {
                player.setHealth((float) newMax);
            }

            // --- GEYSER-FRIENDLY ITEM CREATION ---
            ItemStack hearts = new ItemStack(Items.NETHER_STAR, amount);
            
            // Set Custom Name
            hearts.set(DataComponentTypes.CUSTOM_NAME, Text.literal("§c§lCustom Heart"));
            
            // Set Lore
            hearts.set(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("§7Right-click to consume and gain a heart slot!")
            )));

            if (!player.getInventory().insertStack(hearts)) {
                player.dropItem(hearts, false); 
            }

            source.sendFeedback(() -> Text.literal("§aYou withdrew " + amount + " heart(s)!"), false);
        }

        return 1;
    }
}