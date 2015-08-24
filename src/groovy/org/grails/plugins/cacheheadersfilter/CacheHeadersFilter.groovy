package org.grails.plugins.cacheheadersfilter

import com.grailsrocks.cacheheaders.CacheHeadersService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.plugins.cacheheadersfilter.matcher.CacheHeadersUrlMatcher
import org.springframework.web.filter.OncePerRequestFilter

import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Slf4j("LOGGER")
@CompileStatic
class CacheHeadersFilter extends OncePerRequestFilter {
  CacheHeadersService cacheHeadersService
  List<CacheHeadersUrlMatcher> matcherList = []

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
