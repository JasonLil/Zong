package com.xenoage.zong.musiclayout.notator.beam;

import static com.xenoage.utils.math.Fraction.fr;
import static com.xenoage.zong.core.music.Pitch.pi;
import static com.xenoage.zong.core.music.beam.Beam.beamFromChords;
import static com.xenoage.zong.core.music.chord.StemDirection.Down;
import static com.xenoage.zong.core.music.chord.StemDirection.Up;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static com.xenoage.zong.musiclayout.notations.BeamNotation.Waypoint.HookLeft;
import static com.xenoage.zong.musiclayout.notations.BeamNotation.Waypoint.HookRight;
import static com.xenoage.zong.musiclayout.notations.BeamNotation.Waypoint.None;
import static com.xenoage.zong.musiclayout.notations.BeamNotation.Waypoint.Start;
import static com.xenoage.zong.musiclayout.notations.BeamNotation.Waypoint.Stop;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.xenoage.utils.math.Fraction;
import com.xenoage.zong.core.music.beam.Beam;
import com.xenoage.zong.core.music.chord.Chord;
import com.xenoage.zong.core.music.chord.Note;
import com.xenoage.zong.musiclayout.notations.BeamNotation.Waypoint;
import com.xenoage.zong.musiclayout.notator.beam.BeamNotator;
import com.xenoage.zong.musiclayout.stamper.BeamStamper;

/**
 * Tests for {@link BeamNotator}.
 * 
 * The test examples are from Chlapik, page 45, rule 6.
 * 
 * @author Andreas Wenger
 */
public class BeamNotatorTest {

	BeamNotator testee = BeamNotator.beamNotator;


	@Test public void getLevelsTest() {
		assertEquals(2, testee.getMaxLinesCount(exampleRow1Col1()));
		assertEquals(2, testee.getMaxLinesCount(exampleRow1Col2()));
		assertEquals(2, testee.getMaxLinesCount(exampleRow1Col3()));
		assertEquals(2, testee.getMaxLinesCount(exampleRow1Col4()));
		assertEquals(2, testee.getMaxLinesCount(exampleRow2Col1()));
		assertEquals(2, testee.getMaxLinesCount(exampleRow2Col2()));
		assertEquals(3, testee.getMaxLinesCount(exampleRow2Col3()));
		assertEquals(3, testee.getMaxLinesCount(exampleRow2Col4()));
		assertEquals(3, testee.getMaxLinesCount(exampleRow2Col5()));
		assertEquals(3, testee.getMaxLinesCount(exampleRow2Col6()));
		assertEquals(2, testee.getMaxLinesCount(exampleRow3Col2()));
		assertEquals(2, testee.getMaxLinesCount(exampleRow3Col4()));
		assertEquals(3, testee.getMaxLinesCount(exampleRow3Col6()));
	}
	
	@Test public void testIsBeamOutsideStaff() {
		//stem up, above staff
		assertTrue(testee.isBeamOutsideStaff(Up, 13, 13, 5, 2));
		assertTrue(testee.isBeamOutsideStaff(Up, 12.1f, 13, 5, 2));
		assertTrue(testee.isBeamOutsideStaff(Up, 13f, 12.1f, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Up, 11.9f, 11.9f, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Up, 11.9f, 13, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Up, 13f, 11.9f, 5, 2));
		//stem up, below staff
		assertTrue(testee.isBeamOutsideStaff(Up, -1, -1, 5, 2));
		assertTrue(testee.isBeamOutsideStaff(Up, -0.1f, -1, 5, 2));
		assertTrue(testee.isBeamOutsideStaff(Up, -1, -0.1f, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Up, 0.1f, 0.1f, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Up, 0.1f, -1, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Up, -1, 0.1f, 5, 2));
		//stem down, above staff
		assertTrue(testee.isBeamOutsideStaff(Down, 9, 9, 5, 2));
		assertTrue(testee.isBeamOutsideStaff(Down, 8.1f, 9, 5, 2));
		assertTrue(testee.isBeamOutsideStaff(Down, 9, 8.1f, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Down, 7.9f, 7.9f, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Down, 7.9f, 9, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Down, 9, 7.9f, 5, 2));
		//stem down, below staff
		assertTrue(testee.isBeamOutsideStaff(Down, -5, -5, 5, 2));
		assertTrue(testee.isBeamOutsideStaff(Down, -4.1f, -5, 5, 2));
		assertTrue(testee.isBeamOutsideStaff(Down, -5, -4.1f, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Down, -3.9f, -3.9f, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Down, -3.9f, -5, 5, 2));
		assertFalse(testee.isBeamOutsideStaff(Down, -5, -3.9f, 5, 2));
	}


