package org.grails.plugins.cacheheadersfilter.matcher

import spock.lang.Specification
import spock.lang.Unroll

class RegexCacheHeadersUrlMatcherSpecification extends Specification {
  @Unroll("should match '#pathExpression' path expression with '#url' url")
  def "should match"(String pathExpression, String url) {
    given:
    RegexCacheHeadersUrlMatcher regexCacheHeadersUrlMatcher = new RegexCacheHeadersUrlMatcher(pathExpression, "someCacheHeadersPreset")

    when:
    Boolean matches = regexCacheHeadersUrlMatcher.pathMatchesUrl(url)

    then:
    matches

    where:
    pathExpression          | url
    ".*"                    | "/some/long/url/with/resource.js"
    ".*"                    | "/some/completely/other/url/with/resource.js"
    "^/some/path/.*\\.js\$" | "/some/path/to/some/javascript/resource.js"
  }

  @Unroll("should not match '#pathExpression' path expression with '#url' url")
  def "should not match"(String pathExpression, String url) {
    given:
    RegexCacheHeadersUrlMatcher regexCacheHeadersUrlMatcher = new RegexCacheHeadersUrlMatcher(pathExpression, "someCacheHeadersPreset")

    when:
    Boolean matches = regexCacheHeadersUrlMatcher.pathMatchesUrl(url)

    then:
    !matches

    where:
    pathExpression        | url
    "^/some/path/.*\\.js" | "/some/path/with/resource.css"
    "^/some/path/.*\$"    | "/some/otherPath/resource.css"
  }
}
