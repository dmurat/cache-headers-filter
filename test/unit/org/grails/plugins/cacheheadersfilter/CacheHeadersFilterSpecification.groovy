package org.grails.plugins.cacheheadersfilter

import com.grailsrocks.cacheheaders.CacheHeadersService
import org.grails.plugins.cacheheadersfilter.matcher.CacheHeadersUrlMatcher
import org.springframework.mock.web.MockFilterChain
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import spock.lang.Specification

class CacheHeadersFilterSpecification extends Specification {
  CacheHeadersService cacheHeadersService
  List<CacheHeadersUrlMatcher> matcherList

  MockHttpServletRequest request
  MockHttpServletResponse response
  MockFilterChain filterChain

  def setup() {
    request = new MockHttpServletRequest()
    response = new MockHttpServletResponse()
    filterChain = new MockFilterChain()

    ConfigObject configObject = new ConfigSlurper().parse("""
        grails.plugins.cacheHeadersFilter.mappingList = [
            [
                cacheHeadersPreset: "noCache",
                type: "ant",
                pathList: [
                    "/senchaWorkspace/build/production/*/*-classic-*.json",
                    "/senchaWorkspace/build/production/*/*-modern-*.json",
                    "/senchaWorkspace/build/production/*/bootstrap.js"
                ]
            ],
            [
                cacheHeadersPreset: "cache6Months",
                type: "re",
                pathList: [
                    "^/senchaWorkspace/.*"
                ]
            ]
        ]

        cache.headers.presets = [
            "noCache": false,
            "cache6Months": [shared: true, validFor: 1 * 60 * 60 * 24 * 30 * 6]
        ]""")

    matcherList = PluginSetupHelper.constructMatcherList(configObject)

    Map presets = configObject.cache.headers.presets
    cacheHeadersService = new CacheHeadersService(presets: presets as Map)
  }

  def "should not modify non matching request"() {
    given:
    request.servletPath = "/some/path/to/resource.js"
    CacheHeadersFilter cacheHeadersFilter = new CacheHeadersFilter(matcherList: matcherList, cacheHeadersService: cacheHeadersService)

    when:
    cacheHeadersFilter.doFilterInternal(request, response, filterChain)

    then:
    response.headerNames == old(response.headerNames)
  }

  def "should add cache headers for matching request"() {
    given:
    def oldResponseHeadersSize = response.headerNames.size()
    request.servletPath = "/senchaWorkspace/build/production/something/something-classic-something.json"
    CacheHeadersFilter cacheHeadersFilter = new CacheHeadersFilter(matcherList: matcherList, cacheHeadersService: cacheHeadersService)

    when:
    cacheHeadersFilter.doFilterInternal(request, response, filterChain)

    then:
    response.headerNames.size() != oldResponseHeadersSize
  }

  def "added cache headers should have correct values for noCache preset"() {
    given:
    request.servletPath = "/senchaWorkspace/build/production/something/something-classic-something.json"
    CacheHeadersFilter cacheHeadersFilter = new CacheHeadersFilter(matcherList: matcherList, cacheHeadersService: cacheHeadersService)

    when:
    cacheHeadersFilter.doFilterInternal(request, response, filterChain)

    then:
    response.getHeaderValue("Cache-Control") == "no-cache, no-store"

    and:
    response.getHeaderValue("Expires")
    Long.valueOf(response.getHeaderValue("Expires") as String) < new Date().time

    and:
    response.getHeaderValue("Pragma") == "no-cache"
  }

  def "added cache headers should have correct values for cache6Months preset"() {
    given:
    request.servletPath = "/senchaWorkspace/something.json"
    CacheHeadersFilter cacheHeadersFilter = new CacheHeadersFilter(matcherList: matcherList, cacheHeadersService: cacheHeadersService)

    when:
    cacheHeadersFilter.doFilterInternal(request, response, filterChain)

    then:
    Long lastModifiedTime = Long.valueOf(response.getHeaderValue("Last-Modified") as String)
    Long yesterdayTime = (new Date() - 1).time
    Long currentTime = new Date().time

    response.getHeaderValue("Cache-Control")
    response.getHeaderValue("Cache-Control") == "public, s-maxage=15552000, max-age=15552000"

    and:
    response.getHeaderValue("Expires")
    Long.valueOf(response.getHeaderValue("Expires") as String) > (new Date() + 30 * 6 - 1).time

    and:
    response.getHeaderValue("Last-Modified")
    yesterdayTime < lastModifiedTime
    currentTime > lastModifiedTime
  }
}
