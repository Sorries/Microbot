/*
 * Copyright (c) 2023, Obasill <https://github.com/Obasill>
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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.secretsofthenorth;

import net.runelite.client.plugins.microbot.questhelper.bank.banktab.BankSlotIcons;
import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SkillRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.quest.QuestRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.Operation;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.widget.WidgetTextRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;

import java.util.*;

public class SecretsOfTheNorth extends BasicQuestHelper
{
	// Required
	ItemRequirement coins, lockpick, tinderbox, combatGear, food;

	// Recommended
	ItemRequirement antipoison, ardyCloak, icyBasalt, prayerPot;

	// Quest items
	ItemRequirement dustyScroll, leverHandle, icyChest, settlementsNote, jewelShard1, jewelShard2, ancientJewel, icyKey;

	QuestStep startQuest, goUpstairs, talkToGuardUpstairs, inspectBody, inspectWall, inspectWindow, climbHiddenLadder, inspectChest,
		goDownToGuard, talkToGuardInvestigated, talkToGuardCompleted, downStaircaseToKhaz, speakToBarman,
		inspectBarrel, inspectBoulder, inspectBush, inspectStump, inspectBoulder2, inspectBush2, fightEvelot,
		speakToEvelot, enterCave, boardRaft, returnRaft, returnStairs, returnGuard, ladderToClaus, talkToClaus,
		examineShelves, examineWall, lockpickChest, returnToHazeel, returnToHazeelWall, returnToHazeelLadder,
		returnToHazeelCave, returnToHazeelRaft, inspectScroll, goNorth, talkToSnowflake, talkToTroll, talkToTrollFinish,
		moveToWeissCave, enterWeissCave, fightAssassin, talkToKhazard, talkToHazeelWeiss, searchBarrel, openCentreGate, solveCenterGate,
		openNorthChest, solveChestPuzzle, getTinderbox,
		lightNW, lightSE, lightNE, lightSW, openWestChest, openNorthGate, useLeverOnMechanism, pullLever, inspectPillar,
		combineShards, openIcyChest, openSouthGate, enterCrevice, defeatMuspah,
		moveToWeissCaveEnd, enterCreviceEnd, enterWeissCaveEnd, talkToJhallan, continueCutscene;

	NpcStep talkToAlomoneOrClivet, talkToHazeel, finishQuest;

	Requirement notTalkedToGuard, notGoneUpstairs, notInspectCeril, notInspectWall, notInspectWindow, notInspectChest,
		inspectedCrimeScene, toldWindow, toldCeril, toldWall, checkedBarrel, checkedBoulder, checkedBush, checkedStump,
		checkedBoulder2, checkedBush2, evelotDefeated, talkedToAlomoneOrClivet, talkedToHazeel, talkedToGuard, talkedToClaus, buttonPressed,
		chestPicked, scrollInspected, handedInScroll, hadDustyScroll, talkedNorth, talkedToSnowflake, questioned1, questioned2, assassinFight,
		assassinDefeated, talkedToKhazard, puzzleStart, centreGateUnlocked, nwBrazier, seBrazier, neBrazier, swBrazier, brazierFin,
		northGateUnlocked, fixedLever, leverPulled, southGateOpened, jhallanTalkedTo, returnToGuard, gottenIcyKey;

	Requirement notInspectedCrimeScene, onTheTrail, inGateInterface;

	Zone mansionFirst, mansionSecond, hidingSpace, raftZone, hazeelZone, basement, hiddenRoom, weissCave, mahjarratCave, muspahRoom;

	Requirement inMansionFirst, inMansionSecond, inHidingSpace, inRaftZone, inHazeelZone, inBasement, inHiddenRoom,
		inWeissCave, inMahjarratCave, inMuspahRoom;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		/*
		14739 - idk 0->1 on quest pickup
		14740 - idk 0-> 1 on quest pickup
		14769 - idk  1 -> 2 on quest pickup
		14722 - part 1 house?

		 */

		ConditionalStep startingOff = new ConditionalStep(this, startQuest);
		startingOff.addStep(new Conditions(inMansionFirst, notInspectedCrimeScene, inspectedCrimeScene,
			toldCeril, toldWall, toldWindow), talkToGuardCompleted);
		startingOff.addStep(new Conditions(inMansionFirst, notInspectedCrimeScene, inspectedCrimeScene), talkToGuardInvestigated);
		startingOff.addStep(new Conditions(inMansionSecond, notInspectedCrimeScene, inspectedCrimeScene), goDownToGuard);
		startingOff.addStep(new Conditions(inMansionSecond, notInspectedCrimeScene, notInspectChest), inspectChest);
		startingOff.addStep(new Conditions(inMansionFirst, notInspectedCrimeScene, notInspectCeril), inspectBody);
		startingOff.addStep(new Conditions(inMansionFirst, notInspectedCrimeScene, notInspectWindow), inspectWindow);
		startingOff.addStep(new Conditions(inHidingSpace,  notInspectedCrimeScene, notInspectChest), climbHiddenLadder);
		startingOff.addStep(new Conditions(inMansionFirst, notInspectedCrimeScene, notInspectChest), inspectWall);
		startingOff.addStep(new Conditions(notGoneUpstairs, inMansionFirst), talkToGuardUpstairs);
		startingOff.addStep(notGoneUpstairs, goUpstairs);
		startingOff.addStep(notTalkedToGuard, startQuest);

		steps.put(0, startingOff);
		steps.put(2, startingOff);
		steps.put(4, startingOff);
		steps.put(6, startingOff);
		steps.put(8, startingOff);
		steps.put(10, startingOff);
		steps.put(12, startingOff);

		ConditionalStep huntingEvelot = new ConditionalStep(this, speakToBarman);
		huntingEvelot.addStep(new Conditions(onTheTrail, evelotDefeated), speakToEvelot);
		huntingEvelot.addStep(new Conditions(onTheTrail, checkedBush2), fightEvelot);
		huntingEvelot.addStep(new Conditions(onTheTrail, checkedBoulder2), inspectBush2);
		huntingEvelot.addStep(new Conditions(onTheTrail, checkedStump), inspectBoulder2);
		huntingEvelot.addStep(new Conditions(onTheTrail, checkedBush), inspectStump);
		huntingEvelot.addStep(new Conditions(onTheTrail, checkedBoulder), inspectBush);
		huntingEvelot.addStep(new Conditions(onTheTrail, checkedBarrel), inspectBoulder);
		huntingEvelot.addStep(onTheTrail, inspectBarrel);
		huntingEvelot.addStep(inMansionFirst, downStaircaseToKhaz);

		steps.put(14, huntingEvelot);
		steps.put(16, huntingEvelot);
		steps.put(18, huntingEvelot);
		steps.put(20, huntingEvelot);
		steps.put(22, huntingEvelot);
		steps.put(24, huntingEvelot);
		steps.put(26, huntingEvelot);
		steps.put(28, huntingEvelot);


		ConditionalStep questionsForHazeel = new ConditionalStep(this, enterCave);
		questionsForHazeel.addStep(new Conditions(scrollInspected, inHazeelZone, hadDustyScroll), returnToHazeel);
		questionsForHazeel.addStep(new Conditions(scrollInspected, inRaftZone, dustyScroll), returnToHazeelRaft);
		questionsForHazeel.addStep(new Conditions(scrollInspected, inBasement, dustyScroll), returnToHazeelLadder);
		questionsForHazeel.addStep(new Conditions(scrollInspected, inHiddenRoom, dustyScroll), returnToHazeelWall);
		questionsForHazeel.addStep(new Conditions(scrollInspected, dustyScroll), returnToHazeelCave);
		questionsForHazeel.addStep(new Conditions(buttonPressed, inHiddenRoom, dustyScroll), inspectScroll);
		questionsForHazeel.addStep(new Conditions(buttonPressed, inHiddenRoom), lockpickChest);
		questionsForHazeel.addStep(new Conditions(buttonPressed, inBasement), examineWall);
		questionsForHazeel.addStep(new Conditions(talkedToClaus, inBasement), examineShelves);
		questionsForHazeel.addStep(new Conditions(talkedToGuard, inBasement), talkToClaus);
		questionsForHazeel.addStep(talkedToGuard, ladderToClaus);
		questionsForHazeel.addStep(new Conditions(inRaftZone, talkedToHazeel), returnStairs);
		questionsForHazeel.addStep(new Conditions(inHazeelZone, talkedToHazeel), returnRaft);
		questionsForHazeel.addStep(talkedToHazeel, returnGuard);
		questionsForHazeel.addStep(new Conditions(talkedToAlomoneOrClivet, inHazeelZone), talkToHazeel);
		questionsForHazeel.addStep(inHazeelZone, talkToAlomoneOrClivet);
		questionsForHazeel.addStep(inRaftZone, boardRaft);

		steps.put(30, questionsForHazeel);
		steps.put(32, questionsForHazeel);
		steps.put(34, questionsForHazeel);
		steps.put(36, questionsForHazeel);
		steps.put(38, questionsForHazeel);
		steps.put(40, questionsForHazeel);
		steps.put(42, questionsForHazeel);
		steps.put(44, questionsForHazeel);
		steps.put(46, questionsForHazeel);
		steps.put(48, questionsForHazeel);
		steps.put(50, questionsForHazeel);
		steps.put(52, questionsForHazeel);


		ConditionalStep prisonPuzzleSteps = new ConditionalStep(this, searchBarrel);
		prisonPuzzleSteps.addStep(new Conditions(gottenIcyKey), openSouthGate);
		prisonPuzzleSteps.addStep(new Conditions(icyChest, ancientJewel), openIcyChest);
		prisonPuzzleSteps.addStep(new Conditions(icyChest, jewelShard1, jewelShard2), combineShards);
		prisonPuzzleSteps.addStep(new Conditions(icyChest, jewelShard1, northGateUnlocked, leverPulled), inspectPillar);
		prisonPuzzleSteps.addStep(new Conditions(icyChest, jewelShard1, northGateUnlocked, fixedLever), pullLever);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle, icyChest, settlementsNote, jewelShard1, northGateUnlocked), useLeverOnMechanism);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle, icyChest, settlementsNote, jewelShard1), openNorthGate);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle, icyChest, brazierFin), openWestChest);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle, icyChest, tinderbox, nwBrazier, seBrazier, neBrazier), lightSW);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle, icyChest, tinderbox, nwBrazier, seBrazier), lightNE);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle, icyChest, tinderbox, nwBrazier), lightSE);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle, icyChest, tinderbox), lightNW);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle, icyChest), getTinderbox);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle, centreGateUnlocked, inGateInterface), solveChestPuzzle);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle, centreGateUnlocked), openNorthChest);
		prisonPuzzleSteps.addStep(new Conditions(new Conditions(inGateInterface, leverHandle)), solveCenterGate);
		prisonPuzzleSteps.addStep(new Conditions(leverHandle), openCentreGate);

		ConditionalStep thePrisonSteps = new ConditionalStep(this, fightAssassin);
		thePrisonSteps.addStep(southGateOpened, enterCrevice);
		thePrisonSteps.addStep(puzzleStart, prisonPuzzleSteps);
		thePrisonSteps.addStep(talkedToKhazard, talkToHazeelWeiss);
		thePrisonSteps.addStep(assassinDefeated, talkToKhazard);

		ConditionalStep goingNorth = new ConditionalStep(this, goNorth);
		goingNorth.addStep(inMuspahRoom, defeatMuspah);
		goingNorth.addStep(inMahjarratCave, thePrisonSteps);
		goingNorth.addStep(new Conditions(assassinFight, inWeissCave), enterWeissCave);
		goingNorth.addStep(assassinFight, moveToWeissCave);
		goingNorth.addStep(new Conditions(talkedToSnowflake, questioned1, questioned2), talkToTrollFinish);
		goingNorth.addStep(talkedToSnowflake, talkToTroll);
		goingNorth.addStep(talkedNorth, talkToSnowflake);

		steps.put(54, goingNorth);
		steps.put(56, goingNorth);
		steps.put(58, goingNorth);
		steps.put(60, goingNorth);
		steps.put(62, goingNorth);
		steps.put(64, goingNorth);
		steps.put(66, goingNorth);
		steps.put(68, goingNorth);
		steps.put(70, goingNorth);
		steps.put(72, goingNorth);
		steps.put(74, goingNorth);
		steps.put(76, goingNorth);
		steps.put(78, goingNorth);
		steps.put(80, goingNorth);

		ConditionalStep finishingUp = new ConditionalStep(this, moveToWeissCaveEnd);
		finishingUp.addStep(returnToGuard, finishQuest);
		finishingUp.addStep(new Conditions(inMahjarratCave, jhallanTalkedTo), continueCutscene);
		finishingUp.addStep(inMuspahRoom, talkToJhallan);
		finishingUp.addStep(inMahjarratCave, enterCreviceEnd);
		finishingUp.addStep(inWeissCave, enterWeissCaveEnd);

		steps.put(82, finishingUp);
		steps.put(84, finishingUp);
		steps.put(86, finishingUp); // talk to hazeel
		steps.put(88, finishingUp);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		// Required
		coins = new ItemRequirement("Coins", ItemCollections.COINS, 100);
		lockpick = new ItemRequirement("Lockpick", ItemID.LOCKPICK).isNotConsumed();
		lockpick.addAlternates(ItemID.KR_HAIRCLIP);
		tinderbox = new ItemRequirement("Tinderbox", ItemID.TINDERBOX).isNotConsumed();
		tinderbox.canBeObtainedDuringQuest();
		combatGear = new ItemRequirement("Combat Gear", -1, -1).isNotConsumed();
		combatGear.setDisplayItemId(BankSlotIcons.getCombatGear());
		food = new ItemRequirement("Food", ItemCollections.GOOD_EATING_FOOD, -1);
		prayerPot = new ItemRequirement("Prayer potions", ItemCollections.PRAYER_POTIONS);

		dustyScroll = new ItemRequirement("Dusty Scroll", ItemID.SOTN_CREST_SKETCH);
		leverHandle = new ItemRequirement("Lever Handle", ItemID.SOTN_GHORROCK_LEVER);
		icyChest = new ItemRequirement("Icy chest", ItemID.SOTN_GHORROCK_ICY_CHEST);
		settlementsNote = new ItemRequirement("Settlements note", ItemID.SOTN_GHORROCK_SETTLEMENTS_NOTE);
		jewelShard1 = new ItemRequirement("Jewel shard", ItemID.SOTN_GHORROCK_SHARD_2);
		jewelShard2 = new ItemRequirement("Jewel shard", ItemID.SOTN_GHORROCK_SHARD_1);
		ancientJewel = new ItemRequirement("Ancient jewel", ItemID.SOTN_GHORROCK_JEWEL);
		icyKey = new ItemRequirement("Icy key", ItemID.SOTN_GHORROCK_KEY);
		icyKey.setTooltip("You can get another from the chest in the north of the central room of the puzzle area");

		// Recommended
		antipoison = new ItemRequirement("Antipoisons", ItemCollections.ANTIPOISONS);
		ardyCloak = new ItemRequirement("Any Ardougne Cloak", ItemCollections.ARDY_CLOAKS).isNotConsumed();
		icyBasalt = new ItemRequirement("Icy Basalt", ItemID.WEISS_TELEPORT_BASALT);

		notTalkedToGuard = new VarbitRequirement(VarbitID.SOTN, 2, Operation.GREATER_EQUAL);
		notGoneUpstairs = new VarbitRequirement(VarbitID.SOTN, 4, Operation.GREATER_EQUAL);
		notInspectedCrimeScene = new VarbitRequirement(VarbitID.SOTN, 6, Operation.GREATER_EQUAL);

		notInspectWall = new VarbitRequirement(14726, 0);
		notInspectCeril = new VarbitRequirement(14727, 0);
		notInspectWindow = new VarbitRequirement(14728, 0);
		notInspectChest = new VarbitRequirement(14729, 0);

		inspectedCrimeScene = new VarbitRequirement(VarbitID.SOTN, 8, Operation.GREATER_EQUAL);

		toldWindow = new VarbitRequirement(14731, 1);
		toldCeril = new VarbitRequirement(14730, 1);
		toldWall = new VarbitRequirement(14732, 1);

		onTheTrail = new VarbitRequirement(VarbitID.SOTN, 20, Operation.GREATER_EQUAL);

		checkedBarrel = new VarbitRequirement(14733, 1);
		checkedBoulder = new VarbitRequirement(14734, 1);
		checkedBush = new VarbitRequirement(14735, 1);
		checkedStump = new VarbitRequirement(14736, 1);
		checkedBoulder2 = new VarbitRequirement(14737, 1);
		checkedBush2 = new VarbitRequirement(14738, 1);

		evelotDefeated = new VarbitRequirement(VarbitID.SOTN, 28, Operation.GREATER_EQUAL);
		talkedToAlomoneOrClivet = new VarbitRequirement(VarbitID.SOTN, 32, Operation.GREATER_EQUAL);
		talkedToHazeel = new VarbitRequirement(VarbitID.SOTN, 36, Operation.GREATER_EQUAL);

		talkedToGuard = new VarbitRequirement(VarbitID.SOTN, 38, Operation.GREATER_EQUAL);

		talkedToClaus = new VarbitRequirement(VarbitID.SOTN, 40, Operation.GREATER_EQUAL);

		buttonPressed = new VarbitRequirement(VarbitID.SOTN, 42, Operation.GREATER_EQUAL);

		// Puzzle values
		// 811.0, 811.1 big wrapper
		// 'Confirm', 811.15
		// 811.9[0-98] for positions. Going from A (bottom left) to F (top right)
		// 2 elements per tile, first value contains the number if there is one in Text
		// 811.14[0-63] for guesses. Goes from First guess A-F, and loops to top
		// Incorrect guesses are 2 rows, green/blue are 3, non-guessed are 1

		chestPicked = new VarbitRequirement(VarbitID.SOTN, 48, Operation.GREATER_EQUAL);

		scrollInspected = new VarbitRequirement(VarbitID.SOTN, 50, Operation.GREATER_EQUAL);

		handedInScroll = new VarbitRequirement(VarbitID.SOTN, 52, Operation.GREATER_EQUAL);
		hadDustyScroll = new Conditions(LogicType.OR, dustyScroll, handedInScroll);

		talkedNorth = new VarbitRequirement(VarbitID.SOTN, 56, Operation.GREATER_EQUAL);

		talkedToSnowflake = new VarbitRequirement(VarbitID.SOTN, 58, Operation.GREATER_EQUAL);

		questioned1 = new VarbitRequirement(VarbitID.SOTN_GHORROCK_QUESTION, 1, Operation.GREATER_EQUAL);
		questioned2 = new VarbitRequirement(VarbitID.SOTN_RITUAL_QUESTION, 1, Operation.GREATER_EQUAL);

		assassinFight = new VarbitRequirement(VarbitID.SOTN, 62, Operation.GREATER_EQUAL);

		assassinDefeated = new VarbitRequirement(VarbitID.SOTN, 68, Operation.GREATER_EQUAL);
		talkedToKhazard = new VarbitRequirement(VarbitID.SOTN, 70, Operation.GREATER_EQUAL);

		puzzleStart = new VarbitRequirement(VarbitID.SOTN, 72, Operation.GREATER_EQUAL);
		inGateInterface = new WidgetTextRequirement(809, 5, 9, "Confirm");
		centreGateUnlocked = new VarbitRequirement(VarbitID.SOTN_GHORROCK_GATE_2, 1, Operation.GREATER_EQUAL);

		nwBrazier = new VarbitRequirement(VarbitID.SOTN_GHORROCK_BRAZIER_1, 4, Operation.EQUAL);
		seBrazier = new VarbitRequirement(VarbitID.SOTN_GHORROCK_BRAZIER_2, 1, Operation.EQUAL);
		neBrazier = new VarbitRequirement(VarbitID.SOTN_GHORROCK_BRAZIER_3, 3, Operation.EQUAL);
		swBrazier = new VarbitRequirement(VarbitID.SOTN_GHORROCK_BRAZIER_4, 2, Operation.EQUAL);
		brazierFin = new VarbitRequirement(VarbitID.SOTN_GHORROCK_BRAZIER_CHEST, 1, Operation.GREATER_EQUAL);

		northGateUnlocked = new VarbitRequirement(VarbitID.SOTN_GHORROCK_GATE_1, 1, Operation.GREATER_EQUAL);

		fixedLever = new VarbitRequirement(VarbitID.SOTN_GHORROCK_LEVER, 1, Operation.GREATER_EQUAL);
		leverPulled = new VarbitRequirement(VarbitID.SOTN_GHORROCK_LEVER, 2, Operation.GREATER_EQUAL);

		gottenIcyKey = new VarbitRequirement(VarbitID.SOTN, 76, Operation.GREATER_EQUAL);

		southGateOpened = new VarbitRequirement(VarbitID.SOTN, 78, Operation.GREATER_EQUAL);

		jhallanTalkedTo = new VarbitRequirement(VarbitID.SOTN, 86, Operation.GREATER_EQUAL);
		returnToGuard = new VarbitRequirement(VarbitID.SOTN, 88, Operation.GREATER_EQUAL);

		// 14745 0->1 when teleport used from Hazeel
	}

	@Override
	protected void setupZones()
	{
		mansionFirst = new Zone(new WorldPoint(2562, 3276, 1), new WorldPoint(2577, 3266, 1));
		mansionSecond = new Zone(new WorldPoint(2562, 3276, 2), new WorldPoint(2577, 3266, 2));
		hidingSpace = new Zone(new WorldPoint(2571, 3271, 1), new WorldPoint(2753, 3271, 1));
		raftZone = new Zone(new WorldPoint(2564, 9686, 0), new WorldPoint(2572, 9680, 0));
		hazeelZone = new Zone(new WorldPoint(2598, 9696,  0), new WorldPoint(2616, 9664, 0));
		basement = new Zone(new WorldPoint(2536, 9701,  0), new WorldPoint(2550, 9692, 0));
		hiddenRoom = new Zone(new WorldPoint(2531, 9623,  0), new WorldPoint(2539, 9616, 0));
		weissCave = new Zone(new WorldPoint(2825, 10355,  0), new WorldPoint(2860, 10323, 0));
		mahjarratCave = new Zone(new WorldPoint(2890, 10290, 0), new WorldPoint(2936, 10360, 0));
		muspahRoom = new Zone(new WorldPoint(2820, 4230, 0), new WorldPoint(2870, 4290, 0));
	}

	public void setupConditions()
	{
		inMansionFirst = new ZoneRequirement(mansionFirst);
		inMansionSecond = new ZoneRequirement(mansionSecond);
		inHidingSpace = new ZoneRequirement(hidingSpace);
		inRaftZone = new ZoneRequirement(raftZone);
		inHazeelZone = new ZoneRequirement(hazeelZone);
		inBasement = new ZoneRequirement(basement);
		inHiddenRoom = new ZoneRequirement(hiddenRoom);
		inWeissCave = new ZoneRequirement(weissCave);
		inMahjarratCave = new ZoneRequirement(mahjarratCave);
		inMuspahRoom = new ZoneRequirement(muspahRoom);
	}

	public void setupSteps()
	{
		startQuest = new NpcStep(this, NpcID.GUARD_CARNILLEAN_VIS, new WorldPoint(2570, 3276, 0),
			"Talk to the guard outside of the Carnillean Mansion in East Ardougne.");
		startQuest.addDialogStep("Yes.");
		goUpstairs = new ObjectStep(this, ObjectID.CARNILLEAN_STAIRS, new WorldPoint(2568, 3269, 0),
			"Climb the stairs in Carnillean Mansion and talk to the guard there.");
		talkToGuardUpstairs = new NpcStep(this, NpcID.SOTN_GUARD_CARNILLEAN_UPSTAIRS, new WorldPoint(2571, 3267, 1), "Talk to the guard.");
		goUpstairs.addSubSteps(talkToGuardUpstairs);
		inspectBody = new ObjectStep(this, ObjectID.SOTN_DEAD_BODY, "Inspect Ceril Carnillean's body.");
		inspectWall = new ObjectStep(this, ObjectID.CARNILLEANBOOKCASE_KNOCK, "Knock-at the wall on the North side of the room.");
		inspectWall.addDialogStep("Yes.");
		inspectWindow = new ObjectStep(this, ObjectID.SOTN_WINDOW_INNER_OP, "Inspect the broken Window on the East side of the room.");
		climbHiddenLadder = new ObjectStep(this, ObjectID.LADDER, "Climb the Ladder.");
		inspectChest = new ObjectStep(this, ObjectID.SOTN_BROKEN_CHEST, "Inspect the chest.");
		goDownToGuard = new ObjectStep(this, ObjectID.LADDERTOP, "Descend the ladder and talk to the guard " +
			"about what you've found.");
		talkToGuardInvestigated = new TellAboutMurder(this);
		talkToGuardCompleted = new NpcStep(this, NpcID.SOTN_GUARD_CARNILLEAN_UPSTAIRS, "Talk to the guard.");
		talkToGuardCompleted.addDialogSteps("I think that's everything.", "I think I've gone over everything.");
		goDownToGuard.addSubSteps(talkToGuardInvestigated, talkToGuardCompleted);

		downStaircaseToKhaz = new ObjectStep(this, ObjectID.CARNILLEAN_STAIRSTOP, "Climb down the staircase.");
		speakToBarman = new NpcStep(this, NpcID.KHAZARD_BARMAN, new WorldPoint(2566, 3140, 0),
			"Head to the Fight Arena Bar and talk to the Khazard Barman.", coins, combatGear);
		speakToBarman.addDialogStep("Do you know anyone called Evelot?");
		inspectBarrel = new ObjectStep(this, 46873, new WorldPoint(2568, 3152, 0),
			"Inspect the barrels outside the Fight Arena Bar entrance.",  combatGear);
		inspectBoulder = new ObjectStep(this, 46874, new WorldPoint(2573, 3180, 0),
			"Inspect the boulder to the north.", combatGear);
		inspectBush = new ObjectStep(this, 46878, new WorldPoint(2567, 3202, 0),
			"Continue north and inspect the bush there.", combatGear);
		inspectStump = new ObjectStep(this, 46877, new WorldPoint(2584, 3197, 0),
			"Inspect the stump to the east.", combatGear);
		inspectBoulder2 = new ObjectStep(this, 46875, new WorldPoint(2605, 3188, 0),
			"Inspect the boulder to the south east.", combatGear);
		inspectBush2 = new ObjectStep(this, 46878, new WorldPoint(2621, 3193, 0),
			"Inspect the bush to the east.", combatGear);
		fightEvelot = new NpcStep(this, NpcID.SOTN_EVELOT_VIS, new WorldPoint(2643, 3202, 0),
			"Approach Evelot to the east to start the fight.\n\n" +
				"Protect from melee and reactivate prayer after she special attacks.",
			combatGear);
		speakToEvelot = new NpcStep(this, NpcID.SOTN_EVELOT_VIS, new WorldPoint(2643, 3202, 0),
			"Talk to Evelot again after the fight.");

		enterCave = new ObjectStep(this, ObjectID.HAZEELCULTCAVE, new WorldPoint(2586, 3234, 0),
			"Enter the cave that leads to the Ardougne sewers, near the Clock Tower.", lockpick);
		boardRaft = new ObjectStep(this, ObjectID.HAZEELSEWERRAFT, new WorldPoint(2567, 9679, 0),
			"Board the raft.");
		// TODO check if player killed Alomone and direct accordingly
		talkToAlomoneOrClivet = new NpcStep(this, NpcID.ALOMONE_HAZEEL_CULTIST_1OP, new WorldPoint(2607, 9671, 0), "Talk to Alomone or Clivet inside the cult area.");
		talkToAlomoneOrClivet.addAlternateNpcs(NpcID.ALOMONE_HAZEEL_CULTIST_2OP, NpcID.ALOMONE_HAZEEL_CULTIST_CUTSCENE, NpcID.SOTN_CLIVET_VIS, NpcID.CLIVET_HAZEEL_CULTIST_VIS);
		talkToHazeel = new NpcStep(this, NpcID.SOTN_HAZEEL_VIS, new WorldPoint(2607, 9672, 0),
			"Talk to Hazeel.");
		returnRaft = new ObjectStep(this, ObjectID.HAZEELSEWERRAFT, new WorldPoint(2606, 9693, 0),
			"Return to the Carnillean Mansion.");
		returnStairs = new ObjectStep(this, ObjectID.HAZEELCULTSTAIRS, new WorldPoint(2570, 9684, 0),
			"Return to the Carnillean Mansion.");
		returnGuard = new NpcStep(this, NpcID.GUARD_CARNILLEAN_VIS, new WorldPoint(2570, 3276, 0),
			"Return to the Carnillean Mansion and speak to the Guard.");
		returnGuard.addSubSteps(returnRaft, returnStairs);
		ladderToClaus = new ObjectStep(this, ObjectID.CARNILLEAN_LADDER_DOWN, new WorldPoint(2570, 3267, 0),
			"Climb down the ladder into the basement of the mansion.");
		talkToClaus = new NpcStep(this, NpcID.CLAUS_CARNILLEAN, new WorldPoint(2542, 9696, 0),
			"Talk to Claus the Chef.");
		examineShelves = new ObjectStep(this, ObjectID.SOTN_SHELVES_BUTTON, new WorldPoint(2540, 9694, 0),
			"Search the cooking shelves for a button.");
		examineShelves.addDialogStep("Yes.");
		examineWall = new ObjectStep(this, 46897, new WorldPoint(2544, 9698, 0),
			"Inspect the wall next to the noticeboard.");
		examineWall.addDialogStep("Enter the passage.");
		lockpickChest = new ObjectStep(this, 46899, new WorldPoint(2535, 9621, 0),
			"Picklock the chest.", lockpick);
		lockpickChest.addText("Green means it's the right number and position.");
		lockpickChest.addText("Blue means it's the right position but wrong number.");
		lockpickChest.addText("The puzzle's solution does not change attempt to attempt.");
		inspectScroll = new ItemStep(this, "Inspect the dusty scroll.", dustyScroll.highlighted());
		returnToHazeel = new NpcStep(this, NpcID.SOTN_HAZEEL_VIS, new WorldPoint(2607, 9672, 0),
		"Return to Hazeel and tell him about the scroll.", dustyScroll.hideConditioned(handedInScroll));
		returnToHazeelWall = new ObjectStep(this, ObjectID.SOTN_HIDDEN_DOOR_OP, new WorldPoint(2533, 9617, 0),
			"Return to Hazeel and tell him about the scroll.", dustyScroll.hideConditioned(handedInScroll));
		returnToHazeelWall.addDialogStep("Enter the passage.");
		returnToHazeelLadder = new ObjectStep(this, ObjectID.CARNILLEAN_LADDER_UP, new WorldPoint(2544, 9694, 0),
			"Return to Hazeel and tell him about the scroll.", dustyScroll.hideConditioned(handedInScroll));
		returnToHazeelCave = new ObjectStep(this, ObjectID.HAZEELCULTCAVE, new WorldPoint(2586, 3234, 0),
			"Return to Hazeel and tell him about the scroll.", dustyScroll.hideConditioned(handedInScroll));
		returnToHazeelRaft = new ObjectStep(this, ObjectID.HAZEELSEWERRAFT, new WorldPoint(2567, 9679, 0),
			"Return to Hazeel and tell him about the scroll.", dustyScroll.hideConditioned(handedInScroll));
		returnToHazeel.addSubSteps(returnToHazeelWall, returnToHazeelLadder, returnToHazeelCave, returnToHazeelRaft);

		goNorth = new NpcStep(this, NpcID.SOTN_HAZEEL_TROLL_VIS, new WorldPoint(2880, 3946, 0),
			"Talk to Big Fish at the north entrance to Weiss.");
		talkToTroll = new AskAboutRitual(this);
		talkToTrollFinish = new NpcStep(this, NpcID.SOTN_HAZEEL_TROLL_VIS, new WorldPoint(2880, 3946, 0),
			"Talk to Big Fish (Hazeel) at the north entrance to Weiss again.");
		talkToTrollFinish.addDialogSteps("Okay, I'm done asking questions. What now?", "I'm done asking questions. What now?");
		talkToTroll.addSubSteps(talkToTrollFinish);
		talkToSnowflake = new NpcStep(this, NpcID.MY2ARM_SNOWFLAKE, new WorldPoint(2872, 3934, 0),
			"Talk to Snowflake.");
		talkToSnowflake.addDialogStep("Have you seen anything odd around here recently?");
		moveToWeissCave = new ObjectStep(this, ObjectID.MY2ARM_THRONEROOM_STAIRSDOWN, new WorldPoint(2867, 3940, 0),
			"Prepare for a fight and climb down the stairs in the middle of Weiss.", combatGear, antipoison);
		enterWeissCave = new ObjectStep(this, 46905, new WorldPoint(2846, 10332, 0),
			"Enter the cave to the south.", combatGear, antipoison);
		enterWeissCave.addDialogStep("Yes.");
		fightAssassin = new NpcStep(this, NpcID.AKD_SETTLEMENT_RUINS_ASSASSIN, new WorldPoint(2927, 10348, 0), "Defeat the assassin.");
		fightAssassin.addText("Put the Assassin in the smoke bombs so you can hit him and dodge the poison vials he throws out.");
		talkToKhazard = new NpcStep(this, NpcID.SOTN_KHAZARD_GHORROCK, new WorldPoint(2927, 10348, 0),
			"Talk to General Khazard.");
		talkToHazeelWeiss = new NpcStep(this, NpcID.SOTN_HAZEEL_GHORROCK, new WorldPoint(2903, 10335, 0), "Talk to Hazeel to the west and tell him about what happened.");
		searchBarrel = new ObjectStep(this, ObjectID.SOTN_GHORROCK_WEAPON_BARREL, new WorldPoint(2923, 10322, 0),
			"Search the barrel south of the room you fought the assassin.");
		openCentreGate = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.SOTN_GHORROCK_GATE_2, new WorldPoint(2924, 10329, 0),
			"Enter the centre room. Use the code \"BLOOD\" to unlock the gate."), new ObjectStep(this, ObjectID.SOTN_GHORROCK_GATE_2, new WorldPoint(2924, 10329, 0),
			"Unlock the gate to the centre room by working out the correct code."));
		solveCenterGate =  new PuzzleWrapperStep(this, new SolveDoorCode(this), "Work out and enter the code to the central room's gate.");
		openCentreGate.addSubSteps(solveCenterGate);
		openNorthChest =  new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.SOTN_GHORROCK_CHEST_4,
			new WorldPoint(2919, 10331, 0), "Open the north chest using the code \"7402\"."),  new ObjectStep(this, ObjectID.SOTN_GHORROCK_CHEST_4,
			new WorldPoint(2919, 10331, 0), "Open the north chest after working out the code for it."));
		solveChestPuzzle = new PuzzleWrapperStep(this, new SolveChestCode(this), "Open the north chest after working out its code.");
		openNorthChest.addSubSteps(solveChestPuzzle);
		getTinderbox = new ObjectStep(this, ObjectID.SOTN_GHORROCK_CRATE_2, new WorldPoint(2916, 10329, 0),
			"Get a tinderbox from the crate in the west of the central room.");
		lightNW = new ObjectStep(this, ObjectID.SOTN_GHORROCK_BRAZIER_UNLIT, new WorldPoint(2916, 10331, 0), "Light the North West Brazier.");
		lightSE = new ObjectStep(this, ObjectID.SOTN_GHORROCK_BRAZIER_UNLIT, new WorldPoint(2922, 10325, 0), "Light the South East Brazier.");
		lightNE = new ObjectStep(this, ObjectID.SOTN_GHORROCK_BRAZIER_UNLIT, new WorldPoint(2922, 10331, 0), "Light the North East Brazier.");
		lightSW = new ObjectStep(this, ObjectID.SOTN_GHORROCK_BRAZIER_UNLIT, new WorldPoint(2916, 10325, 0), "Light the South West Brazier.");
		openWestChest = new ObjectStep(this, ObjectID.SOTN_GHORROCK_CHEST_3, new WorldPoint(2916, 10327, 0), "Open the western chest.");
		openNorthGate = new PuzzleWrapperStep(this, new ObjectStep(this, ObjectID.SOTN_GHORROCK_GATE_1, new WorldPoint(2916, 10338, 0),
		"Open the north gate using the code LEFT - UP - LEFT - DOWN."), new ObjectStep(this, ObjectID.SOTN_GHORROCK_GATE_1, new WorldPoint(2916, 10338, 0),
			"Open the north gate using a code you can work out."));
		// VarClientInt 1120 represents number of arrows
		// Left 1119 3->15
		// Right 1119 1->5
		// UP 1119 nothing?
		// down 1119 2->10
		// Need combo of LEFT (1/3), UP (2/3), LEFT (3/51) DOWN (4/179)
		// Reset 810.14
		// Confirm 810 15
		// Up 810.6
		// Down 810.7
		// Left 810.8
		// Right 810.9
		useLeverOnMechanism = new ObjectStep(this, ObjectID.SOTN_GHORROCK_LEVER, new WorldPoint(2917, 10342, 0), "Use the handle on the lever mechanism",
			leverHandle.highlighted());
		useLeverOnMechanism.addIcon(leverHandle.getId());
		useLeverOnMechanism.addDialogStep("Yes.");
		pullLever = new ObjectStep(this, ObjectID.SOTN_GHORROCK_LEVER, new WorldPoint(2917, 10342, 0), "Pull the lever.");
		pullLever.addDialogStep("Yes.");
		inspectPillar = new ObjectStep(this, ObjectID.SOTN_GHORROCK_PILLAR, new WorldPoint(2924, 10346, 0),
			"Inspect the south west pillar in the room where you fought the assassin.");
		combineShards = new DetailedQuestStep(this, "Use the jewel shards on one another.",
			jewelShard1. highlighted(), jewelShard2.highlighted());
		openIcyChest = new DetailedQuestStep(this, "Use the ancient jewel on the icy chest.",
			ancientJewel.highlighted(), icyChest.highlighted());
		openSouthGate = new ObjectStep(this, ObjectID.SOTN_GHORROCK_GATE_3, new WorldPoint(2918, 10321, 0),
			"Open the gate near the barrel you found the lever handle in.", icyKey);
		enterCrevice = new ObjectStep(this, ObjectID.GHORROCK_DUNGEON_CAVE_ENTRY, new WorldPoint(2908, 10317, 0),
			"Resupply if you haven't already. Prepare yourself for a challenging fight then enter the crevice.");

		defeatMuspah = new NpcStep(this, NpcID.MUSPAH_MELEE_QUEST, new WorldPoint(2849, 4259, 0), "Defeat the Strange Creature.");
		((NpcStep) defeatMuspah).addAlternateNpcs(NpcID.MUSPAH_QUEST, NpcID.SOTN_MUSPAH_CUTSCENE, NpcID.MUSPAH_SOULSPLIT_QUEST, NpcID.MUSPAH_SOULSPLIT_QUEST);
		moveToWeissCaveEnd = new ObjectStep(this, ObjectID.MY2ARM_THRONEROOM_STAIRSDOWN, new WorldPoint(2867, 3940, 0),
		"Speak to Jhallan in the Muspah room.");
		enterWeissCaveEnd = new ObjectStep(this, 46905, new WorldPoint(2846, 10332, 0),
			"Speak to Jhallan in the Muspah room.");
		enterCreviceEnd = new ObjectStep(this, ObjectID.GHORROCK_DUNGEON_CAVE_ENTRY, new WorldPoint(2908, 10317, 0),
			"Speak to Jhallan in the Muspah room.");
		talkToJhallan = new NpcStep(this, NpcID.SOTN_JHALLAN_GHORROCK, "Speak to Jhallan in the Muspah room.");
		talkToJhallan.addSubSteps(moveToWeissCaveEnd, enterWeissCaveEnd, enterCreviceEnd);
		continueCutscene = new NpcStep(this, NpcID.SOTN_HAZEEL_GHORROCK, new WorldPoint(2926, 10350, 0),
			"Talk to Hazeel inside the ruins for a teleport to Ardougne.");
		continueCutscene.addDialogStep("Yes.");
		finishQuest = new NpcStep(this, NpcID.GUARD_CARNILLEAN_VIS, new WorldPoint(2570, 3276, 0),
			"Return to the guard outside of the Carnillean Mansion in East Ardougne to finish the quest. " +
				"Talk to Hazeel for a teleport to Ardougne.");
		finishQuest.addAlternateNpcs(NpcID.SOTN_HAZEEL_GHORROCK);
		finishQuest.addDialogStep("Yes.");
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(coins, lockpick, tinderbox, combatGear, food);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(antipoison, ardyCloak, icyBasalt);
	}

	@Override
	public List<String> getCombatRequirements()
	{
		return Arrays.asList("Evelot (level 148)", "Assassin (level 262)", "Strange Creature (level 368)");
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> req = new ArrayList<>();
		req.add(new QuestRequirement(QuestHelperQuest.MAKING_FRIENDS_WITH_MY_ARM, QuestState.FINISHED));
		req.add(new QuestRequirement(QuestHelperQuest.THE_GENERALS_SHADOW, QuestState.FINISHED));
		req.add(new QuestRequirement(QuestHelperQuest.DEVIOUS_MINDS, QuestState.FINISHED));
		req.add(new QuestRequirement(QuestHelperQuest.HAZEEL_CULT, QuestState.FINISHED));
		req.add(new SkillRequirement(Skill.AGILITY, 69, false));
		req.add(new SkillRequirement(Skill.THIEVING, 64, false));
		req.add(new SkillRequirement(Skill.HUNTER, 56, false));
		return req;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(2);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Arrays.asList(
			new ExperienceReward(Skill.AGILITY, 60000),
			new ExperienceReward(Skill.THIEVING, 50000),
			new ExperienceReward(Skill.HUNTER, 40000)
		);
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Arrays.asList(
			new UnlockReward("Ability to mine ancient essence crystals in the Ghorrock Dungeon"),
			new UnlockReward("Access to a new boss, the Phantom Muspah"));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Starting Off",
			Arrays.asList(startQuest, goUpstairs, inspectBody, inspectWindow, inspectWall, climbHiddenLadder, inspectChest,
				goDownToGuard)));

		allSteps.add(new PanelDetails("Hunting Down Evelot",
			Arrays.asList(downStaircaseToKhaz, speakToBarman, inspectBarrel, inspectBoulder,
				inspectBush, inspectStump, inspectBoulder2, inspectBush2, fightEvelot,
				speakToEvelot), coins, combatGear));

		allSteps.add(new PanelDetails("The Mysterious Benefactor",
			Arrays.asList(enterCave, boardRaft, talkToAlomoneOrClivet, talkToHazeel, returnGuard, ladderToClaus, talkToClaus,
				examineShelves, examineWall, lockpickChest, inspectScroll, returnToHazeel), lockpick));

		allSteps.add(new PanelDetails("In The North",
			Arrays.asList(goNorth, talkToSnowflake, talkToTroll, moveToWeissCave,
				enterWeissCave, fightAssassin, talkToKhazard, talkToHazeelWeiss, searchBarrel, openCentreGate, openNorthChest, getTinderbox,
				lightNW, lightSE, lightNE, lightSW, openWestChest, openNorthGate, useLeverOnMechanism,
				pullLever, inspectPillar, combineShards, openIcyChest, openSouthGate),
			Collections.singletonList(combatGear), Arrays.asList(antipoison, icyBasalt)));


		allSteps.add(new PanelDetails("Secrets of the Dungeon",
			Arrays.asList(enterCrevice, defeatMuspah, talkToJhallan, continueCutscene, finishQuest), combatGear));

		return allSteps;
	}
}
