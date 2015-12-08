package hudson.plugins.logparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DiffToHtmlUtils is used to generate HTML for diff.
 */
public class DiffToHtmlUtils {
    /**
     * A wrapper for difference between two lists of strings It contains the
     * strings that the two lists share, and strings that two lists have in
     * unique
     */
    private static class Diff {
        public List<String> intersections;
        public List<String> list1Uniques;
        public List<String> list2Uniques;
    }

    /**
     * Compute the difference between two list of strings
     * 
     * @param list1
     *            the first list
     * @param list
     *            the second list
     * @return the difference between those two lists in a Diff wrapper object
     */
    private static Diff diffStringLists(List<String> list1, List<String> list2) {
        Diff diff = new Diff();

        // Compute intersection
        List<String> intersections = new ArrayList<>(list1);
        intersections.retainAll(list2);

        // Compute content 1 unique
        List<String> list1Uniques = new ArrayList<>(list1);
        list1Uniques.removeAll(list2);

        // Compute content 2 unique
        List<String> list2Uniques = new ArrayList<>(list2);
        list2Uniques.removeAll(list1);

        diff.intersections = intersections;
        diff.list1Uniques = list1Uniques;
        diff.list2Uniques = list2Uniques;

        return diff;
    }

    private static String generateBody(int build1, int build2, String diffType,
            Map<String, List<String>> content1, Map<String, List<String>> content2,
            Map<String, String> iconLocations) {
        // Keys
        List<String> content1Keys = new ArrayList<>(content1.keySet());
        List<String> content2Keys = new ArrayList<>(content2.keySet());

        Diff diff = diffStringLists(content1Keys, content2Keys);

        StringBuilder sb = new StringBuilder();
        sb.append("<body>\n");
        sb.append(generateBodyTitle(build1, build2, diffType));
        for (String unique : diff.list1Uniques) {
            sb.append(generateUniqueItem(build1, unique));
        }
        for (String unique : diff.list2Uniques) {
            sb.append(generateUniqueItem(build2, unique));
        }
        for (String common : diff.intersections) {
            sb.append(generateCommonItem(build1, build2, common, content1.get(common),
                    content2.get(common)));
        }
        sb.append("</body>\n");
        return sb.toString();
    }

    private static String generateBodyTitle(int build1, int build2, String diffType) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        sb.append(String.format("<font size = \"5\">%s diff between build %s and build %s</font>",
                diffType, build1, build2));
        sb.append("</div>");
        sb.append("<br>");
        return sb.toString();
    }

    private static String generateUniqueItem(int build, String key) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        sb.append("build " + build + " unique: " + key);
        sb.append("</div>");
        return sb.toString();
    }

    private static String generateCommonItem(int build1, int build2, String key,
            List<String> content1, List<String> content2) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        sb.append("build " + build1 + " and build " + build2 + " share " + key);
        sb.append("</div>");
        sb.append(new DiffToHtmlGenerator(content1, content2).generateHtmlString(true));
        return sb.toString();
    }

    private static String generateHead() {
        StringBuilder sb = new StringBuilder();
        sb.append("<head>\n");
        sb.append("<meta charset='utf-8'>\n");
        // CSS file can not be served in this way
        // sb.append("<link rel='stylesheet' type='text/css'
        // href='style.css'>\n");
        sb.append("<style>\n");
        sb.append(new DiffToHtmlGenerator().generateCSS());
        sb.append("</style>\n");
        sb.append("</head>\n");
        return sb.toString();
    }

    /**
     * Generate the HTML for diff.
     *
     * @param build1
     *            the build number of the left side
     * @param build2
     *            the build number of the right side
     * @param diffType
     *            the type of diff
     * @param content1
     *            the content for the left side build
     * @param content2
     *            the content for the right side build
     * @param iconLocations
     *            the locations for icons for sections, if any
     * @return the HTML for the diff as a String
     */
    public static String generateDiffHTML(int build1, int build2, String diffType,
            Map<String, List<String>> content1, Map<String, List<String>> content2,
            Map<String, String> iconLocations) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!doctype html>\n");
        sb.append("<html lang='en'>\n");
        sb.append(generateHead());
        sb.append(generateBody(build1, build2, diffType, content1, content2, iconLocations));
        sb.append("</html>\n");
        return sb.toString();
    }

    /**
     * To generate the button tag in HTML
     * 
     * @param buttonId
     *            the id of the button
     * @param text
     *            the text information of button
     * @return String of the button tag
     */
    public static String generateButton(String buttonId, String text) {
        String buttonString = "<button id ='" + buttonId + "'>" + text + "</button>\n";
        return buttonString;
    }

    /**
     * To generate the JavaScript library information
     * 
     * @return String of JavaScript library information
     */
    public static String generateJSLib() {
        String jslib = "<script src='https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js'></script>\n";
        return jslib;
    }

    /**
     * To generate the JavaScript String defining the function of buttons
     * 
     * @param buttonId
     *            the id of the button
     * @param closingItem
     *            the name of the item to be closed
     * @param itemType
     *            the type of item, "" for tag, "." for class and "#" for id
     * @return String of JavaScript defining the function of buttons
     */
    public static String generateJS(String buttonId, String closingItem, String itemType) {
        String js = "<script>\n" + "jQuery(function ($){\n" + "$(document).ready(function(){\n"
                + "$('#" + buttonId + "').click(function(){\n" + "$('" + itemType + closingItem
                + "').toggle();\n" + "});\n" + "});\n" + "});\n" + "</script>\n";
        return js;
    }
}
