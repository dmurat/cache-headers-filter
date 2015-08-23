package org.grails.plugins.cacheheadersfilter

import com.grailsrocks.cacheheaders.CacheHeadersService
import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.grails.plugins.cacheheadersfilter.matcher.AntCacheHeadersUrlMatcher
import org.grails.plugins.cacheheadersfilter.matcher.CacheHeadersUrlMatcher
import org.grails.plugins.cacheheadersfilter.matcher.RegexCacheHeadersUrlMatcher
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j("LOGGER")
class CacheHeadersFilter extends OncePerRequestFilter {
  CacheHeadersService cacheHeadersService
  List<CacheHeadersUrlMatcher> matcherList = []

  @Override
  protected void initFilterBean() throws ServletException {
    cacheHeadersService = fetchCacheHeadersService()

    List mappingList = fetchMappingList()
    LOGGER.debug("Mapping list read from grails config: ${mappingList.inspect()}")

    matcherList = configureMatcherList(mappingList)
    LOGGER.debug("MatcherList configured: ${matcherList}")
  }

  static GrailsApplication fetchGrailsApplication() {
    return Holders.grailsApplication
  }

  static CacheHeadersService fetchCacheHeadersService() {
    return fetchGrailsApplication().mainContext.getBean("cacheHeadersService", CacheHeadersService)
  }

  static List fetchMappingList() {
    return fetchGrailsApplication().config.grails.plugins.cacheHeadersFilter.mappingList ?: []
  }

  static List<CacheHeadersUrlMatcher> configureMatcherList(List mappingList) {
    List<CacheHeadersUrlMatcher> matcherListLocal = []

    if (!mappingList) {
      return matcherListLocal
    }

    mappingList.each { Map mapping ->
      String matcherType = mapping.type?.toLowerCase() ?: "ant"
      List pathList = mapping.pathList ?: []
      String cacheHeadersPreset = mapping.cacheHeadersPreset

      if (!pathList || !cacheHeadersPreset) {
        LOGGER.warn("Insufficient data for creating matcher: [type: ${matcherType}, cacheHeadersPreset: ${cacheHeadersPreset}, pathList: ${pathList.inspect()}]")
        return // continue with next iteration
      }

      pathList.each { String path ->
        switch (matcherType) {
          case ["re", "regex"]:
            matcherListLocal << new RegexCacheHeadersUrlMatcher(path, cacheHeadersPreset)
            break
          default:
            matcherListLocal << new AntCacheHeadersUrlMatcher(path, cacheHeadersPreset)
            break
        }
      }
    }

    return matcherListLocal
  }

  @CompileStatic
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    String servletPath = request.getServletPath()
    CacheHeadersUrlMatcher matchingMatcher = matcherList.find { CacheHeadersUrlMatcher matcher ->
      String urlTryingToMatch = servletPath
      matcher.pathMatchesUrl(urlTryingToMatch)
    }

    if (matchingMatcher) {
      LOGGER.debug("Selected matcher: ${matchingMatcher}")

      String cacheHeadersPreset = matchingMatcher.cacheHeadersPreset
      LOGGER.debug("Setting cache headers for response: [request path: ${servletPath}${request.getQueryString() ? "?" + request.getQueryString() : ""}, cacheHeadersPreset: ${cacheHeadersPreset}]")
      cacheHeadersService.cache(response, cacheHeadersPreset)
    }
    else {
      LOGGER.debug("There is no matcher found: [request path: ${servletPath}${request.getQueryString() ? "?" + request.getQueryString() : ""}]")
    }

    filterChain.doFilter(request, response);
  }
}
