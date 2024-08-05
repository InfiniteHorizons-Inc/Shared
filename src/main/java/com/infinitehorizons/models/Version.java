package com.infinitehorizons.models;

import com.infinitehorizons.utils.Regex;
import lombok.*;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static Version of(int major, int minor, int patch, String build) {
        if (build != null) {
            if (!Regex.POSITIVE_INTEGER_REGEX.matcher(build).matches() || !build.equalsIgnoreCase("dev")) {
                throw new IllegalArgumentException("Build is in the wrong format! Must be a positive integer or dev.");
            }
        }
        return new Version(Math.abs(major), Math.abs(minor), Math.abs(patch), build);
    }

    public static Version of(int major, int minor, int patch) {
        return new Version(Math.abs(major), Math.abs(minor), Math.abs(patch), null);
    }

    public static Version of(int major, int minor) {
        return new Version(Math.abs(major), Math.abs(minor), 0, null);
    }

    public static Version of(int major) {
        return new Version(Math.abs(major), 0, 0, null);
    }

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

    @Override
    public String toString() {
        return major + "." + minor + "." + patch + (build == null ? "" : "_" + build);
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(major, minor, patch, build);
    }
}
