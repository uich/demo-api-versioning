package jp.uich.web.mvc;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;

import jp.uich.Version;
import jp.uich.web.controler.annotation.ApiVersion;

public class VersionedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

  @Override
  protected RequestCondition<?> getCustomMethodCondition(Method method) {
    if (method.isAnnotationPresent(ApiVersion.class)) {
      return this.createApiVersionCondition(AnnotationUtils.findAnnotation(method, ApiVersion.class));
    }

    return null;
  }

  @Override
  protected RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
    RequestMappingInfo info = super.getMappingForMethod(method, handlerType);

    if (AnnotationUtils.findAnnotation(method, ApiVersion.class) != null) {
      RequestCondition<?> methodCondition = this.getCustomMethodCondition(method);
      info = this.createApiVersionInfo(methodCondition).combine(info);
    }

    return info;
  }

  private RequestMappingInfo createApiVersionInfo(RequestCondition<?> customCondition) {
    String pathPattern = "{version:\\d+\\.\\d+}";

    PatternsRequestCondition patternCondtion = new PatternsRequestCondition(new String[] { pathPattern },
      this.getUrlPathHelper(), this.getPathMatcher(), this.useSuffixPatternMatch(), this.useTrailingSlashMatch(),
      this.getFileExtensions());

    return new RequestMappingInfo(patternCondtion, null, null, null, null, null, customCondition);
  }

  private ApiVersionRequestCondition createApiVersionCondition(@Nonnull ApiVersion apiVersion) {
    return new ApiVersionRequestCondition(this.createVersionRangeSet(apiVersion));
  }

  private Set<Range<Version>> createVersionRangeSet(@Nonnull ApiVersion apiVersion) {
    final String[] supported = apiVersion.value();

    if (ArrayUtils.isNotEmpty(supported)) {
      return Arrays.stream(supported)
        .map(Version::parse)
        .map(Range::singleton)
        .collect(Collectors.toSet());
    }

    final String atLeast = apiVersion.atLeast();
    final String atMost = apiVersion.atMost();
    final String lessThan = apiVersion.lessThan();
    final String greaterThan = apiVersion.greaterThan();

    final BoundType lowerBoundType = StringUtils.isNotBlank(atMost)
      ? BoundType.CLOSED
      : StringUtils.isNotBlank(greaterThan)
        ? BoundType.OPEN
        : null;
    final BoundType upperBoundType = StringUtils.isNotBlank(atLeast)
      ? BoundType.CLOSED
      : StringUtils.isNotBlank(lessThan)
        ? BoundType.OPEN
        : null;
    final String lowerVersion = StringUtils.isNotBlank(atMost)
      ? atMost
      : StringUtils.isNotBlank(greaterThan)
        ? greaterThan
        : null;
    final String upperVersion = StringUtils.isNotBlank(atLeast)
      ? atLeast
      : StringUtils.isNotBlank(lessThan)
        ? lessThan
        : null;

    final Range<Version> versionRange = (StringUtils.isNotBlank(lowerVersion) && StringUtils.isNotBlank(upperVersion))
      ? Range.range(Version.parse(lowerVersion), lowerBoundType, Version.parse(upperVersion), upperBoundType)
      : StringUtils.isNotBlank(lowerVersion)
        ? Range.downTo(Version.parse(lowerVersion), lowerBoundType)
        : StringUtils.isNotBlank(upperVersion)
          ? Range.upTo(Version.parse(upperVersion), upperBoundType)
          : null;

    if (versionRange == null) {
      return Collections.emptySet();
    }

    return Collections.singleton(versionRange);
  }

}