	@Test public void computeWaypointsTest() {
		Beam b;

		//example of row 1, column 1
		b = exampleRow1Col1();
		try {
			testee.computeWaypoints(b, 0, null); //not working for 8th lines
			fail();
		} catch (IllegalArgumentException ex) {
			//ok
		}
		List<Waypoint> wp = testee.computeWaypoints(b, 2, null);
		assertEqualsList(wp, None, None, None); //32th
		assertEqualsList(testee.computeWaypoints(b, 1, wp), HookRight, None, HookLeft); //16th

		//example of row 1, column 2
		b = exampleRow1Col2();
		assertEqualsList(testee.computeWaypoints(b, 1, null), HookRight, None, HookLeft); //16th

		//example of row 1, column 3
		b = exampleRow1Col3();
		assertEqualsList(testee.computeWaypoints(b, 1, null), HookRight, None, None, HookLeft); //16th

		//example of row 1, column 4
		b = exampleRow1Col4();
		assertEqualsList(testee.computeWaypoints(b, 1, null), HookRight, None, None, HookLeft); //16th

		//example of row 2, column 1
		b = exampleRow2Col1();
		assertEqualsList(testee.computeWaypoints(b, 1, null), None, HookLeft); //16th

		//example of row 2, column 2
		b = exampleRow2Col2();
		assertEqualsList(testee.computeWaypoints(b, 1, null), None, HookLeft); //16th

		//example of row 2, column 3
		b = exampleRow2Col3();
		wp = testee.computeWaypoints(b, 2, null);
		assertEqualsList(wp, None, HookLeft, None, HookLeft); //32th
		assertEqualsList(testee.computeWaypoints(b, 1, wp), Start, None, None, Stop); //16th

		//example of row 2, column 4
		b = exampleRow2Col4();
		wp = testee.computeWaypoints(b, 2, null);
		assertEqualsList(wp, None, HookLeft, None, HookLeft); //32th
		assertEqualsList(testee.computeWaypoints(b, 1, wp), Start, None, None, Stop); //16th

		//example of row 3, column 2
		b = exampleRow3Col2();
		assertEqualsList(testee.computeWaypoints(b, 1, null), HookRight, None, HookLeft, None); //16th

		//example of row 3, column 4
		b = exampleRow3Col4();
		assertEqualsList(testee.computeWaypoints(b, 1, null), None, HookRight, None); //16th

		//example of row 3, column 6
		b = exampleRow3Col6();
		wp = testee.computeWaypoints(b, 2, null);
		assertEqualsList(wp, None, HookLeft, None, HookLeft); //32th
		assertEqualsList(testee.computeWaypoints(b, 1, wp), None, HookLeft, Start, Stop); //16th

	}

	private Beam exampleRow1Col1() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordC(fr(1, 16)));
		chords.add(chordC(fr(1, 8)));
		chords.add(chordC(fr(1, 16)));
		return beamFromChords(chords);
	}

	private Beam exampleRow1Col2() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordF(fr(1, 16)));
		chords.add(chordF(fr(1, 8)));
		chords.add(chordF(fr(1, 16)));
		return beamFromChords(chords);
	}

	private Beam exampleRow1Col3() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordC(fr(1, 16)));
		chords.add(chordC(fr(1, 8)));
		chords.add(chordC(fr(1, 8)));
		chords.add(chordC(fr(1, 16)));
		return beamFromChords(chords);
	}

	private Beam exampleRow1Col4() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordF(fr(1, 16)));
		chords.add(chordF(fr(1, 8)));
		chords.add(chordF(fr(1, 8)));
		chords.add(chordF(fr(1, 16)));
		return beamFromChords(chords);
	}

	private Beam exampleRow2Col1() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordC(fr(3, 16)));
		chords.add(chordC(fr(1, 16)));
		return beamFromChords(chords);
	}

	private Beam exampleRow2Col2() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordF(fr(3, 16)));
		chords.add(chordF(fr(1, 16)));
		return beamFromChords(chords);
	}

	private Beam exampleRow2Col3() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordC(fr(3, 32)));
		chords.add(chordC(fr(1, 32)));
		chords.add(chordC(fr(3, 32)));
		chords.add(chordC(fr(1, 32)));
		return beamFromChords(chords);
	}

	private Beam exampleRow2Col4() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordF(fr(3, 32)));
		chords.add(chordF(fr(1, 32)));
		chords.add(chordF(fr(3, 32)));
		chords.add(chordF(fr(1, 32)));
		return beamFromChords(chords);
	}

	private Beam exampleRow2Col5() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordC(fr(1, 8)));
		chords.add(chordC(fr(1, 32)));
		chords.add(chordC(fr(3, 32)));
		return beamFromChords(chords);
	}

	private Beam exampleRow2Col6() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordF(fr(1, 8)));
		chords.add(chordF(fr(1, 32)));
		chords.add(chordF(fr(3, 32)));
		return beamFromChords(chords);
	}

	private Beam exampleRow3Col2() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordC(fr(1, 16)));
		chords.add(chordC(fr(1, 8)));
		chords.add(chordC(fr(1, 16)));
		chords.add(chordC(fr(1, 8)));
		return beamFromChords(chords);
	}

	private Beam exampleRow3Col4() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordC(fr(1, 8)));
		chords.add(chordC(fr(1, 16)));
		chords.add(chordC(fr(3, 16)));
		return beamFromChords(chords);
	}

	private Beam exampleRow3Col6() {
		LinkedList<Chord> chords = new LinkedList<Chord>();
		chords.add(chordC(fr(7, 32)));
		chords.add(chordC(fr(1, 32)));
		chords.add(chordC(fr(3, 32)));
		chords.add(chordC(fr(1, 32)));
		return beamFromChords(chords);
	}

	private Chord chordC(Fraction duration) {
		return new Chord(new Note(pi(0, 0, 5)), duration);
	}

	private Chord chordF(Fraction duration) {
		return new Chord(new Note(pi(3, 0, 4)), duration);
	}

	private void assertEqualsList(List<Waypoint> list, Waypoint... expected) {
		for (int i = 0; i < expected.length; i++) {
			assertEquals("Fail at position " + i, expected[i], list.get(i));
		}
	}

}
