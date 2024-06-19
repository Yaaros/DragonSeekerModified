package syric.dragonseeker.item.tool;

import com.github.alexthe666.iceandfire.IceAndFire;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import syric.dragonseeker.DragonseekerConfig;

import java.util.List;

public class dragonseekerGeneric extends Item {

    //Defining statistics
    //Ping chance stats
    private int opDist;
    private int maxDist;
    private double minPing;
    private double maxPing;

    //Ping characteristic stats
    private int minSig;
    private double pow;
    private float minVol;
    private float maxVol;
    private float minPitch;
    private float maxPitch;
    private final SoundEvent negSound;
    private final SoundEvent pingSound;

    //Other stats
    private boolean detectsCorpses;
    private boolean detectsTame;
    private int durability;
    private Rarity rarity;
    private final Item repairItem;
    private final int seekerType;
    private boolean isDefault;

    //Constructor
//    public dragonseekerGeneric() {
//        super(new Properties()
//                .stacksTo(1)
//                .tab(IceAndFire.TAB_ITEMS)
//        );
//    }

    public dragonseekerGeneric(int opDistIn, int maxDistIn, double minPingIn, double maxPingIn, int minSigIn, double powIn, double minVolIn, double maxVolIn, double minPitchIn, double maxPitchIn, SoundEvent negSoundIn, SoundEvent pingSoundIn, boolean detectsCorpsesIn, boolean detectsTameIn, int durabilityIn, Rarity rarityIn, Item repairItemIn, int seekerTypeIn) {
        super(new Properties()
                .stacksTo(1)
                .durability(durabilityIn)
                .rarity(rarityIn)
        );
        opDist = opDistIn;
        maxDist = maxDistIn;
        minPing = minPingIn;
        maxPing = maxPingIn;
        minSig = minSigIn;
        pow = powIn;
        minVol = (float) minVolIn;
        maxVol = (float) maxVolIn;
        minPitch = (float) minPitchIn;
        maxPitch = (float) maxPitchIn;
        negSound = negSoundIn;
        pingSound = pingSoundIn;
        detectsCorpses = detectsCorpsesIn;
        detectsTame = detectsTameIn;
        durability = durabilityIn;
        rarity = rarityIn;
        repairItem = repairItemIn;
        seekerType = seekerTypeIn;
        isDefault = true;
    }


    //Repairing
    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repair.getItem() == repairItem;
    }


    //Using the Item
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @NotNull InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

