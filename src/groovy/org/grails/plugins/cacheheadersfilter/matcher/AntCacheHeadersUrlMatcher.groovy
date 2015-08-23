package org.grails.plugins.cacheheadersfilter.matcher

import groovy.transform.CompileStatic
import org.springframework.util.AntPathMatcher
import org.springframework.util.PathMatcher

/**
 * Ant matcher based strategy for URL matching.
 *
 * <p>
 *   If the path consists of the pattern {@code /**} or {@code **}, it is treated as a universal match, which wil match any URL. For all other cases, Spring's {@code AntPathMatcher} is used to
 *   perform the check for a match. See the Spring documentation for this class for more information on the syntax details.
 * </p>
 * <p>
 *   Taken from spring-security-web module's org.springframework.security.web.util.AntUrlPathMatcher and adopted.
 * </p>
 */
@CompileStatic
class AntCacheHeadersUrlMatcher implements CacheHeadersUrlMatcher {
  private Boolean requiresLowerCaseUrl
  private String path
  private String cacheHeadersPreset

  private PathMatcher pathMatcher

  AntCacheHeadersUrlMatcher(String pathParam, String cacheHeadersPresetParam, Boolean requiresLowerCaseUrlParam = true) {
    this.pathMatcher = new AntPathMatcher()

    requiresLowerCaseUrl = requiresLowerCaseUrlParam
    path = this.compile(pathParam)
    cacheHeadersPreset = cacheHeadersPresetParam
  }

  private Object compile(String urlPattern) {
    if (requiresLowerCaseUrl) {
      return urlPattern.toLowerCase()
    }

    return urlPattern
  }

  @Override
  Boolean pathMatchesUrl(String url) throws IllegalStateException {
    if ("/**" == path || "**" == path) {
      return true
    }

    return pathMatcher.match(path, requiresLowerCaseUrl ? url.toLowerCase() : url)
  }

  @Override
  String getCacheHeadersPreset() {
    return cacheHeadersPreset
  }

  public String toString() {
    return getClass().getName() + "[path=${path}, cacheHeadersPreset=${cacheHeadersPreset}, requiresLowerCase=${requiresLowerCaseUrl}]";
  }
}
