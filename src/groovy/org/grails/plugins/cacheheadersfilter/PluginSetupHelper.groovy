package org.grails.plugins.cacheheadersfilter

import groovy.util.logging.Slf4j
import org.grails.plugins.cacheheadersfilter.matcher.AntCacheHeadersUrlMatcher
import org.grails.plugins.cacheheadersfilter.matcher.CacheHeadersUrlMatcher
import org.grails.plugins.cacheheadersfilter.matcher.RegexCacheHeadersUrlMatcher
import org.springframework.web.filter.DelegatingFilterProxy

@Slf4j("LOGGER")
class PluginSetupHelper {
  static void updateWebXml(webXml, ConfigObject config) {
    if (!isPluginEnabled(config)) {
      LOGGER.info("Filter is NOT enabled")
      return
    }

    LOGGER.info("Filter is enabled")

    def contextParam = webXml."context-param"

    // We need to use spring's DelegatingFilterProxy, and the name of the filter matches the name of the Spring bean that it delegates to
    contextParam[contextParam.size() - 1] + {
      filter {
        "filter-name"("cacheHeadersFilter")
        "filter-class"(DelegatingFilterProxy.name)
      }
    }

    String filterMappingUrlPattern = getFilterMappingUrlPattern(config)
    def filter = webXml.filter
    filter[filter.size() - 1] + {
      "filter-mapping" {
        "filter-name"("cacheHeadersFilter")
        "url-pattern"(filterMappingUrlPattern)
      }
    }

    LOGGER.info("filter-mapping url-pattern: '${filterMappingUrlPattern}'")
  }

  static Boolean isPluginEnabled(ConfigObject config) {
    def enabled = config.grails.plugins.cacheHeadersFilter.enabled
    enabled = enabled != false && enabled != true ? true : enabled
    return enabled
  }

  static String getFilterMappingUrlPattern(ConfigObject config) {
    def urlPattern = config.grails.plugins.cacheHeadersFilter.filterMappingUrlPattern
    urlPattern = urlPattern ?: "/*"
    return urlPattern
  }

  static List<CacheHeadersUrlMatcher> constructMatcherList(ConfigObject config) {
    List mappingList = config.grails.plugins.cacheHeadersFilter.mappingList ?: []

    List<CacheHeadersUrlMatcher> matcherList = []

    if (!mappingList) {
      return matcherList
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
            matcherList << new RegexCacheHeadersUrlMatcher(path, cacheHeadersPreset)
            break
          default:
            matcherList << new AntCacheHeadersUrlMatcher(path, cacheHeadersPreset)
            break
        }
      }
    }

    LOGGER.debug("Matchers added: ${matcherList.inspect()}")
    return matcherList
  }
}
