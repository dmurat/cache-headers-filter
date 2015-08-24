package org.grails.plugins.cacheheadersfilter.matcher

import spock.lang.Specification
import spock.lang.Unroll

class AntCacheHeadersUrlMatcherSpecification extends Specification {
  @Unroll("should match '#pathExpression' path expression with '#url' url")
  def "should match"(String pathExpression, String url) {
    given:
    AntCacheHeadersUrlMatcher antCacheHeadersUrlMatcher = new AntCacheHeadersUrlMatcher(pathExpression, "someCacheHeadersPreset")

    when:
    Boolean matches = antCacheHeadersUrlMatcher.pathMatchesUrl(url)

    then:
    matches

    where:
    pathExpression        | url
    "/**"                 | "/some/long/url/with/resource.js"
    "**"                  | "/some/long/url/with/resource.js"
    "/some/path/**"       | "/some/path/with/resource.css"
    "/*"                  | "/somePath"
    "/some/path/*"        | "/some/path/resource.css"
    "/some/path/*/*"      | "/some/path/with/resource.css"
    "/some/path/**/*"     | "/some/path/with/resource.css"
    "/some/path/**/*"     | "/some/path/with/some/precious/resource.css"
    "/some/path/**/*.css" | "/some/path/with/some/very/precious/resource.css"
  }

  @Unroll("should not match '#pathExpression' path expression with '#url' url")
  def "should not match"(String pathExpression, String url) {
    given:
    AntCacheHeadersUrlMatcher antCacheHeadersUrlMatcher = new AntCacheHeadersUrlMatcher(pathExpression, "someCacheHeadersPreset")

    when:
    Boolean matches = antCacheHeadersUrlMatcher.pathMatchesUrl(url)

    then:
    !matches

    where:
    pathExpression    | url
    "/*"              | "/some/long/url/with/resource.js"
    "/some/path/*"    | "/some/path/with/resource.css"
    "/some/path/*"    | "/some/otherPath/resource.css"
    "/some/path/*"    | "/otherSome/otherPath/resource.css"
    "/some/path/*"    | "/some/path/with/resource.css"
    "/some/path/*.js" | "/some/path/resource.css"
  }
}
