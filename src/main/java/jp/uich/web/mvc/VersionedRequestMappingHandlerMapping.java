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
import org.springframework.web.bind.annotation.RequestMapping;
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
  protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
    if (handlerType.isAnnotationPresent(ApiVersion.class)) {
      return this.createApiVersionCondition(AnnotationUtils.findAnnotation(handlerType, ApiVersion.class));
    }

    return null;
  }

  @Override
  protected RequestMappingInfo createRequestMappingInfo(RequestMapping requestMapping,
    RequestCondition<?> customCondition) {
    if (customCondition instanceof ApiVersionRequestCondition) {
      return RequestMappingInfo.paths(Arrays.stream(requestMapping.path())
        .map(path -> "/{version:\\d+\\.\\d+}/" + StringUtils.removeStart(path, "/"))
        .toArray(String[]::new))
        .methods(requestMapping.method())
        .params(requestMapping.params())
        .headers(requestMapping.headers())
        .consumes(requestMapping.consumes())
        .produces(requestMapping.produces())
        .mappingName(requestMapping.name())
        .customCondition(customCondition)
        .build();
    }

    return super.createRequestMappingInfo(requestMapping, customCondition);
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
