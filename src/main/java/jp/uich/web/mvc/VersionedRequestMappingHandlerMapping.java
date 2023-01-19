package jp.uich.web.mvc;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import jp.uich.Version;
import jp.uich.web.controler.annotation.ApiVersion;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class VersionedRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

  @Override
  protected RequestCondition<?> getCustomMethodCondition(Method method) {
    if (method.isAnnotationPresent(ApiVersion.class)) {
      var annotation = AnnotationUtils.findAnnotation(method, ApiVersion.class);
      return this.createApiVersionCondition(Objects.requireNonNull(annotation));
    }

    return null;
  }

  @Override
  protected RequestCondition<?> getCustomTypeCondition(Class<?> handlerType) {
    if (handlerType.isAnnotationPresent(ApiVersion.class)) {
      var annotation = AnnotationUtils.findAnnotation(handlerType, ApiVersion.class);
      return this.createApiVersionCondition(Objects.requireNonNull(annotation));
    }

    return null;
  }

  @Override
  protected RequestMappingInfo createRequestMappingInfo(RequestMapping requestMapping,
                                                        @Nullable RequestCondition<?> customCondition) {
    if (customCondition instanceof ApiVersionRequestCondition) {
      var paths = Arrays.stream(requestMapping.path())
                        .map(path -> "/{version:\\d+\\.\\d+}/" + StringUtils.removeStart(path, "/"))
                        .toArray(String[]::new);
      return RequestMappingInfo.paths(paths)
                               .methods(requestMapping.method())
                               .params(requestMapping.params())
                               .headers(requestMapping.headers())
                               .consumes(requestMapping.consumes())
                               .produces(requestMapping.produces())
                               .mappingName(requestMapping.name())
                               .options(getBuilderConfiguration())
                               .customCondition(customCondition)
                               .build();
    }

    return super.createRequestMappingInfo(requestMapping, customCondition);
  }

  private ApiVersionRequestCondition createApiVersionCondition(ApiVersion apiVersion) {
    return new ApiVersionRequestCondition(this.createVersionRangeSet(apiVersion));
  }

  private Set<Range<Version>> createVersionRangeSet(ApiVersion apiVersion) {
    var supported = apiVersion.value();

    if (ArrayUtils.isNotEmpty(supported)) {
      return Arrays.stream(supported)
                   .map(Version::parse)
                   .map(Range::singleton)
                   .collect(Collectors.toSet());
    }

    var atLeast = apiVersion.atLeast();
    var atMost = apiVersion.atMost();
    var lessThan = apiVersion.lessThan();
    var greaterThan = apiVersion.greaterThan();

    var lowerBoundType = StringUtils.isNotBlank(atMost)
        ? BoundType.CLOSED
        : StringUtils.isNotBlank(greaterThan)
        ? BoundType.OPEN
        : null;
    var upperBoundType = StringUtils.isNotBlank(atLeast)
        ? BoundType.CLOSED
        : StringUtils.isNotBlank(lessThan)
        ? BoundType.OPEN
        : null;
    var lowerVersion = StringUtils.isNotBlank(atMost)
        ? atMost
        : StringUtils.isNotBlank(greaterThan)
        ? greaterThan
        : null;
    var upperVersion = StringUtils.isNotBlank(atLeast)
        ? atLeast
        : StringUtils.isNotBlank(lessThan)
        ? lessThan
        : null;

    var versionRange = (StringUtils.isNotBlank(lowerVersion) && StringUtils.isNotBlank(upperVersion))
        ? Range.range(Version.parse(lowerVersion), Objects.requireNonNull(lowerBoundType),
        Version.parse(upperVersion), Objects.requireNonNull(upperBoundType))
        : StringUtils.isNotBlank(lowerVersion)
        ? Range.downTo(Version.parse(lowerVersion), Objects.requireNonNull(lowerBoundType))
        : StringUtils.isNotBlank(upperVersion)
        ? Range.upTo(Version.parse(upperVersion), Objects.requireNonNull(upperBoundType))
        : null;

    if (versionRange == null) {
      return Collections.emptySet();
    }

    return Collections.singleton(versionRange);
  }

}
