package com.infinitehorizons.models;

import com.infinitehorizons.utils.Regex;
import lombok.*;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a software version following the semantic versioning convention.
 * <p>
 * A version is represented by a major, minor, and patch number, optionally followed by a build identifier.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Version implements Comparable<Version> {

    private static final Pattern VERSION_REGEX = Pattern.compile("^([0-9]+)(?:\\.([0-9]+))?(?:\\.([0-9]+))?(?:_(.+))?$");

    private int major;
    private int minor;
    private int patch;
    private String build;

    /**
     * Creates a new {@code Version} instance with the specified major, minor, patch, and build components.
     *
     * @param major the major version number.
     * @param minor the minor version number.
     * @param patch the patch version number.
     * @param build the build identifier, which must be a positive integer or "dev".
     * @return a new {@code Version} instance.
     * @throws IllegalArgumentException if the build identifier is not a positive integer or "dev".
     */
    public static Version of(int major, int minor, int patch, String build) {
        if (build != null) {
            if (!Regex.POSITIVE_INTEGER_REGEX.matcher(build).matches() || !build.equalsIgnoreCase("dev")) {
                throw new IllegalArgumentException("Build is in the wrong format! Must be a positive integer or dev.");
            }
        }
        return new Version(Math.abs(major), Math.abs(minor), Math.abs(patch), build);
    }

    /**
     * Creates a new {@code Version} instance with the specified major, minor, and patch components.
     *
     * @param major the major version number.
     * @param minor the minor version number.
     * @param patch the patch version number.
     * @return a new {@code Version} instance.
     */
    public static Version of(int major, int minor, int patch) {
        return new Version(Math.abs(major), Math.abs(minor), Math.abs(patch), null);
    }

    /**
     * Creates a new {@code Version} instance with the specified major and minor components.
     *
     * @param major the major version number.
     * @param minor the minor version number.
     * @return a new {@code Version} instance.
     */
    public static Version of(int major, int minor) {
        return new Version(Math.abs(major), Math.abs(minor), 0, null);
    }

    /**
     * Creates a new {@code Version} instance with the specified major component.
     *
     * @param major the major version number.
     * @return a new {@code Version} instance.
     */
    public static Version of(int major) {
        return new Version(Math.abs(major), 0, 0, null);
    }

    /**
     * Parses a version string into a {@code Version} instance.
     *
     * @param versionToParse the version string to parse.
     * @return a new {@code Version} instance parsed from the string.
     * @throws IllegalArgumentException if the version string is in the wrong format.
     */
    public static Version parseVersion(String versionToParse) {
        Matcher matcher = VERSION_REGEX.matcher(versionToParse);
        if (matcher.matches()) {
            int major = matcher.group(1) == null ? 0 : Integer.parseInt(matcher.group(1));
            int minor = matcher.group(2) == null ? 0 : Integer.parseInt(matcher.group(2));
            int patch = matcher.group(3) == null ? 0 : Integer.parseInt(matcher.group(3));
            return new Version(major, minor, patch, matcher.group(4));
        } else {
            throw new IllegalArgumentException("Version is in the wrong format! Expected 1.2.3 Actual: " + versionToParse);
        }
    }

    /**
     * Returns a string representation of this version in the format "major.minor.patch_build".
     *
     * @return the string representation of this version.
     */
    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (build == null ? "" : "_" + build);
    }

    /**
     * Compares this version with the specified object for equality.
     *
     * @param obj the object to compare.
     * @return {@code true} if the specified object is equal to this version, {@code false} otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Version) {
            Version that = (Version) obj;
            return(this.major == that.major) &&
                    (this.minor == that.minor) &&
                    (this.patch == that.patch) &&
                    (Objects.equals(this.build, that.build));
        } else {
            return false;
        }
    }

    /**
     * Compares this version to another version.
     *
     * @param that the other version to compare to.
     * @return a negative integer, zero, or a positive integer as this version is less than, equal to, or greater than the specified version.
     */
    @Override
    public int compareTo(Version that) {

        if (this.major > that.major) return 1;
        if (this.major < that.major) return -1;

        if (this.minor > that.minor) return 1;
        if (this.minor < that.minor) return -1;

        if (this.patch > that.patch) return 1;
        if (this.patch < that.patch) return -1;

        if (this.build == null || that.build == null) {
            return 0;
        } else if (this.build.equalsIgnoreCase("dev")) {
            return 1;
        } else if (Regex.POSITIVE_INTEGER_REGEX.matcher(this.build).matches() &&
                Regex.POSITIVE_INTEGER_REGEX.matcher(that.build).matches()) {
            return this.build.compareTo(that.build);
        }

        return 0;
    }

    /**
     * Returns the hash code for this version.
     *
     * @return the hash code value for this version.
     */
    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, build);
    }
}
