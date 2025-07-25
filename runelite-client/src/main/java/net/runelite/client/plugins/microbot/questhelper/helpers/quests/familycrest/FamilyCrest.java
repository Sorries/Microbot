/*
 * Copyright (c) 2020, Zoinkwiz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.familycrest;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.ObjectCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemOnTileRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SkillRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class FamilyCrest extends BasicQuestHelper
{
	//Items Required
	ItemRequirement shrimp, salmon, tuna, bass, swordfish, pickaxe, ruby, ruby2, ringMould, necklaceMould, antipoison, runesForBlasts, gold2, gold,
		perfectRing, perfectNecklace, goldBar, goldBar2, crestPiece1, crestPiece2, crestPiece3, crest;

	// Items Recommended
	ItemRequirement varrockTele, faladorTele, ardyTele, alkharidTele, catherbyTele, dwarvenMineTele;

	Requirement inDwarvenMines, inHobgoblinDungeon, northWallUp, southRoomUp, northRoomUp, northWallDown, southRoomDown, northRoomDown,
		inJollyBoar, inEdgevilleDungeon, crest3Nearby;

	NpcStep talkToDimintheis, talkToCaleb, talkToCalebWithFish, talkToCalebOnceMore, talkToGemTrader, talkToMan;
	ObjectStep enterDwarvenMine;
	NpcStep talkToBoot;
	ObjectStep enterWitchavenDungeon, pullNorthLever, pullSouthRoomLever, pullNorthLeverAgain, pullNorthRoomLever, pullNorthLever3, pullSouthRoomLever2;
	QuestStep followPathAroundEast, mineGold;
	ObjectStep smeltGold;
	QuestStep makeRing, makeNecklace, returnToMan;
	ObjectStep goUpToJohnathon;
	QuestStep talkToJohnathon, giveJohnathonAntipoison,
		killChronizon, pickUpCrest3, repairCrest, returnCrest;

	ObjectStep goDownToChronizon;

	//Zones
	Zone dwarvenMines, hobgoblinDungeon, jollyBoar, edgevilleDungeon;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		steps.put(0, talkToDimintheis);
		steps.put(1, talkToCaleb);
		steps.put(2, talkToCalebWithFish);
		steps.put(3, talkToCalebOnceMore);
		steps.put(4, talkToGemTrader);
		steps.put(5, talkToMan);

		ConditionalStep goTalkToBoot = new ConditionalStep(this, enterDwarvenMine);
		goTalkToBoot.addStep(inDwarvenMines, talkToBoot);

		steps.put(6, goTalkToBoot);

		ConditionalStep getGold = new ConditionalStep(this, enterWitchavenDungeon);
		getGold.addStep(new Conditions(perfectNecklace.alsoCheckBank(questBank), perfectRing.alsoCheckBank(questBank)), returnToMan);
		getGold.addStep(perfectNecklace.alsoCheckBank(questBank), makeRing);
		getGold.addStep(new Conditions(gold.alsoCheckBank(questBank), goldBar.alsoCheckBank(questBank)), smeltGold);
		getGold.addStep(goldBar.alsoCheckBank(questBank), makeNecklace);
		getGold.addStep(gold2.alsoCheckBank(questBank), smeltGold);
		getGold.addStep(new Conditions(northRoomUp, southRoomDown), mineGold);
		getGold.addStep(new Conditions(northRoomUp, northWallUp), pullSouthRoomLever2);
		getGold.addStep(new Conditions(northRoomUp, northWallDown), pullNorthLever3);
		getGold.addStep(new Conditions(northWallDown, southRoomUp), pullNorthRoomLever);
		getGold.addStep(southRoomUp, pullNorthLeverAgain);
		getGold.addStep(northWallUp, pullSouthRoomLever);
		getGold.addStep(northWallDown, pullNorthLever);
		getGold.addStep(inHobgoblinDungeon, followPathAroundEast);

		steps.put(7, getGold);

		ConditionalStep goTalkToJohnathon = new ConditionalStep(this, goUpToJohnathon);
		goTalkToJohnathon.addStep(inJollyBoar, talkToJohnathon);

		steps.put(8, goTalkToJohnathon);

		ConditionalStep goGiveAntipoisonToJohnathon = new ConditionalStep(this, goUpToJohnathon);
		goGiveAntipoisonToJohnathon.addStep(inJollyBoar, giveJohnathonAntipoison);

		steps.put(9, goGiveAntipoisonToJohnathon);

		ConditionalStep goKillChronizon = new ConditionalStep(this, goDownToChronizon);
		goKillChronizon.addStep(crest.alsoCheckBank(questBank), returnCrest);
		goKillChronizon.addStep(crestPiece3.alsoCheckBank(questBank), repairCrest);
		goKillChronizon.addStep(crest3Nearby, pickUpCrest3);
		goKillChronizon.addStep(inEdgevilleDungeon, killChronizon);

		steps.put(10, goKillChronizon);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		// Recommended
		varrockTele = new ItemRequirement("Varrock Teleports", ItemID.POH_TABLET_VARROCKTELEPORT, 2);
		faladorTele = new ItemRequirement("Falador Teleport", ItemID.POH_TABLET_FALADORTELEPORT);
		ardyTele = new ItemRequirement("Ardougne Teleport", ItemID.POH_TABLET_ARDOUGNETELEPORT);
		alkharidTele = new ItemRequirement("Al-Kharid Teleport", ItemCollections.RING_OF_DUELINGS, 2);
		alkharidTele.setChargedItem(true);
		catherbyTele = new ItemRequirement("Camelot/Catherby Teleport", ItemID.LUNAR_TABLET_CATHERBY_TELEPORT);
		dwarvenMineTele = new ItemRequirement("Teleport to the Dwarven Mine (Combat Bracelet [3], Skills Necklace [2])", ItemCollections.SKILLS_NECKLACES);
		dwarvenMineTele.addAlternates(ItemCollections.COMBAT_BRACELETS);

		varrockTele.addAlternates(ItemID.SKILLCAPE_AD, ItemID.SKILLCAPE_AD_TRIMMED);
		varrockTele.addAlternates(ItemCollections.RING_OF_WEALTHS);
		ardyTele.addAlternates(ItemCollections.ARDY_CLOAKS);
		alkharidTele.addAlternates(ItemCollections.AMULET_OF_GLORIES);
		catherbyTele.addAlternates(ItemID.POH_TABLET_CAMELOTTELEPORT);

		// Required
		shrimp = new ItemRequirement("Shrimps", ItemID.SHRIMP);
		salmon = new ItemRequirement("Salmon", ItemID.SALMON);
		tuna = new ItemRequirement("Tuna", ItemID.TUNA);
		bass = new ItemRequirement("Bass", ItemID.BASS);
		swordfish = new ItemRequirement("Swordfish", ItemID.SWORDFISH);

		pickaxe = new ItemRequirement("Any pickaxe", ItemCollections.PICKAXES).isNotConsumed();
		ruby = new ItemRequirement("Ruby", ItemID.RUBY);
		ruby2 = new ItemRequirement("Ruby", ItemID.RUBY, 2);
		ringMould = new ItemRequirement("Ring mould", ItemID.RING_MOULD).isNotConsumed();
		necklaceMould = new ItemRequirement("Necklace mould", ItemID.NECKLACE_MOULD).isNotConsumed();

		antipoison = new ItemRequirement("At least one dose of antipoison or superantipoison", ItemCollections.ANTIPOISONS);

		runesForBlasts = new ItemRequirement("Runes for casting each of the 4 blast spells", -1, -1);
		runesForBlasts.setDisplayItemId(ItemID.DEATHRUNE);

		gold = new ItemRequirement("'perfect' gold ore", ItemID.PERFECT_GOLD_ORE);
		gold2 = new ItemRequirement("'perfect' gold ore", ItemID.PERFECT_GOLD_ORE, 2);
		goldBar = new ItemRequirement("'perfect' gold bar", ItemID.PERFECT_GOLD_BAR);
		goldBar2 = new ItemRequirement("'perfect' gold bar", ItemID.PERFECT_GOLD_BAR, 2);

		perfectRing = new ItemRequirement("'perfect' ring", ItemID.PERFECT_RUBY_RING);
		perfectNecklace = new ItemRequirement("'perfect' necklace", ItemID.PERFECT_RUBY_NECKLACE);

		crest = new ItemRequirement("Family crest", ItemID.FAMILY_CREST);
		crestPiece1 = new ItemRequirement("Crest part", ItemID.AVAN_CREST);
		crestPiece1.setTooltip("You can get another from Caleb in Catherby");
		crestPiece2 = new ItemRequirement("Crest part", ItemID.CALEB_CREST);
		crestPiece2.setTooltip("You can get another from Avan north of Al Kharid");
		crestPiece3 = new ItemRequirement("Crest part", ItemID.JOHNATHON_CREST);
	}

	@Override
	protected void setupZones()
	{
		dwarvenMines = new Zone(new WorldPoint(2960, 9696, 0), new WorldPoint(3062, 9854, 0));
		hobgoblinDungeon = new Zone(new WorldPoint(2691, 9665, 0), new WorldPoint(2749, 9720, 0));
		jollyBoar = new Zone(new WorldPoint(3271, 3485, 1), new WorldPoint(3288, 3511, 1));
		edgevilleDungeon = new Zone(new WorldPoint(3073, 9820, 0), new WorldPoint(3287, 10000, 0));
	}

	public void setupConditions()
	{
		inDwarvenMines = new ZoneRequirement(dwarvenMines);
		inHobgoblinDungeon = new ZoneRequirement(hobgoblinDungeon);
		northWallUp = new ObjectCondition(ObjectID.LEVERG2, new WorldPoint(2722, 9710, 0));
		southRoomUp = new ObjectCondition(ObjectID.LEVERH2, new WorldPoint(2724, 9669, 0));
		northRoomUp = new ObjectCondition(ObjectID.LEVERI2, new WorldPoint(2722, 9718, 0));

		northWallDown = new ObjectCondition(ObjectID.LEVERG, new WorldPoint(2722, 9710, 0));
		southRoomDown = new ObjectCondition(ObjectID.LEVERH, new WorldPoint(2724, 9669, 0));
		northRoomDown = new ObjectCondition(ObjectID.LEVERI, new WorldPoint(2722, 9718, 0));

		inJollyBoar = new ZoneRequirement(jollyBoar);

		inEdgevilleDungeon = new ZoneRequirement(edgevilleDungeon);

		crest3Nearby = new ItemOnTileRequirement(crestPiece3);
	}

	public void setupSteps()
	{
		talkToDimintheis = new NpcStep(this, NpcID.DIMINTHEIS, new WorldPoint(3280, 3402, 0), "Talk to Dimintheis in south east Varrock.");
		talkToDimintheis.addDialogStep("Why would a nobleman live in a dump like this?");
		talkToDimintheis.addDialogStep("So where is this crest?");
		talkToDimintheis.addDialogStep("Ok, I will help you.");

		talkToCaleb = new NpcStep(this, NpcID.CALEB_FITZHARMON_1OP, new WorldPoint(2819, 3452, 0), "Talk to Caleb in Catherby.");
		talkToCaleb.addDialogStep("Are you Caleb Fitzharmon?");
		talkToCaleb.addDialogStep("So can I have your bit?");
		talkToCaleb.addDialogStep("Ok, I will get those.");
		talkToCaleb.addTeleport(catherbyTele);
		talkToCalebWithFish = new NpcStep(this, NpcID.CALEB_FITZHARMON_1OP, new WorldPoint(2819, 3452, 0),
			"Talk to Caleb again with the required fish.", shrimp, salmon, tuna, bass, swordfish);

		talkToCalebOnceMore = new NpcStep(this, NpcID.CALEB_FITZHARMON_1OP, new WorldPoint(2819, 3452, 0), "Talk to Caleb in " +
			"Catherby once more.");
		talkToCalebOnceMore.addDialogStep("Uh.. what happened to the rest of the crest?");

		talkToGemTrader = new NpcStep(this, NpcID.GEM_TRADER, new WorldPoint(3286, 3211, 0), "Talk to the Gem Trader in Al Kharid.");
		talkToGemTrader.addDialogStep("I'm in search of a man named Avan Fitzharmon.");
		talkToGemTrader.addTeleport(alkharidTele.quantity(1));
		talkToMan = new NpcStep(this, NpcID.AVAN_FITZHARMON_MAN, new WorldPoint(3295, 3275, 0), "Talk to the man south of the Al Kharid mine.");
		talkToMan.addDialogStep("I'm looking for a man named Avan Fitzharmon.");
		enterDwarvenMine = new ObjectStep(this, ObjectID.FAI_DWARF_TRAPDOOR_DOWN, new WorldPoint(3019, 3450, 0),
			"Talk to Boot in the south western Dwarven Mines.");
		enterDwarvenMine.addTeleport(dwarvenMineTele);
		talkToBoot = new NpcStep(this, NpcID.BOOT_THE_DWARF, new WorldPoint(2984, 9810, 0), "Talk to Boot in the south western Dwarven Mines.");
		talkToBoot.addDialogStep("Hello. I'm in search of very high quality gold.");
		talkToBoot.addSubSteps(enterDwarvenMine);

		enterWitchavenDungeon = new ObjectStep(this, ObjectID.SLUG2_RUIN_ENTRANCE, new WorldPoint(2696, 3283, 0),
			"Enter the old ruin entrance west of Witchaven.");
		enterWitchavenDungeon.addTeleport(ardyTele);

		pullNorthLever = new ObjectStep(this, ObjectID.LEVERG, new WorldPoint(2722, 9710, 0),
			"Follow the path around, and pull the lever on the wall in the north east corner.");
		pullSouthRoomLever = new ObjectStep(this, ObjectID.LEVERH, new WorldPoint(2724, 9669, 0), "Pull the lever in the south room up.");

		pullNorthLeverAgain = new ObjectStep(this, ObjectID.LEVERG2, new WorldPoint(2722, 9710, 0), "Pull the north wall lever again.");

		pullNorthRoomLever = new ObjectStep(this, ObjectID.LEVERI, new WorldPoint(2722, 9718, 0), "Pull the lever in the north room up.");

		pullNorthLever3 = new ObjectStep(this, ObjectID.LEVERG, new WorldPoint(2722, 9710, 0), "Pull the north wall lever again.");

		pullSouthRoomLever2 = new ObjectStep(this, ObjectID.LEVERH2, new WorldPoint(2724, 9669, 0), "Pull the lever in the south room down.");

		followPathAroundEast = new DetailedQuestStep(this, new WorldPoint(2721, 9700, 0), "Follow the dungeon around to the east.");

		mineGold = new ObjectStep(this, ObjectID.GOLDROCK2, new WorldPoint(2732, 9680, 0),
			"Mine 2 perfect gold in the east room.", true, pickaxe, gold2);
		((ObjectStep) mineGold).setMaxObjectDistance(5000);

		smeltGold = new ObjectStep(this, ObjectID.FAI_FALADOR_FURNACE, new WorldPoint(3273, 3186, 0), "Smelt the perfect gold ore into bars.", gold2.highlighted());
		smeltGold.addIcon(ItemID.GOLD_ORE);
		smeltGold.addTeleport(alkharidTele.quantity(1));

		makeNecklace = new ObjectStep(this, ObjectID.FAI_FALADOR_FURNACE, "Make a perfect ruby necklace at a furnace. Make sure to only craft one.", goldBar, ruby, necklaceMould);
		makeRing = new ObjectStep(this, ObjectID.FAI_FALADOR_FURNACE, "Make a perfect ruby ring at a furnace. Make sure to only craft one.", goldBar, ruby, ringMould);

		returnToMan = new NpcStep(this, NpcID.AVAN_FITZHARMON_AVAN_1OP, new WorldPoint(3295, 3275, 0),
			"Return to the man south of the Al Kharid mine.", perfectRing, perfectNecklace);

		goUpToJohnathon = new ObjectStep(this, ObjectID.FAI_VARROCK_STAIRS_TALLER, new WorldPoint(3286, 3494, 0),
			"Go upstairs in the Jolly Boar Inn north east of Varrock and talk to Johnathon.", antipoison);
		goUpToJohnathon.addTeleport(varrockTele.quantity(1));

		talkToJohnathon = new NpcStep(this, NpcID.JOHNATHON_FITZHARMON_1OP, new WorldPoint(3277, 3504, 1), "Talk to Johnathon.", antipoison);
		giveJohnathonAntipoison = new NpcStep(this, NpcID.JOHNATHON_FITZHARMON_1OP, new WorldPoint(3277, 3504, 1),
			"Give Johnathon some antipoison.", antipoison.highlighted());
		giveJohnathonAntipoison.addIcon(ItemID._3DOSEANTIPOISON);

		goUpToJohnathon.addSubSteps(talkToJohnathon);

		goDownToChronizon = new ObjectStep(this, ObjectID.TRAPDOOR_OPEN, new WorldPoint(3097, 3468, 0),
			"Enter the Edgeville Wilderness Dungeon, ready to kill Chronozon. Other players will be able to attack you.", runesForBlasts);
		goDownToChronizon.addAlternateObjects(ObjectID.TRAPDOOR);

		killChronizon = new NpcStep(this, NpcID.CHRONOZON, new WorldPoint(3087, 9936, 0),
			"Kill Chronozon in the south west corner of the Edgeville Wilderness Dungeon. You need to hit him at least once with all 4 elemental blast spell.",
			runesForBlasts);
		killChronizon.addSubSteps(goDownToChronizon);

		pickUpCrest3 = new ItemStep(this, "Pick up the crest part.", crestPiece3);
		killChronizon.addSubSteps(pickUpCrest3);

		repairCrest = new DetailedQuestStep(this, "Combine the 3 crest parts together.", crestPiece1.highlighted(), crestPiece2.highlighted(), crestPiece3.highlighted());

		returnCrest = new NpcStep(this, NpcID.DIMINTHEIS, new WorldPoint(3280, 3402, 0),
			"Return the family crest to Dimintheis in south east Varrock.", crest);
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(shrimp);
		reqs.add(salmon);
		reqs.add(tuna);
		reqs.add(bass);
		reqs.add(swordfish);
		reqs.add(pickaxe);
		reqs.add(ruby2);
		reqs.add(ringMould);
		reqs.add(necklaceMould);
		reqs.add(antipoison);
		reqs.add(runesForBlasts);
		return reqs;
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(varrockTele, catherbyTele, faladorTele, ardyTele, alkharidTele, dwarvenMineTele);
	}

	@Override
	public List<String> getNotes()
	{
		ArrayList<String> reqs = new ArrayList<>();
		reqs.add("The final boss of this quest is in the Edgeville WILDERNESS dungeon, where other players can kill " +
			"you. Make sure when you go there you aren't risking anything you'd not be willing to lose.");
		return reqs;
	}

	@Override
	public List<String> getCombatRequirements()
	{
		ArrayList<String> reqs = new ArrayList<>();
		reqs.add("Chronozon (level 170, in the Wilderness)");
		return reqs;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(1);
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Collections.singletonList(new ItemReward("A pair of Steel Gauntlets", ItemID.STEEL_GAUNTLETS, 1));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Starting off", Collections.singletonList(talkToDimintheis)));
		allSteps.add(new PanelDetails("Caleb's piece", Arrays.asList(talkToCaleb, talkToCalebWithFish, talkToCalebOnceMore), shrimp, salmon, tuna, bass, swordfish));
		allSteps.add(new PanelDetails("Avan's piece", Arrays.asList(talkToGemTrader, talkToMan, talkToBoot, enterWitchavenDungeon, pullNorthLever,
			pullSouthRoomLever, pullNorthLeverAgain, pullNorthRoomLever, pullNorthLever3, pullSouthRoomLever2, mineGold, smeltGold, makeNecklace, makeRing, returnToMan),
			pickaxe, ruby2, necklaceMould, ringMould));
		allSteps.add(new PanelDetails("Johnathon's piece", Arrays.asList(goUpToJohnathon, giveJohnathonAntipoison, killChronizon),
			runesForBlasts, antipoison));
		allSteps.add(new PanelDetails("Return the crest", Arrays.asList(repairCrest, returnCrest)));
		return allSteps;
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> req = new ArrayList<>();
		req.add(new SkillRequirement(Skill.MINING, 40, true));
		req.add(new SkillRequirement(Skill.SMITHING, 40, true));
		req.add(new SkillRequirement(Skill.MAGIC, 59, true));
		req.add(new SkillRequirement(Skill.CRAFTING, 40, true));
		return req;
	}
}
