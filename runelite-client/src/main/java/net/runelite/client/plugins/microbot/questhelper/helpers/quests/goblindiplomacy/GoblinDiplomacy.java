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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.goblindiplomacy;

import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class GoblinDiplomacy extends BasicQuestHelper
{
	//Required items
	ItemRequirement goblinMailThree, orangeDye, blueDye, goblinMail, goblinMailTwo, blueArmour, orangeArmour, mailReq;

	Requirement isUpstairs, hasUpstairsArmour, hasWestArmour, hasNorthArmour;

	QuestStep talkToGeneral1, talkToGeneral2, talkToGeneral3, goUpLadder, searchUpLadder, goDownLadder, searchWestHut, searchBehindGenerals,
		dyeOrange, dyeBlue, getCrate2, getCrate3;

	//Zones
	Zone upstairs;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		ConditionalStep lootArmour = new ConditionalStep(this, goUpLadder);
		lootArmour.addStep(new Conditions(isUpstairs, hasUpstairsArmour), goDownLadder);
		lootArmour.addStep(new Conditions(hasUpstairsArmour, hasWestArmour), searchBehindGenerals);
		lootArmour.addStep(hasUpstairsArmour, searchWestHut);
		lootArmour.addStep(isUpstairs, searchUpLadder);

		ConditionalStep prepareForQuest = new ConditionalStep(this, lootArmour);
		prepareForQuest.addStep(new Conditions(goblinMail, blueArmour), dyeOrange);
		prepareForQuest.addStep(new Conditions(LogicType.OR, goblinMailThree, new Conditions(hasUpstairsArmour, hasWestArmour,
			hasNorthArmour)), dyeBlue);

		ConditionalStep step1 = new ConditionalStep(this, prepareForQuest);
		step1.addStep(new Conditions(goblinMail, blueArmour, orangeArmour), talkToGeneral1);

		steps.put(0, step1);
		steps.put(3, step1);

		ConditionalStep prepareBlueArmour = new ConditionalStep(this, lootArmour);
		prepareBlueArmour.addStep(new Conditions(LogicType.OR, goblinMailTwo, new Conditions(hasUpstairsArmour, hasWestArmour,
			hasNorthArmour)), dyeBlue);

		ConditionalStep step2 = new ConditionalStep(this, prepareBlueArmour);
		step2.addStep(blueArmour, talkToGeneral2);

		steps.put(4, step2);

		ConditionalStep step3 = new ConditionalStep(this, lootArmour);
		step3.addStep(new Conditions(LogicType.OR, goblinMail, new Conditions(hasUpstairsArmour, hasWestArmour,
			hasNorthArmour)), talkToGeneral3);

		steps.put(5, step3);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		blueDye = new ItemRequirement("Blue dye", ItemID.BLUEDYE);
		blueDye.setTooltip("You can have Aggie in Draynor Village make you some for 2 woad leaves (bought from Wyson in Falador Park for 20 coins) and 5 coins.");
		blueDye.setHighlightInInventory(true);
		orangeDye = new ItemRequirement("Orange dye", ItemID.ORANGEDYE);
		orangeDye.setTooltip("This is made from red dye and yellow dye. Bring Aggie in Draynor Village 3 redberries and 5 coins for red dye, then 2 onions and 5 coins for yellow dye.");
		orangeDye.setHighlightInInventory(true);
		goblinMailThree = new ItemRequirement("Goblin mail", ItemID.GOBLIN_ARMOUR, 3);
		mailReq = new ItemRequirement("Goblin mail", ItemID.GOBLIN_ARMOUR, 3);
		mailReq.canBeObtainedDuringQuest();
		goblinMailTwo = new ItemRequirement("Goblin mail", ItemID.GOBLIN_ARMOUR, 2);

		goblinMail = new ItemRequirement("Goblin mail", ItemID.GOBLIN_ARMOUR);
		goblinMail.canBeObtainedDuringQuest();
		goblinMail.setTooltip("You can get goblin mail by killing goblins around goblin village.");
		goblinMail.setHighlightInInventory(true);

		blueArmour = new ItemRequirement("Blue goblin mail", ItemID.GOBLIN_ARMOUR_DARKBLUE);
		orangeArmour = new ItemRequirement("Orange goblin mail", ItemID.GOBLIN_ARMOUR_ORANGE);
	}

	public void setupConditions()
	{
		isUpstairs = new ZoneRequirement(upstairs);
		hasUpstairsArmour = new VarbitRequirement(2381, 1);
		hasWestArmour = new VarbitRequirement(2380, 1);
		hasNorthArmour = new VarbitRequirement(2379, 1);
	}

	@Override
	protected void setupZones()
	{
		upstairs = new Zone(new WorldPoint(2952, 3495, 2), new WorldPoint(2959, 3498, 2));
	}

	public void setupSteps()
	{
		goUpLadder = new ObjectStep(this, ObjectID.GOBLIN_LADDER_BOTTOM, new WorldPoint(2954, 3497, 0), "You need three goblin mails, which you can find around the Goblin " +
			"Village. The first is up the ladder in a crate in the south of the village.");
		searchUpLadder = new ObjectStep(this, ObjectID.GOBLIN_OUTPOST_LARGE_CRATE_ARMOUR3, new WorldPoint(2955, 3498, 2), "Search the crate up the ladder.");
		goUpLadder.addSubSteps(searchUpLadder);
		goDownLadder = new ObjectStep(this, ObjectID.GOBLIN_LADDER_TOP, new WorldPoint(2954, 3497, 2), "Go back down the ladder.");
		searchWestHut = new ObjectStep(this, ObjectID.GOBLIN_OUTPOST_LARGE_CRATE_ARMOUR2, new WorldPoint(2951, 3508, 0), "Search the crate in the west of Goblin Village for Goblin Mail.");
		getCrate2 = new DetailedQuestStep(this, "The second goblin mail can be found in the west hut in a crate.");
		getCrate2.addSubSteps(goDownLadder, searchWestHut);

		searchBehindGenerals = new ObjectStep(this, ObjectID.GOBLIN_OUTPOST_LARGE_CRATE_ARMOUR1, new WorldPoint(2959, 3514, 0), "Search the crate north of the General's hut in Goblin Village.");
		getCrate3 = new DetailedQuestStep(this, "The last goblin mail is north of the generals' hut in a crate.");
		getCrate3.addSubSteps(searchBehindGenerals);

		dyeBlue = new DetailedQuestStep(this, "Use the blue dye on one of the goblin mail.", blueDye, goblinMail);
		dyeOrange = new DetailedQuestStep(this, "Use the orange dye on one of the goblin mail.", orangeDye, goblinMail);

		talkToGeneral1 = new NpcStep(this, NpcID.GENERAL_BENTNOZE_RED, new WorldPoint(2958, 3512, 0), "Talk to one of the Goblin Generals in Goblin Village.", orangeArmour);
		talkToGeneral1.addDialogStep("So how is life for the goblins?");
		talkToGeneral1.addDialogStep("Yes, Wartface looks fat");
		talkToGeneral1.addDialogStep("Do you want me to pick an armour colour for you?");
		talkToGeneral1.addDialogStep("What about a different colour?");
		talkToGeneral1.addDialogStep("I have some orange armour here.");

		talkToGeneral2 = new NpcStep(this, NpcID.GENERAL_BENTNOZE_RED, new WorldPoint(2958, 3512, 0), "Talk to one of the Goblin Generals in Goblin Village again.", blueArmour);
		talkToGeneral2.addDialogStep("So how is life for the goblins?");
		talkToGeneral2.addDialogStep("I have some blue armour here.");

		talkToGeneral3 = new NpcStep(this, NpcID.GENERAL_BENTNOZE_RED, new WorldPoint(2958, 3512, 0), "Talk to one of the Goblin Generals in Goblin Village once more.", goblinMail);
		talkToGeneral3.addDialogStep("So how is life for the goblins?");
		talkToGeneral3.addDialogStep("Yes, Wartface looks fat");
		talkToGeneral3.addDialogStep("I have some brown armour here.");
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(blueDye);
		reqs.add(orangeDye);
		reqs.add(mailReq);
		return reqs;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(5);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Collections.singletonList(new ExperienceReward(Skill.CRAFTING, 200));
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Collections.singletonList(new ItemReward("A Gold Bar", ItemID.GOLD_BAR, 1));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		PanelDetails getArmours = new PanelDetails("Prepare goblin mail",
			Arrays.asList(goUpLadder, getCrate2, getCrate3, dyeBlue, dyeOrange), blueDye, orangeDye, goblinMailThree);
		allSteps.add(getArmours);

		allSteps.add(new PanelDetails("Present the armours", Arrays.asList(talkToGeneral1, talkToGeneral2, talkToGeneral3)));
		return allSteps;
	}
}
