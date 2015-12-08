package org.jenkinsci.plugins.logparser;

import hudson.plugins.logparser.DiffToHtmlUtils;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;
import java.util.regex.Pattern;

public class DiffToHtmlUtilsTest {
    private int build1;
    private int build2;
    private String diffType;
    private Map<String, List<String>> content1;
    private Map<String, List<String>> content2;
    private Map<String, String> iconLocations;
    private static List<String> dummyValue = Collections.emptyList();

    @Before
    public void setUp() {
        build1 = 2;
        build2 = 5;
        diffType = "Source Code";
        content1 = new LinkedHashMap<>();
        content2 = new LinkedHashMap<>();
        iconLocations = new HashMap<>();
    }

    private String generateUniquePattern() {
        return generateUniquePattern(null, null);
    }

    private String generateUniquePattern(Integer build, String item) {
        if (build == null || item == null) {
            return "build . unique:";
        } else {
            return "build " + build + " unique: " + item;
        }
    }

    private String generateSharePattern() {
        return generateSharePattern(null, null, null);
    }

    private String generateSharePattern(Integer build1, Integer build2, String item) {
        if (build1 == null || build2 == null || item == null) {
            return "build . and build . share .";
        } else {
            return "build " + build1 + " and build " + build2 + " share " + item;
        }
    }

    private boolean matchAndFind(String regex, String str) {
        return Pattern.compile(regex).matcher(str).find();
    }

    @Test
    public void testTitle() throws Exception {
        String html = DiffToHtmlUtils.generateDiffHTML(build1, build2, diffType, content1, content2, iconLocations);
        assertTrue(html.contains(diffType + " diff between build " + build1 + " and build " + build2));
    }

    @Test
    public void testBothEmptyContents() throws Exception {
        String html = DiffToHtmlUtils.generateDiffHTML(build1, build2, diffType, content1, content2, iconLocations);
        assertFalse(matchAndFind(generateUniquePattern(), html));
        assertFalse(matchAndFind(generateSharePattern(), html));
    }

    @Test
    public void testContent1EmptyContent2NonEmpty() throws Exception {
        content2.put("a/", dummyValue);
        content2.put("a/1.java", dummyValue);
        String html = DiffToHtmlUtils.generateDiffHTML(build1, build2, diffType, content1, content2, iconLocations);
        assertTrue(matchAndFind(generateUniquePattern(build2, "a/"), html));
        assertTrue(matchAndFind(generateUniquePattern(build2, "a/1.java"), html));
    }

    @Test
    public void testContent2EmptyContent1NonEmpty() throws Exception {
        content1.put("a/", dummyValue);
        content1.put("a/1.java", dummyValue);
        String html = DiffToHtmlUtils.generateDiffHTML(build1, build2, diffType, content1, content2, iconLocations);
        assertTrue(matchAndFind(generateUniquePattern(build1, "a/"), html));
        assertTrue(matchAndFind(generateUniquePattern(build1, "a/1.java"), html));
    }

    @Test
    public void testContent1AndContent2AllShare() throws Exception {
        content1.put("a/", dummyValue);
        content1.put("a/1.java", dummyValue);
        content2.put("a/", dummyValue);
        content2.put("a/1.java", dummyValue);
        String html = DiffToHtmlUtils.generateDiffHTML(build1, build2, diffType, content1, content2, iconLocations);
        assertTrue(matchAndFind(generateSharePattern(build1, build2, "a/"), html));
        assertTrue(matchAndFind(generateSharePattern(build1, build2, "a/1.java"), html));
    }

    @Test
    public void testContent1AndContent2AllUnique() throws Exception {
        content1.put("a/", dummyValue);
        content1.put("a/1.java", dummyValue);
        content2.put("b/", dummyValue);
        content2.put("b/1.java", dummyValue);
        String html = DiffToHtmlUtils.generateDiffHTML(build1, build2, diffType, content1, content2, iconLocations);
        assertTrue(matchAndFind(generateUniquePattern(build1, "a/"), html));
        assertTrue(matchAndFind(generateUniquePattern(build1, "a/1.java"), html));
        assertTrue(matchAndFind(generateUniquePattern(build2, "b/"), html));
        assertTrue(matchAndFind(generateUniquePattern(build2, "b/1.java"), html));
    }

    @Test
    public void testContent1IncludeContent2() throws Exception {
        content1.put("a/", dummyValue);
        content1.put("a/1.java", dummyValue);
        content2.put("a/", dummyValue);
        String html = DiffToHtmlUtils.generateDiffHTML(build1, build2, diffType, content1, content2, iconLocations);
        assertTrue(matchAndFind(generateSharePattern(build1, build2, "a/"), html));
        assertTrue(matchAndFind(generateUniquePattern(build1, "a/1.java"), html));
    }

    @Test
    public void testContent2IncludeContent1() throws Exception {
        content2.put("a/", dummyValue);
        content2.put("a/1.java", dummyValue);
        content1.put("a/", dummyValue);
        String html = DiffToHtmlUtils.generateDiffHTML(build1, build2, diffType, content1, content2, iconLocations);
        assertTrue(matchAndFind(generateSharePattern(build1, build2, "a/"), html));
        assertTrue(matchAndFind(generateUniquePattern(build2, "a/1.java"), html));
    }

    @Test
    public void testContent1AndContent2Intersect() throws Exception {
        content1.put("a/", dummyValue);
        content1.put("a/1.java", dummyValue);
        content2.put("a/1.java", dummyValue);
        content2.put("b/", dummyValue);
        String html = DiffToHtmlUtils.generateDiffHTML(build1, build2, diffType, content1, content2, iconLocations);
        assertTrue(matchAndFind(generateUniquePattern(build1, "a/"), html));
        assertTrue(matchAndFind(generateSharePattern(build1, build2, "a/1.java"), html));
        assertTrue(matchAndFind(generateUniquePattern(build2, "b/"), html));
    }
}
