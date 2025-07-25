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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.runemysteries;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.ConditionalStep;
import net.runelite.client.plugins.microbot.questhelper.steps.NpcStep;
import net.runelite.client.plugins.microbot.questhelper.steps.ObjectStep;
import net.runelite.client.plugins.microbot.questhelper.steps.QuestStep;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

import static net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicHelper.and;

public class RuneMysteries extends BasicQuestHelper
{
	//Items Required
	ItemRequirement airTalisman, researchPackage, notes;

	//Items Recommended
	ItemRequirement varrockTeleport, wizardTeleport;

	Requirement inUpstairsLumbridge, inWizardBasement;

	QuestStep goUpToHoracio, talkToHoracio, goF1ToF0LumbridgeCastle, goDownToSedridor, talkToSedridor, finishTalkingToSedridor, talkToAubury, talkToAudburyAgain, goDownToSedridor2, talkToSedridor2;

	//Zones
	Zone wizardBasement, upstairsLumbridge;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		ConditionalStep goTalkToHoracio = new ConditionalStep(this, goUpToHoracio);
		goTalkToHoracio.addStep(inUpstairsLumbridge, talkToHoracio);

		steps.put(0, goTalkToHoracio);

		ConditionalStep goTalkToSedridor = new ConditionalStep(this, goDownToSedridor);
		goTalkToSedridor.addStep(and(airTalisman, inUpstairsLumbridge), goF1ToF0LumbridgeCastle);
		goTalkToSedridor.addStep(inWizardBasement, talkToSedridor);

		steps.put(1, goTalkToSedridor);

		steps.put(2, finishTalkingToSedridor);

		steps.put(3, talkToAubury);

		steps.put(4, talkToAudburyAgain);

		ConditionalStep goTalkToSedridor2 = new ConditionalStep(this, goDownToSedridor2);
		goTalkToSedridor2.addStep(inWizardBasement, talkToSedridor2);
		steps.put(5, goTalkToSedridor2);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		airTalisman = new ItemRequirement("Air talisman", ItemID.AIR_TALISMAN).isNotConsumed();
		airTalisman.setTooltip("You can get another from Duke Horacio if you lost it");
		researchPackage = new ItemRequirement("Research package", ItemID.RESEARCH_PACKAGE);
		researchPackage.setTooltip("You can get another from Sedridor if you lost it");
		notes = new ItemRequirement("Research notes", ItemID.RESEARCH_NOTES);
		notes.setTooltip("You can get another from Aubury if you lost them");
		varrockTeleport = new ItemRequirement("Varrock teleport", ItemID.POH_TABLET_VARROCKTELEPORT);
		wizardTeleport = new ItemRequirement("A teleport to the Wizard's Tower", ItemCollections.NECKLACE_OF_PASSAGES);
	}

	public void setupConditions()
	{
		inUpstairsLumbridge = new ZoneRequirement(upstairsLumbridge);
		inWizardBasement = new ZoneRequirement(wizardBasement);
	}

	@Override
	protected void setupZones()
	{
		upstairsLumbridge = new Zone(new WorldPoint(3203, 3206, 1), new WorldPoint(3218, 3231, 1));
		wizardBasement = new Zone(new WorldPoint(3094, 9553, 0), new WorldPoint(3125, 9582, 0));
	}

	public void setupSteps()
	{
		goUpToHoracio = new ObjectStep(this, ObjectID.SPIRALSTAIRS, new WorldPoint(3205, 3208, 0), "Talk to Duke Horacio on the first floor of Lumbridge castle.");
		talkToHoracio = new NpcStep(this, NpcID.DUKE_OF_LUMBRIDGE, new WorldPoint(3210, 3220, 1), "Talk to Duke Horacio on the first floor of Lumbridge castle.");
		talkToHoracio.addDialogStep("Have you any quests for me?");
		talkToHoracio.addDialogStep("Yes.");
		talkToHoracio.addSubSteps(goUpToHoracio);

		goF1ToF0LumbridgeCastle = new ObjectStep(this, ObjectID.SPIRALSTAIRSMIDDLE, new WorldPoint(3204, 3207, 1),
			"Bring the Air Talisman to Sedridor in the Wizard Tower's basement.");

		goDownToSedridor = new ObjectStep(this, ObjectID.WIZARDS_TOWER_LADDERTOP, new WorldPoint(3104, 3162, 0), "Bring the Air Talisman to Sedridor in the Wizard Tower's basement.", airTalisman);
		goDownToSedridor.addDialogStep("Have you any quests for me?");

		talkToSedridor = new NpcStep(this, NpcID.HEAD_WIZARD_1OP, new WorldPoint(3104, 9571, 0), "Bring the Air Talisman to Sedridor in the Wizard Tower's basement.", airTalisman);
		talkToSedridor.addDialogStep("I'm looking for the head wizard.");
		talkToSedridor.addDialogStep("Okay, here you are.");

		finishTalkingToSedridor = new NpcStep(this, NpcID.HEAD_WIZARD_1OP, new WorldPoint(3104, 9571, 0), "Accept taking the package for Sedridor.");
		finishTalkingToSedridor.addDialogStep("Yes, certainly.");

		talkToSedridor.addSubSteps(goDownToSedridor, finishTalkingToSedridor, goF1ToF0LumbridgeCastle);

		talkToAubury = new NpcStep(this, NpcID.AUBURY_2OP, new WorldPoint(3253, 3401, 0), "Bring the Research Package to Aubury in south east Varrock.", researchPackage);
		talkToAubury.addDialogStep("I've been sent here with a package for you.");
		talkToAudburyAgain = new NpcStep(this, NpcID.AUBURY_2OP, new WorldPoint(3253, 3401, 0), "Talk to Aubury again in south east Varrock.");

		goDownToSedridor2 = new ObjectStep(this, ObjectID.WIZARDS_TOWER_LADDERTOP, new WorldPoint(3104, 3162, 0), "Bring the research notes to Sedridor in the Wizard Tower's basement.", notes);
		talkToSedridor2 = new NpcStep(this, NpcID.HEAD_WIZARD_1OP, new WorldPoint(3104, 9571, 0), "Bring the notes to Sedridor in the Wizard Tower's basement.", notes);
		talkToSedridor2.addSubSteps(goDownToSedridor2);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(varrockTeleport);
		reqs.add(wizardTeleport);
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
		return Collections.singletonList(new ItemReward("Air Talisman", ItemID.AIR_TALISMAN, 1));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
				new UnlockReward("Ability to use the Runecrafting Skill."),
				new UnlockReward("Ability to mine Rune and Pure Essence."));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();

		allSteps.add(new PanelDetails("Discover Runecrafting", Arrays.asList(talkToHoracio, talkToSedridor, talkToAubury, talkToAudburyAgain, talkToSedridor2)));
		return allSteps;
	}
}
