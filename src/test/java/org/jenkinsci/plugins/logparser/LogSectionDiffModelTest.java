package org.jenkinsci.plugins.logparser;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import org.junit.Test;

import hudson.plugins.logparser.*;

/**
 * 
 */
public class LogSectionDiffModelTest {

	@Test
	public void testAdded()
	{
		LogSection section1 = new LogSection(1);
		LogSection section2 = new LogSection(2);
		
		section1.setData(LogParserConsts.ERROR, Arrays.asList(
				"abc"
		));
		section2.setData(LogParserConsts.ERROR, Arrays.asList(
				"abc",
				"bcd"
		));
		LogSectionDiff diff = new LogSectionDiff(section1, section2);
		assertEquals(Arrays.asList("bcd"),diff.getDiffBySection(LogParserConsts.ERROR, LogSectionDiff.DiffType.ADD));
	}
	

	@Test
	public void testRemoved()
	{
		LogSection section1 = new LogSection(1);
		LogSection section2 = new LogSection(2);
		
		section1.setData(LogParserConsts.ERROR, Arrays.asList(
				"abc",
				"bcd"
		));
		section2.setData(LogParserConsts.ERROR, Arrays.asList(
				"abc"
		));
		LogSectionDiff diff = new LogSectionDiff(section1, section2);
		System.out.println();
		assertEquals(Arrays.asList("bcd"),diff.getDiffBySection(LogParserConsts.ERROR, LogSectionDiff.DiffType.REMOVED));
	}
	
	
}
