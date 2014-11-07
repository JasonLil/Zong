package musicxmltestsuite.tests.musicxmlin;

import static musicxmltestsuite.tests.utils.ScoreTest.assertEqualsEndBarlines;
import musicxmltestsuite.tests.base.Base45a;

import org.junit.Test;

import com.xenoage.zong.core.Score;


public class Test45a
	implements Base45a, MusicXmlInTest {
	
	@Test public void test() {
		Score score = getScore();
		assertEqualsEndBarlines(expectedEndBarlines, score);
	}

	
	
}
