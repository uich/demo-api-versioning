package jp.uich.web.mvc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.DelegatingWebMvcConfiguration;

import jp.uich.web.mvc.VersionedRequestMappingHandlerMapping;

@Configuration
public class WebMvcConfig extends DelegatingWebMvcConfiguration {

  @Override
  protected VersionedRequestMappingHandlerMapping createRequestMappingHandlerMapping() {
    return new VersionedRequestMappingHandlerMapping();
  }

}
