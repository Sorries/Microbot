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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.aporcineofinterest;

import net.runelite.client.plugins.microbot.questhelper.bank.banktab.BankSlotIcons;
import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class APorcineOfInterest extends BasicQuestHelper
{
	//Items Required
	ItemRequirement rope, slashItem, reinforcedGoggles, combatGear, hoof;

	//Items Recommended
	ItemRequirement draynorTeleport, faladorFarmTeleport;

	Requirement inCave;

	DetailedQuestStep readNotice, talkToSarah, useRopeOnHole, enterHole, investigateSkeleton, talkToSpria, enterHoleAgain, killSourhog,
		enterHoleForFoot, cutOffFoot, returnToSarah, returnToSpria;

	//Zones
	Zone cave;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		steps.put(0, readNotice);
		steps.put(5, talkToSarah);
		steps.put(10, useRopeOnHole);

		ConditionalStep investigateCave = new ConditionalStep(this, enterHole);
		investigateCave.addStep(inCave, investigateSkeleton);

		steps.put(15, investigateCave);

		steps.put(20, talkToSpria);

		ConditionalStep goKillSourhog = new ConditionalStep(this, enterHoleAgain);
		goKillSourhog.addStep(inCave, killSourhog);

		steps.put(25, goKillSourhog);

		ConditionalStep getFootSteps = new ConditionalStep(this, enterHoleForFoot);
		getFootSteps.addStep(hoof, returnToSarah);
		getFootSteps.addStep(inCave, cutOffFoot);

		steps.put(30, getFootSteps);
		steps.put(35, returnToSpria);
		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		rope = new ItemRequirement("Rope", ItemID.ROPE);
		rope.setHighlightInInventory(true);

		slashItem = new ItemRequirement("A knife or slash weapon", ItemID.KNIFE).isNotConsumed();
		slashItem.setTooltip("Except abyssal whip, abyssal tentacle, or dragon claws.");

		reinforcedGoggles = new ItemRequirement("Reinforced goggles", ItemID.SLAYER_REINFORCED_GOGGLES, 1, true).isNotConsumed();
		reinforcedGoggles.setTooltip("You can get another pair from Spria");

		combatGear = new ItemRequirement("Combat gear", -1, -1).isNotConsumed();
		combatGear.setDisplayItemId(BankSlotIcons.getCombatGear());

		hoof = new ItemRequirement("Sourhog foot", ItemID.PORCINE_SOURHOG_TROPHY);
		hoof.setTooltip("You can get another from Sourhog's corpse in his cave");

		// Recommended
		draynorTeleport = new ItemRequirement("Teleport to north Draynor", ItemID.TELETAB_DRAYNOR);
		draynorTeleport.addAlternates(ItemCollections.AMULET_OF_GLORIES);
		faladorFarmTeleport = new ItemRequirement("Teleport to Falador Farm", ItemCollections.EXPLORERS_RINGS);
	}

	@Override
	protected void setupZones()
	{
		cave = new Zone(new WorldPoint(3152, 9669, 0), new WorldPoint(3181, 9720, 0));
	}

	public void setupConditions()
	{
		inCave = new ZoneRequirement(cave);
	}

	public void setupSteps()
	{
		readNotice = new ObjectStep(this, ObjectID.PORCINE_NOTICEBOARD, new WorldPoint(3086, 3251, 0), "Read the notice board in Draynor Village.");
		readNotice.addDialogStep("Yes.");

		talkToSarah = new NpcStep(this, NpcID.FARMING_SHOPKEEPER_1, new WorldPoint(3033, 3293, 0), "Talk to Sarah in the South Falador Farm.");
		talkToSarah.addDialogSteps("Talk about the bounty.");

		useRopeOnHole = new ObjectStep(this, ObjectID.PORCINE_HOLE, new WorldPoint(3151, 3348, 0), "Use a rope on the Strange Hole east of Draynor Manor.", rope);
		useRopeOnHole.addTeleport(draynorTeleport);
		useRopeOnHole.addIcon(ItemID.ROPE);
		useRopeOnHole.addDialogSteps("I think that'll be all for now.");

		enterHole = new ObjectStep(this, ObjectID.PORCINE_HOLE, new WorldPoint(3151, 3348, 0), "Climb down into the Strange Hole east of Draynor Manor.");
		investigateSkeleton = new ObjectStep(this, ObjectID.PORCINE_SKELETON, new WorldPoint(3164, 9676, 0), "Go to the end of the cave and investigate the skeleton there.");

		talkToSpria = new NpcStep(this, NpcID.PORCINE_SPRIA, new WorldPoint(3092, 3267, 0), "Talk to Spria in Draynor Village.");

		enterHoleAgain = new ObjectStep(this, ObjectID.PORCINE_HOLE, new WorldPoint(3151, 3348, 0), "Climb down into the Strange Hole east of Draynor Manor. Be prepared to fight Sourhog (level 37)", reinforcedGoggles, slashItem, combatGear);
		killSourhog = new NpcStep(this, NpcID.PORCINE_SOURHOG_SECOND, "Kill Sourhog.", reinforcedGoggles);
		killSourhog.addDialogStep("Yes");

		enterHoleForFoot = new ObjectStep(this, ObjectID.PORCINE_HOLE, new WorldPoint(3151, 3348, 0), "Climb down into the Strange Hole east of Draynor Manor.", slashItem);
		cutOffFoot = new ObjectStep(this, ObjectID.PORCINE_DEAD_SOURHOG, "Cut off Sourhog's foot.", slashItem);
		((ObjectStep) cutOffFoot).addAlternateObjects(ObjectID.PORCINE_DEAD_SOURHOG_9);
		cutOffFoot.addSubSteps(enterHoleForFoot);

		returnToSarah = new NpcStep(this, NpcID.FARMING_SHOPKEEPER_1, new WorldPoint(3033, 3293, 0), "Return to Sarah in the South Falador Farm.", hoof);
		returnToSarah.addTeleport(faladorFarmTeleport);
		returnToSarah.addDialogSteps("Talk about the bounty.");
		returnToSpria = new NpcStep(this, NpcID.PORCINE_SPRIA, new WorldPoint(3092, 3267, 0), "Return to Spria in Draynor Village.");
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(rope, slashItem, combatGear);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(draynorTeleport, faladorFarmTeleport);
	}

	@Override
	public List<String> getCombatRequirements()
	{
		return Collections.singletonList("Sourhog (level 37)");
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(1);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Collections.singletonList(new ExperienceReward(Skill.SLAYER, 1000));
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Collections.singletonList(new ItemReward("Coins", ItemID.COINS, 5000));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(new UnlockReward("30 Slayer Points"), new UnlockReward("Access to Sourhog Cave"), new UnlockReward("Sourhogs can be assigned as a slayer task by Spria"));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Starting off", Arrays.asList(readNotice, talkToSarah, useRopeOnHole,
			enterHole, investigateSkeleton, talkToSpria, enterHoleAgain, killSourhog, cutOffFoot, returnToSarah,
			returnToSpria), rope, slashItem, combatGear));
		return allSteps;
	}
}
