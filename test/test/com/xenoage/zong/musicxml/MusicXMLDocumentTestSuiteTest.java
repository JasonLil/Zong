package com.xenoage.zong.musicxml;

import static com.xenoage.utils.jse.JsePlatformUtils.jsePlatformUtils;
import static com.xenoage.utils.math.Fraction.fr;
import static com.xenoage.zong.core.music.Pitch.pi;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.xenoage.utils.math.Fraction;
import com.xenoage.zong.core.music.Pitch;
import com.xenoage.zong.musicxml.types.MxlNote;
import com.xenoage.zong.musicxml.types.MxlPitch;
import com.xenoage.zong.musicxml.types.MxlSyllabicText;
import com.xenoage.zong.musicxml.types.choice.MxlFullNoteContent.MxlFullNoteContentType;
import com.xenoage.zong.musicxml.types.choice.MxlMusicDataContent;
import com.xenoage.zong.musicxml.types.choice.MxlMusicDataContent.MxlMusicDataContentType;
import com.xenoage.zong.musicxml.types.choice.MxlNormalNote;
import com.xenoage.zong.musicxml.types.groups.MxlFullNote;
import com.xenoage.zong.musicxml.types.partwise.MxlMeasure;
import com.xenoage.zong.musicxml.types.partwise.MxlPart;

/**
 * Test the {@link MusicXMLDocument} class for the documents in the
 * <a href="http://lilypond.org/doc/v2.12/input/regression/musicxml/collated-files">
 * Unofficial MusicXML test suite</a>, published under the GPL license.
 * 
 * @author Andreas Wenger
 */
