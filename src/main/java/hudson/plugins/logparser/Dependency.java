package hudson.plugins.logparser;

/**
 * Dependency represents a dependency in POM.xml for Maven.
 */
public class Dependency {
    private final String groupId;
    private final String artifactId;
    private final String version;

    /**
     * @param groupId
     *            the group id for the dependency
     * @param artifactId
     *            the artifact id for the dependency
     * @param version
     *            the version for the dependency
     */
    public Dependency(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }

    /**
     * @return the group id for the dependency
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @return the artifact id for the dependency
     */
    public String getArtifactId() {
        return artifactId;
    }

    /**
     * @return the version for the dependency
     */
    public String getVersion() {
        return version;
    }
}
