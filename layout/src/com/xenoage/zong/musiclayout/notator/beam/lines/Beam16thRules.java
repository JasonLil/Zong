package com.xenoage.zong.musiclayout.notator.beam.lines;

import static com.xenoage.utils.math.MathUtils.mod;
import static com.xenoage.zong.musiclayout.notations.BeamNotation.defaultGapIs;
import static com.xenoage.zong.musiclayout.notations.BeamNotation.lineHeightIs;
import static com.xenoage.zong.musiclayout.notator.beam.BeamNotator.beamNotator;
import lombok.AllArgsConstructor;

import com.xenoage.zong.core.music.chord.StemDirection;

/**
 * Implementation of a {@link BeamLinesRules} strategy for a two-line beam (16th).
 * 
 * @author Uli Teschemacher
 * @author Andreas Wenger
 */
@AllArgsConstructor
public class Beam16thRules
	extends BeamRules {

	private static float totalBeamHeightIs = 2 * lineHeightIs + defaultGapIs;

	private StemDirection stemDirection;
	private int staffLinesCount;
	
	
	@Override public int getBeamLinesCount() {
		return 2;
	}

	@Override public float getMinimumStemLengthIs() {
		return 3.25f;
	}

	@Override public boolean isGoodStemPosition(float startLp, float slantIs) {
		int maxStaffLp = 2 * (staffLinesCount - 1);
		//look whether the beam doesn't start or end at a wrong position (e.g. between the lines)
		if ((startLp < -3 && startLp + slantIs * 2 < -3) ||
			(startLp > maxStaffLp + 3 && startLp + slantIs * 2 > maxStaffLp + 3))
			return true;
			
		//TODO: some of the following 4 are possibly not really always 4 but
		//are dependent on the staffLinesCount.
		int linepositionstart = mod((int) (startLp * 2), 4);
		int linepositionend = mod((int) (startLp + slantIs * 2) * 2, 4);
		if (stemDirection == StemDirection.Down) {
			if (startLp <= 4 && startLp + slantIs * 2 <= 4) {
				//downstems must only straddle the line or sit on it (at the beginning).
				//the end of the stem must not be in the space between two lines.
				if (Math.abs(slantIs) < 0.1f) {
					if (linepositionstart == 0 || linepositionstart == 3)
						return true;
				}
				else {
					if ((linepositionstart == 0 && linepositionend == 3) ||
						(linepositionstart == 3 && linepositionend == 0))
						return true;
				}
			}
		}
		else {
			if (startLp >= 4 && startLp + slantIs * 2 >= 4) {
				if (Math.abs(slantIs) < 0.1f) {
					if (linepositionstart == 0 || linepositionstart == 1)
						return true;
				}
				else {
					if ((linepositionstart == 0 && linepositionend == 1) ||
						(linepositionstart == 1 && linepositionend == 0))
						return true;
				}
			}
		}
		return false;
	}

	@Override public float getSlantCloseSpacingIs(int startNoteLp, int endNoteLp) {
		int dir = stemDirection.getSign();
		float startStemLp = startNoteLp + dir * getMinimumStemLengthIs(); //TODO: *2 missing for IS->LP?
		float endStemLp = endNoteLp + dir * getMinimumStemLengthIs(); //TODO: *2 missing for IS->LP?
		if (beamNotator.isBeamOutsideStaff(stemDirection, startStemLp, endStemLp, staffLinesCount, totalBeamHeightIs)) {
			//use design of single beam
			Beam8thRules sbd = new Beam8thRules(stemDirection, staffLinesCount);
			return sbd.getSlantNormalSpacingIs(startNoteLp, endNoteLp);
		}
		else {
			return 0.5f;
		}
	}

	@Override public float getSlantNormalSpacingIs(int startNoteLP, int endNoteLP) {
		return getNormalOrWideSpacing(startNoteLP, endNoteLP, false);
	}

	@Override public float getSlantWideSpacingIs(int startNoteLP, int endNoteLP) {
		return getNormalOrWideSpacing(startNoteLP, endNoteLP, true);
	}

	private float getNormalOrWideSpacing(int startNoteLp, int endNoteLp, boolean wide) {
		int staffMaxLp = (staffLinesCount - 1) * 2;
		int dir = stemDirection.getSign();
		float startStemLp = startNoteLp + dir * getMinimumStemLengthIs();  //TODO: *2 missing for IS->LP?
		float endStemLp = endNoteLp + dir * getMinimumStemLengthIs(); //TODO: *2 missing for IS->LP?
		if ((startStemLp < -1 && endStemLp < -1) ||
			(startStemLp > staffMaxLp + 1 && endStemLp > staffMaxLp + 1)) {
			//use design of single beam
			Beam8thRules sbd = new Beam8thRules(stemDirection, staffLinesCount);
			return sbd.getSlantNormalSpacingIs(startNoteLp, endNoteLp);
		}
		else {
			return getSlants(Math.abs(startNoteLp - endNoteLp))[wide ? 1 : 0];
		}
	}

	private float[] getSlants(int differenceLp) {
		differenceLp = Math.abs(differenceLp);
		float[][] ted = { //like in Ted Ross' book
		{ 0, 0 }, { 0.5F, 0.5F }, { 0.5F, 1.5F }, { 2, 2.5F }, { 2.5F, 2.5F }, { 2.5F, 2.5F },
			{ 2.5F, 3.5F }, { 2.5F, 4F } };
		/*float[][] sib = {  //like in sibelius
			{0,0},
			{0.5F,0.5F},
			{0.5F,0.5F},
			{1.5F,2}
		};*/
		float[][] used = ted;
		if (used.length - 1 < differenceLp) {
			return used[used.length - 1];
		}
		else {
			return used[differenceLp];
		}
	}

}
