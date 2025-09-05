package com.example.valorquest.data;

import com.example.valorquest.model.Equipment;

import java.util.Arrays;
import java.util.List;

public class EquipmentData {
    public static List<Equipment> getAllEquipment() {
        return Arrays.asList(
                // Potions
                new Equipment("p1", "Elixir of Might", "potion",
                        "A rare concoction brewed by the alchemists of Eldoria, granting a surge of strength for a single battle.", "power",
                        0.20, 1, 0.5, false),
                new Equipment("p2", "Potion of Heroic Strength", "potion",
                        "An ancient potion infused with the essence of fallen heroes, empowering the drinker for one epic clash.", "power",
                        0.40, 1, 0.7, false),
                new Equipment("p3", "Philosopher's Draught", "potion",
                        "A mystical elixir said to unlock latent potential, permanently enhancing your power.", "power",
                        0.05, -1, 2.0, false),
                new Equipment("p4", "Elixir of Eternal Might", "potion",
                        "A legendary potion of the ancients, bestowing enduring strength to those worthy of its power.", "power",
                        0.10, -1, 10.0, false),

                // Armor
                new Equipment("a1", "Gauntlets of the Titan", "armor",
                        "Forged in the volcanic forges of the Titan Mountains, these gauntlets grant immense might for two battles.", "power",
                        0.10, 2, 0.6, false),
                new Equipment("a2", "Aegis of Precision", "armor",
                        "A shield blessed by the gods of war, increasing the chance of landing critical strikes for two battles.", "attackChance",
                        0.10, 2, 0.6, false),
                new Equipment("a3", "Boots of the Swift Strike", "armor",
                        "Lightfoot boots imbued with the speed of the wind spirits, allowing a chance for extra attacks over two battles.", "extraAttack",
                        0.40, 2, 0.8, false),

                // Weapons
                new Equipment("w1", "Sword of Eternal Valor", "weapon",
                        "A sword carried by legendary champions, permanently increasing the wielder's power with every strike.", "power",
                        0.05, -1, 0.0, true),
                new Equipment("w2", "Bow of Golden Fortune", "weapon",
                        "A mystical bow blessed by the spirits of wealth, permanently increasing the coins earned from victorious battles.", "money",
                        0.05, -1, 0.0, true)
        );
    }
}
