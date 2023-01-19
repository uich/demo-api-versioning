package jp.uich.web.mvc;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import jakarta.servlet.http.HttpServletRequest;
import jp.uich.Version;
import lombok.RequiredArgsConstructor;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
public class ApiVersionRequestCondition implements RequestCondition<ApiVersionRequestCondition> {

  private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

  // マイナーバージョンまでしか対応しない
  private static final String VERSIONED_PATH_PATTERN = "/{version:\\d+\\.\\d+}/**";

  private final Set<Range<Version>> supported;

  @Override
  public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
    return new ApiVersionRequestCondition(ImmutableSet.<Range<Version>>builder()
                                                      .addAll(this.supported)
                                                      .addAll(other.supported)
                                                      .build());
  }

  @Override
  public ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {
    var apiVersion = getRequestApiVersion(request);
    if (apiVersion == null) {
      return null;
    }

    return this.supported.stream()
                         .anyMatch(range -> range.contains(apiVersion))
        ? this
        : null;
  }

  @Override
  public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
    return other.supported.size() - this.supported.size();
  }

  @Nullable
  private static Version getRequestApiVersion(HttpServletRequest request) {
    return Optional.of(PATH_MATCHER.extractUriTemplateVariables(VERSIONED_PATH_PATTERN, request.getRequestURI()))
                   .map(map -> map.get("version"))
                   .map(version -> {
                     try {
                       return Version.parse(version);
                     } catch (Exception e) {
                       return null;
                     }
                   })
                   .orElse(null);
  }

  @Override
  public String toString() {
    return "Version supported. [" + this.supported + "]";
  }
}
