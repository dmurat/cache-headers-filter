package org.grails.plugins.cacheheadersfilter

import groovy.util.logging.Slf4j
import org.codehaus.groovy.grails.commons.GrailsApplication

@Slf4j("LOGGER")
class PluginSetupHelper {
  static void updateWebXml(webXml, GrailsApplication application) {
    if (!isPluginEnabled(application.config)) {
      return
    }

    def contextParam = webXml."context-param"
    contextParam[contextParam.size() - 1] + {
      filter {
        "filter-name"("CacheHeadersFilter")
        "filter-class"(CacheHeadersFilter.name)
      }
    }

    def filter = webXml.filter
    filter[filter.size() - 1] + {
      "filter-mapping" {
        "filter-name"("CacheHeadersFilter")
        "url-pattern"(getFilterMappingUrlPattern(application.config))
      }
    }
  }

  static Boolean isPluginEnabled(ConfigObject config) {
    def enabled = config.grails.plugins.cacheHeadersFilter.enabled
    enabled = enabled != false && enabled != true ? true : enabled
    LOGGER.info("Filter enabled: ${enabled}")
    return enabled
  }

  static String getFilterMappingUrlPattern(ConfigObject config) {
    def urlPattern = config.grails.plugins.cacheHeadersFilter.filterMappingUrlPattern
    urlPattern = urlPattern ?: "/*"
    LOGGER.info("filter-mapping url-pattern: '${urlPattern}'")
    return urlPattern
  }
}