//        String s1 = "Checking for entities";
//        player.displayClientMessage(Component.literal(s1), false);
//
//        AABB box2 = new AABB(player.getX() - 3, player.getY() - 2, player.getZ() - 3, player.getX() + 3, player.getY() + 2, player.getZ() + 3);
//        List<LivingEntity> allEntities = world.getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT, player, box2);
//        for (LivingEntity entity : allEntities) {
//            String s2 = String.format("Entity found: %s", entity.getType());
//            player.displayClientMessage(Component.literal(s2), false);
//        }

        if (!world.isClientSide) {
            if (isDefault) {
                importConfig();
                isDefault = false;
            }
            itemstack.hurtAndBreak(1, player, (entity) -> player.broadcastBreakEvent(player.getUsedItemHand()));

            double distance = getDistance(world, player);

            //printDistance(distance, world, player);

            return InteractionResultHolder.sidedSuccess(itemstack, world.isClientSide());
        }
        return InteractionResultHolder.fail(itemstack);
    }


    //methods
    private double getDistance(Level world, Player player) {
        EntityDragonBase closest = switch (seekerType){
            case 1 -> getEntityDragonBase(100,50,world, player);
            case 2 -> getEntityDragonBase(200,100,world,player);
            case 3 -> getEntityDragonBase(300,150,world,player);
            case 4 -> getEntityDragonBase(400,200,world,player);
            default -> null;
        };
        float min = closest!=null?closest.distanceTo(player):-1;
        display(player, closest, min);
        return min;
    }

    private void display(Player player, EntityDragonBase closest, float min) {
        String s = "";
        if (closest != null) {
            s = Math.round(min) + ", x=" + (int) closest.getX() + ", y="+ (int) closest.getY() + ", z=" + (int) closest.getZ();
        } else {
            s = "No dragon found";
        }
        player.displayClientMessage(new TextComponent(s), false);
    }

    @Nullable
    private EntityDragonBase getEntityDragonBase(int delta_xz,int delta_y,Level world, Player player) {
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        float min = 0;
        AABB box = new AABB(x-delta_xz,y-delta_y,z-delta_xz,
                x+delta_xz,y+delta_y,z+delta_xz);
        List<EntityDragonBase> listOfTargets = world.getEntitiesOfClass(EntityDragonBase.class, box);
//        String s = "Identified " + listOfTargets.size() + " dragons.";
//        player.displayClientMessage(Component.literal(s), false);
//
//        List<LivingEntity> allEntities = world.getNearbyEntities(LivingEntity.class, TargetingConditions.DEFAULT, player, box);
//        s = "Identified " + allEntities.size() + " total entities.";
//        player.displayClientMessage(Component.literal(s), false);


        EntityDragonBase closest = null;
        for (EntityDragonBase target : listOfTargets) {
            if ((detectsCorpses || !target.isModelDead()) && (detectsTame || !target.isTame())) {
                float distance = target.distanceTo(player);
                if ((min == 0) || distance < min) {
                    min = distance;
                    closest = target;
//                    s = "Found dragon, updating minimum distance";
//                    player.displayClientMessage(Component.literal(s), false);
                } else if (distance >= min) {
//                    s = "Found further dragon, ignoring";
//                    player.displayClientMessage(Component.literal(s), false);
                }
            } else if (!detectsCorpses && target.isModelDead()) {
//                s = "Found corpse, ignoring";
//                player.displayClientMessage(Component.literal(s), false);
            } else if (!detectsTame && target.isTame()) {
//                s = "Found tamed dragon, ignoring";
//                player.displayClientMessage(Component.literal(s), false);
            }
        }
        return closest;
    }


    private void printDistance(double distance, Level world, Player player) {
        if (!world.isClientSide) {
            int distancenew = (int) Math.round(distance);
            String s = String.valueOf(distancenew);
            player.displayClientMessage(Component.literal(s), false);
        }
    }

    public void importConfig() {
        if (seekerType == 1) {
            opDist = DragonseekerConfig.COMMON.basic_optimalDistance.get();
            maxDist = DragonseekerConfig.COMMON.basic_maxDistance.get();
            minPing = DragonseekerConfig.COMMON.basic_minPingChance.get();
            maxPing = DragonseekerConfig.COMMON.basic_maxPingChance.get();

            //Ping characteristic stats
            minSig = DragonseekerConfig.COMMON.basic_pingCapRadius.get();
            pow = DragonseekerConfig.COMMON.basic_sigPower.get();
            minVol = DragonseekerConfig.COMMON.basic_minVol.get().floatValue();
            maxVol = DragonseekerConfig.COMMON.basic_maxVol.get().floatValue();
            minPitch = DragonseekerConfig.COMMON.basic_minPitch.get().floatValue();
            maxPitch = DragonseekerConfig.COMMON.basic_maxPitch.get().floatValue();

            //Other stats
            detectsCorpses = DragonseekerConfig.COMMON.basic_detectsCorpses.get();
            detectsTame = DragonseekerConfig.COMMON.basic_detectsTame.get();
            isDefault = false;

        } else if (seekerType == 2) {
            opDist = DragonseekerConfig.COMMON.epic_optimalDistance.get();
            maxDist = DragonseekerConfig.COMMON.epic_maxDistance.get();
            minPing = DragonseekerConfig.COMMON.epic_minPingChance.get();
            maxPing = DragonseekerConfig.COMMON.epic_maxPingChance.get();

            //Ping characteristic stats
            minSig = DragonseekerConfig.COMMON.epic_pingCapRadius.get();
            pow = DragonseekerConfig.COMMON.epic_sigPower.get();
            minVol = DragonseekerConfig.COMMON.epic_minVol.get().floatValue();
            maxVol = DragonseekerConfig.COMMON.epic_maxVol.get().floatValue();
            minPitch = DragonseekerConfig.COMMON.epic_minPitch.get().floatValue();
            maxPitch = DragonseekerConfig.COMMON.epic_maxPitch.get().floatValue();

            //Other stats
            detectsCorpses = DragonseekerConfig.COMMON.epic_detectsCorpses.get();
            detectsTame = DragonseekerConfig.COMMON.epic_detectsTame.get();
            isDefault = false;

        } else if (seekerType == 3) {
            opDist = DragonseekerConfig.COMMON.legendary_optimalDistance.get();
            maxDist = DragonseekerConfig.COMMON.legendary_maxDistance.get();
            minPing = DragonseekerConfig.COMMON.legendary_minPingChance.get();
            maxPing = DragonseekerConfig.COMMON.legendary_maxPingChance.get();

            //Ping characteristic stats
            minSig = DragonseekerConfig.COMMON.legendary_pingCapRadius.get();
            pow = DragonseekerConfig.COMMON.legendary_sigPower.get();
            minVol = DragonseekerConfig.COMMON.legendary_minVol.get().floatValue();
            maxVol = DragonseekerConfig.COMMON.legendary_maxVol.get().floatValue();
            minPitch = DragonseekerConfig.COMMON.legendary_minPitch.get().floatValue();
            maxPitch = DragonseekerConfig.COMMON.legendary_maxPitch.get().floatValue();

            //Other stats
            detectsCorpses = DragonseekerConfig.COMMON.legendary_detectsCorpses.get();
            detectsTame = DragonseekerConfig.COMMON.legendary_detectsTame.get();
            isDefault = false;

        } else if (seekerType == 4) {
            opDist = DragonseekerConfig.COMMON.mythic_optimalDistance.get();
            maxDist = DragonseekerConfig.COMMON.mythic_maxDistance.get();
            minPing = DragonseekerConfig.COMMON.mythic_minPingChance.get();
            maxPing = DragonseekerConfig.COMMON.mythic_maxPingChance.get();

            //Ping characteristic stats
            minSig = DragonseekerConfig.COMMON.mythic_pingCapRadius.get();
            pow = DragonseekerConfig.COMMON.mythic_sigPower.get();
            minVol = DragonseekerConfig.COMMON.mythic_minVol.get().floatValue();
            maxVol = DragonseekerConfig.COMMON.mythic_maxVol.get().floatValue();
            minPitch = DragonseekerConfig.COMMON.mythic_minPitch.get().floatValue();
            maxPitch = DragonseekerConfig.COMMON.mythic_maxPitch.get().floatValue();

            //Other stats
            detectsCorpses = DragonseekerConfig.COMMON.mythic_detectsCorpses.get();
            detectsTame = DragonseekerConfig.COMMON.mythic_detectsTame.get();
            isDefault = false;
        }
    }

}
