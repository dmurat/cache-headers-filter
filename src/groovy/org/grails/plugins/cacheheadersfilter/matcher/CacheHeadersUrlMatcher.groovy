package org.grails.plugins.cacheheadersfilter.matcher

import groovy.transform.CompileStatic

/**
 * Strategy interface for deciding weather configured path matches against candidate URL.
 */
@CompileStatic
interface CacheHeadersUrlMatcher {
  /**
   * Carries on actual matching against stored compiled url pattern.
   *
   * @param url Candidate URL to be compared with stored compiled URL pattern.
   * @return true if there is a match, false otherwise.
   * @throws IllegalStateException if there is no stored compiled url pattern.
   */
  Boolean pathMatchesUrl(String url) throws IllegalStateException

  /**
   * Returns stored cache headers preset.
   */
  String getCacheHeadersPreset()
}
