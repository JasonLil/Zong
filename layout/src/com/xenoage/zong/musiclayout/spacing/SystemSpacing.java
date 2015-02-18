package com.xenoage.zong.musiclayout.spacing;

import static com.xenoage.utils.collections.ArrayUtils.sum;
import static com.xenoage.utils.collections.CollectionUtils.getFirst;
import static com.xenoage.utils.collections.CollectionUtils.getLast;
import static com.xenoage.utils.kernel.Range.range;
import static com.xenoage.zong.core.position.MP.atBeat;
import static com.xenoage.zong.core.position.MP.unknown;
import static com.xenoage.zong.core.position.MP.unknownMp;

import java.util.List;

import lombok.Getter;

import com.xenoage.utils.kernel.Range;
import com.xenoage.utils.math.Fraction;
import com.xenoage.zong.core.position.MP;

/**
 * The horizontal and vertical spacing of a system.
 * 
 * It contains the indices of the first and last measure of the system, the widths of all
 * measure columns, the width of the system (which may be longer than the width used
 * by the measures), the distances between the staves and the vertical offset of the system.
 *
 * @author Andreas Wenger
 */
@Getter
public class SystemSpacing {

	/** The list of the spacings of the measure columns in this system. */
	public List<ColumnSpacing> columnSpacings;

	/** The left margin of the system in mm. */
	public float marginLeftMm;
	/** The right margin of the system in mm. */
	public float marginRightMm;

	/** The width of the system (without the horizontal offset).
	 * It may be longer than the used width, e.g. to create empty staves.
	 * To get the used width, call {@link #getUsedWidth()}. */
	public float widthMm;

	/** The heights of the staves in mm. (#staves-1) items. */
	public float[] staffHeightsMm;
	/** The distances between the staves in mm. (#staves-2) items. */
	public float[] staffDistancesMm;

	/** The vertical offset of the system in mm, relative to the top. */
	public float offsetYMm;

	/** Backward reference to the frame. */
	public FrameSpacing parentFrame = null;
	

	public SystemSpacing(List<ColumnSpacing> columnSpacings, float marginLeftMm, float marginRightMm,
		float widthMm, float[] staffHeightsMm, float[] staffDistancesMm, float offsetYMm) {
		this.columnSpacings = columnSpacings;
		this.marginLeftMm = marginLeftMm;
		this.marginRightMm = marginRightMm;
		this.widthMm = widthMm;
		this.staffHeightsMm = staffHeightsMm;
		this.staffDistancesMm = staffDistancesMm;
		this.offsetYMm = offsetYMm;
		//set backward references
		for (ColumnSpacing column : columnSpacings)
			column.parentSystem = this;
	}
	
	/**
	 * Gets the height of the staff with the given index.
	 */
	public float getStaffHeight(int index) {
		return staffHeightsMm[index];
	}

	/**
	 * Gets the distance between the previous and the given staff.
	 */
	public float getStaffDistance(int index) {
		return (index > 0 ? staffDistancesMm[index - 1] : 0);
	}

	/**
	 * Gets the total height of this system in mm.
	 */
	public float getHeight() {
		return sum(staffHeightsMm) + sum(staffDistancesMm);
	}

	/**
	 * Gets the used width of the system.
	 */
	public float getUsedWidth() {
		float usedWidth = 0;
		for (ColumnSpacing mcs : columnSpacings)
			usedWidth += mcs.getWidthMm();
		return usedWidth;
	}
	
	public int getStartMeasureIndex() {
		return getFirst(columnSpacings).measureIndex;
	}
	
	public int getEndMeasureIndex() {
		return getLast(columnSpacings).measureIndex;
	}
	
	public boolean containsMeasure(int scoreMeasure) {
		return getStartMeasureIndex() <= scoreMeasure && scoreMeasure <= getEndMeasureIndex();
	}
	
	public Range getMeasureIndices() {
		return range(getStartMeasureIndex(), getEndMeasureIndex());
	}
	
	public ColumnSpacing getColumn(int scoreMeasure) {
		return columnSpacings.get(scoreMeasure - getStartMeasureIndex());
	}
	
	public int getSystemIndexInFrame() {
		return parentFrame.systems.indexOf(this);
	}
	
	/**
	 * Gets the start position in mm of the measure with the given global index
	 * relative to the beginning of the system.
	 */
	public float getMeasureStartMm(int scoreMeasure) {
		float x = 0;
		for (int iMeasure : range(scoreMeasure - getStartMeasureIndex()))
			x += columnSpacings.get(iMeasure).getWidthMm();
		return x;
	}
	
	/**
	 * Gets the end position of the leading spacing in mm of the measure with the given
	 * global index, relative to the beginning of the system.
	 * If there is no leading spacing, this value is equal to {@link #getMeasureStartMm(int)}
	 */
	public float getMeasureStartAfterLeadingMm(int scoreMeasure) {
		int systemMeasure = scoreMeasure - getStartMeasureIndex();
		return getMeasureStartMm(scoreMeasure) + columnSpacings.get(systemMeasure).getLeadingWidthMm();
	}
	
	/**
	 * Gets the end position in mm of the measure with the given global index
	 * relative to the beginning of the system.
	 */
	public float getMeasureEndMm(int scoreMeasure) {
		int systemMeasure = scoreMeasure - getStartMeasureIndex();
		return getMeasureStartMm(scoreMeasure) + columnSpacings.get(systemMeasure).getWidthMm();
	}

	/**
	 * Gets the {@link MP} at the given horizontal position in mm.
	 * If the given staff is {@link MP#unknown}, all beats of the column
	 * are used, otherwise only the beats used by the given staff.
	 * 
	 * If it is between two beats (which will be true almost ever), the
	 * the right mark is selected (like it is usual e.g. in text
	 * processing applications). If it is behind all known beats of the
	 * hit measure, the last known beat is returned.
	 * 
	 * If it is not within the boundaries of a measure, {@link MP#unknownMp} is returned.
	 */
	public MP getMpAt(float xMm, int staff) {
		//find the measure
		int measureIndex = getSystemMeasureIndexAt(xMm);
		float xMmInMeasure = xMm - getMeasureStartMm(measureIndex);
		//when measure was not found, return null
		if (measureIndex == unknown)
			return unknownMp;
		//get the beat at the given position
		Fraction beat = columnSpacings.get(measureIndex).getBeatAt(xMmInMeasure, staff);
		return atBeat(staff, measureIndex, unknown, beat);
	}
	
	/**
	 * Gets the horizontal position in mm, relative to the beginning of the staff,
	 * of the given measure and beat.
	 * If the given beat is after the last beat, the offset of the last beat is returned.
	 */
	public float getXMmAt(int scoreMeasure, Fraction beat) {
		float measureXMm = getMeasureStartMm(scoreMeasure);
		float elementXMm = columnSpacings.get(scoreMeasure - getStartMeasureIndex()).getXMmAt(beat);
		return measureXMm + elementXMm;
	}
	
	/**
	 * See {@link #getXMmAt(int, Fraction)}.
	 */
	public float getXMmAt(MP mp) {
		return getXMmAt(mp.measure, mp.beat);
	}

	/**
	 * Gets the system-relative index of the measure at the given position in mm,
	 * or {@link MP#unknown} if there is none.
	 */
	private int getSystemMeasureIndexAt(float xMm) {
		if (xMm < 0)
			return unknown;
		float x = 0;
		for (int iMeasure : range(columnSpacings)) {
			x += columnSpacings.get(iMeasure).getWidthMm();
			if (xMm < x)
				return iMeasure;
		}
		return unknown;
	}
}
