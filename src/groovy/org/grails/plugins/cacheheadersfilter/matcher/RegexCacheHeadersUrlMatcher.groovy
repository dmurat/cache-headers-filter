package org.grails.plugins.cacheheadersfilter.matcher

import groovy.transform.CompileStatic

import java.util.regex.Pattern

/**
 * Regular expression matcher based strategy for URL matching.
 *
 * <p>
 *   Taken from spring-security-web module's org.springframework.security.web.util.RegexUrlPathMatcher and adopted.
 * </p>
 */
@CompileStatic
class RegexCacheHeadersUrlMatcher implements CacheHeadersUrlMatcher {
  private Pattern pathCompiled
  private String cacheHeadersPreset

  RegexCacheHeadersUrlMatcher(String path, String cacheHeadersPreset) {
    this.pathCompiled = compile(path)
    this.cacheHeadersPreset = cacheHeadersPreset
  }

  private static Pattern compile(String path) {
    return Pattern.compile(path)
  }

  @Override
  Boolean pathMatchesUrl(String url)  {
    return pathCompiled.matcher(url).matches()
  }

  @Override
  String getCacheHeadersPreset() {
    return cacheHeadersPreset
  }

  public String toString() {
    return getClass().getName() + "[path=${pathCompiled.pattern()}, cacheHeadersPreset=${cacheHeadersPreset}]"
  }
}
