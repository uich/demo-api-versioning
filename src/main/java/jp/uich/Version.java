package jp.uich;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import lombok.Data;

@Data
public class Version implements Comparable<Version> {

  private static final String DELIMITER = ".";
  private static final Joiner JOINER = Joiner.on(DELIMITER).skipNulls();
  private static final Splitter SPLITTER = Splitter.on(DELIMITER).trimResults().omitEmptyStrings();

  private final String version;
  private final int major;
  private final int minor;
  private final int revision;

  public Version(@Nonnull Integer major, @Nullable Integer minor, @Nullable Integer revision) {
    this.major = major;
    this.minor = minor != null ? minor : 0;
    this.revision = revision != null ? revision : 0;
    this.version = JOINER.join(Arrays.asList(major, minor, revision));
  }

  public static Version parse(@Nonnull String version) {
    Assert.isTrue(StringUtils.isNotBlank(version), "Version should not be blank.");
    var split = SPLITTER.splitToList(version);

    Assert.isTrue(split.stream().allMatch(StringUtils::isNumeric), "Version should be numeric.");
    Assert.isTrue(split.size() > 0, "Version should have major version number.");

    var major = Integer.valueOf(split.get(0));
    var minor = split.size() > 1 ? Integer.valueOf(split.get(1)) : null;
    var revision = split.size() > 2 ? Integer.valueOf(split.get(2)) : null;

    return new Version(major, minor, revision);
  }

  @Override
  public int compareTo(@Nonnull Version other) {
    int diff;
    diff = this.major - other.major;
    if (diff == 0) {
      diff = this.minor - other.minor;
      if (diff == 0) {
        diff = this.revision - other.revision;
      }
    }
    return diff;
  }

  public int compareTo(@Nonnull String version) {
    return this.compareTo(Version.parse(version));
  }

  @Override
  public String toString() {
    return this.version;
  }
}