public class MusicXMLDocumentTestSuiteTest
	extends MusicXMLTestSuite {

	@Test @Override public void test_01a_Pitches_Pitches() {
		MusicXMLDocument doc = load("01a-Pitches-Pitches.xml");
		MxlPart part = doc.getScore().getParts().get(0);
		assertEquals(26, part.getMeasures().size());
		int iPitch = 0;
		Pitch[] expectedPitches = get_01a_Pitches_Pitches();
		for (int iM = 0; iM < part.getMeasures().size(); iM++) {
			MxlMeasure measure = part.getMeasures().get(iM);
			for (MxlMusicDataContent data : measure.getMusicData().getContent()) {
				if (data.getMusicDataContentType() == MxlMusicDataContentType.Note) {
					//check note and pitch
					MxlFullNote note = ((MxlNote) data).getContent().getFullNote();
					MxlPitch pitch = (MxlPitch) (note.getContent());
					assertEquals("note " + iPitch, expectedPitches[iPitch++], pitch.getPitch());
				}
			}
		}
		assertEquals("not all notes found", expectedPitches.length, iPitch);
		//TODO: the editiorial sharp (sharp in parenthesis) in the last measure
		//is not supported yet
	}

	@Test @Override public void test_01b_Pitches_Intervals() {
		//the MusicXML file contains only a single measure (possibly an error in the test suite)
		MusicXMLDocument doc = load("01b-Pitches-Intervals.xml");
		Pitch[] expectedPitches = get_01b_Pitches_Intervals();
		MxlMeasure measure = doc.getScore().getParts().get(0).getMeasures().get(0);
		int iPitch = 0;
		for (MxlMusicDataContent data : measure.getMusicData().getContent()) {
			if (data.getMusicDataContentType() == MxlMusicDataContentType.Note) {
				//check note and pitch
				MxlFullNote note = ((MxlNote) data).getContent().getFullNote();
				MxlPitch pitch = (MxlPitch) (note.getContent());
				assertEquals("note " + iPitch, expectedPitches[iPitch++], pitch.getPitch());
			}
		}
		assertEquals("not all notes found", expectedPitches.length, iPitch);
	}

	@Test @Override public void test_01c_Pitches_NoVoiceElement() {
		MusicXMLDocument doc = load("01c-Pitches-NoVoiceElement.xml");
		MxlMeasure measure = doc.getScore().getParts().get(0).getMeasures().get(0);
		for (MxlMusicDataContent data : measure.getMusicData().getContent()) {
			if (data.getMusicDataContentType() == MxlMusicDataContentType.Note) {
				//check pitch
				MxlNote note = (MxlNote) data;
				MxlFullNote fullNote = note.getContent().getFullNote();
				MxlPitch pitch = (MxlPitch) (fullNote.getContent());
				assertEquals(pi('G', 0, 4), pitch.getPitch());
				//check lyric
				assertEquals(1, note.getLyrics().size());
				assertEquals("A", ((MxlSyllabicText) note.getLyrics().get(0).getContent()).getText()
					.getValue());
				return;
			}
		}
		fail("note not found");
	}

	@Test @Override public void test_02a_Rests_Durations() {
		//start in measure 3 (multirests are not supported yet - TODO)
		MusicXMLDocument doc = load("02a-Rests-Durations.xml");
		MxlPart part = doc.getScore().getParts().get(0);
		Fraction[] expectedDurations = get_02a_Rests_Durations();
		int iDuration = 0;
		int divisions = 32; //from MusicXML file
		for (int iM = 2; iM < part.getMeasures().size(); iM++) {
			MxlMeasure measure = part.getMeasures().get(iM);
			for (MxlMusicDataContent data : measure.getMusicData().getContent()) {
				if (data.getMusicDataContentType() == MxlMusicDataContentType.Note) {
					//check type and duration
					MxlNormalNote note = (MxlNormalNote) ((MxlNote) data).getContent();
					assertEquals(MxlFullNoteContentType.Rest, note.getFullNote().getContent()
						.getFullNoteContentType());
					assertEquals("rest " + iDuration, expectedDurations[iDuration++],
						fr(note.getDuration(), divisions * 4));
				}
			}
		}
		assertEquals("not all rests found", expectedDurations.length, iDuration);
	}
	
	@Test @Override public void test_03a_Rhythm_Durations() {
		MusicXMLDocument doc = load("03a-Rhythm-Durations.xml");
		MxlPart part = doc.getScore().getParts().get(0);
		Fraction[] expectedDurations = get_03a_Rhythm_Durations();
		int iDuration = 0;
		int divisions = 64; //from MusicXML file
		for (int iM = 0; iM < part.getMeasures().size(); iM++) {
			MxlMeasure measure = part.getMeasures().get(iM);
			for (MxlMusicDataContent data : measure.getMusicData().getContent()) {
				if (data.getMusicDataContentType() == MxlMusicDataContentType.Note) {
					//check type and duration
					MxlNormalNote note = (MxlNormalNote) ((MxlNote) data).getContent();
					assertEquals(MxlFullNoteContentType.Pitch, note.getFullNote().getContent()
						.getFullNoteContentType());
					assertEquals("note " + iDuration, expectedDurations[iDuration++],
						fr(note.getDuration(), divisions * 4));
				}
			}
		}
		assertEquals("not all notes found", expectedDurations.length, iDuration);
	}
	
	@Test @Override public void test_03b_Rhythm_Backup() {
		MusicXMLDocument doc = load("03b-Rhythm-Backup.xml");
		//elements in this measure: attributes, note, note, backup, note, note
		MxlMeasure measure = doc.getScore().getParts().get(0).getMeasures().get(0);
		List<MxlMusicDataContent> content = measure.getMusicData().getContent();
		assertEquals(6, content.size());
		assertEquals(MxlMusicDataContentType.Attributes, content.get(0).getMusicDataContentType());
		assertEquals(MxlMusicDataContentType.Note, content.get(1).getMusicDataContentType());
		assertEquals(MxlMusicDataContentType.Note, content.get(2).getMusicDataContentType());
		assertEquals(MxlMusicDataContentType.Backup, content.get(3).getMusicDataContentType());
		assertEquals(MxlMusicDataContentType.Note, content.get(4).getMusicDataContentType());
		assertEquals(MxlMusicDataContentType.Note, content.get(5).getMusicDataContentType());
	}

	private MusicXMLDocument load(String filename) {
		try {
			return MusicXMLDocument.read(jsePlatformUtils().createXmlReader(
				jsePlatformUtils().openFile(dir + filename)));
		} catch (Exception ex) {
			ex.printStackTrace();
			Assert.fail("Could not load " + filename + ": " + ex.toString());
			return null;
		}
	}

}