package org.grails.plugins.cacheheadersfilter

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PluginSetupHelper {
  static final Logger LOGGER = LoggerFactory.getLogger(PluginSetupHelper);

  static void updateWebXml(webXml, GrailsApplication application) {
    if (!isPluginEnabled(application)) {
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
        "filter-name"('CacheHeadersFilter')
        "url-pattern"("/*")
      }
    }
  }

  private static boolean isPluginEnabled(GrailsApplication application) {
    def enabled = application.config.grails.plugins.cacheHeadersFilter.enabled
    return enabled == null || enabled != false
  }
}
