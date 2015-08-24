package org.grails.plugins.cacheheadersfilter

import groovy.xml.StreamingMarkupBuilder
import groovy.xml.XmlUtil
import org.grails.plugins.cacheheadersfilter.matcher.AntCacheHeadersUrlMatcher
import org.grails.plugins.cacheheadersfilter.matcher.CacheHeadersUrlMatcher
import org.grails.plugins.cacheheadersfilter.matcher.RegexCacheHeadersUrlMatcher
import spock.lang.Specification

class PluginSetupHelperSpecification extends Specification {
  static final String WEB_XML = """\
      <?xml version="1.0" encoding="UTF-8"?>
      <web-app version="3.0" metadata-complete="true"
               xmlns="http://java.sun.com/xml/ns/javaee"
               xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
               xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_3_0.xsd">

        <display-name>some display name</display-name>

        <context-param>
          <param-name>contextConfigLocation</param-name>
          <param-value>/WEB-INF/applicationContext.xml</param-value>
        </context-param>

        <filter>
          <filter-name>charEncodingFilter</filter-name>
          <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
          <init-param>
            <param-name>targetBeanName</param-name>
            <param-value>characterEncodingFilter</param-value>
          </init-param>
          <init-param>
            <param-name>targetFilterLifecycle</param-name>
            <param-value>true</param-value>
          </init-param>
        </filter>

        <filter-mapping>
          <filter-name>charEncodingFilter</filter-name>
          <url-pattern>/*</url-pattern>
        </filter-mapping>

        <servlet>
          <servlet-name>grails</servlet-name>
          <servlet-class>org.codehaus.groovy.grails.web.servlet.GrailsDispatcherServlet</servlet-class>
        </servlet>
      </web-app>""".stripIndent()

  static String serializeWebXml(xml) {
    def outputBuilder = new StreamingMarkupBuilder()

    Writable outputXml = outputBuilder.bind {
      mkp.declareNamespace('': 'http://java.sun.com/xml/ns/javaee')
      mkp.yield xml
    } as Writable

    return XmlUtil.serialize(outputXml)
  }

  def "should update web.xml when no config is present since plugin should be enabled by default"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("")
    def webXml = new XmlSlurper().parseText(WEB_XML)

    when:
    PluginSetupHelper.updateWebXml(webXml, configObject)

    then:
    // With XmlSlurper, after modifying nodes, we need to parse document again in order to find created or modified nodes.
    def webXmlNodeResult = new XmlSlurper().parseText(serializeWebXml(webXml))
    //noinspection GroovyAssignabilityCheck
    def cacheHeadersFilterNode = webXmlNodeResult.filter[0]
    //noinspection GroovyAssignabilityCheck
    def cacheHeadersFilterMappingNode = webXmlNodeResult."filter-mapping"[0]

    cacheHeadersFilterNode."filter-name".text() == "cacheHeadersFilter"
    cacheHeadersFilterNode."filter-class".text() == "org.springframework.web.filter.DelegatingFilterProxy"

    cacheHeadersFilterMappingNode."filter-name".text() == "cacheHeadersFilter"
  }

  def "should not update web.xml when plugin is not enabled"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("grails.plugins.cacheHeadersFilter.enabled = false")
    def webXml = new XmlSlurper().parseText(WEB_XML)

    when:
    PluginSetupHelper.updateWebXml(webXml, configObject)

    then:
    def webXmlResult = serializeWebXml(webXml)
    !webXmlResult.contains("CacheHeadersFilter")
  }

  def "should setup web.xml with default filter mapping"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("")
    def webXml = new XmlSlurper().parseText(WEB_XML)

    when:
    PluginSetupHelper.updateWebXml(webXml, configObject)

    then:
    // With XmlSlurper, after modifying nodes, we need to parse document again in order to find created or modified nodes.
    def webXmlNodeResult = new XmlSlurper().parseText(serializeWebXml(webXml))
    //noinspection GroovyAssignabilityCheck
    def cacheHeadersFilterMappingNode = webXmlNodeResult."filter-mapping"[0]

    cacheHeadersFilterMappingNode."url-pattern".text() == "/*"
  }

  def "should setup web.xml with configured filter mapping"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("grails.plugins.cacheHeadersFilter.filterMappingUrlPattern = '/someMapping/*'")
    def webXml = new XmlSlurper().parseText(WEB_XML)

    when:
    PluginSetupHelper.updateWebXml(webXml, configObject)

    then:
    // With XmlSlurper, after modifying nodes, we need to parse document again in order to find created or modified nodes.
    def webXmlNodeResult = new XmlSlurper().parseText(serializeWebXml(webXml))
    //noinspection GroovyAssignabilityCheck
    def cacheHeadersFilterMappingNode = webXmlNodeResult."filter-mapping"[0]

    cacheHeadersFilterMappingNode."url-pattern".text() == "/someMapping/*"
  }

  def "should be enabled by default"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("")

    when:
    Boolean isEnabled = PluginSetupHelper.isPluginEnabled(configObject)

    then:
    isEnabled
  }

  def "should be enabled when configured so"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("grails.plugins.cacheHeadersFilter.enabled = true")

    when:
    Boolean isEnabled = PluginSetupHelper.isPluginEnabled(configObject)

    then:
    isEnabled
  }

  def "should be disabled when configured so"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("grails.plugins.cacheHeadersFilter.enabled = false")

    when:
    Boolean isEnabled = PluginSetupHelper.isPluginEnabled(configObject)

    then:
    !isEnabled
  }

  def "should give universal filter mapping by default"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("")

    when:
    String filterMapping = PluginSetupHelper.getFilterMappingUrlPattern(configObject)

    then:
    filterMapping == "/*"
  }

  def "should give configured filter mapping"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("grails.plugins.cacheHeadersFilter.filterMappingUrlPattern = '/someMapping/*'")

    when:
    String filterMapping = PluginSetupHelper.getFilterMappingUrlPattern(configObject)

    then:
    filterMapping == "/someMapping/*"
  }

  def "should construct empty mather list by default"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("")

    when:
    List<CacheHeadersUrlMatcher> matcherList = PluginSetupHelper.constructMatcherList(configObject)

    then:
    matcherList.isEmpty()
  }

  def "should construct expected matcher list for configuration"() {
    given:
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
        ]""")

    when:
    List<CacheHeadersUrlMatcher> matcherList = PluginSetupHelper.constructMatcherList(configObject)

    then:
    matcherList.size() == 4
    matcherList.findAll({it instanceof AntCacheHeadersUrlMatcher }).size() == 3
    matcherList.find { it instanceof RegexCacheHeadersUrlMatcher }
  }

  def "should not construct matcher when cacheHeadersPreset is missing in configuration"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("""
        grails.plugins.cacheHeadersFilter.mappingList = [
            [
                type: "ant",
                pathList: [
                    "/senchaWorkspace/build/production/*/*-classic-*.json",
                    "/senchaWorkspace/build/production/*/*-modern-*.json",
                    "/senchaWorkspace/build/production/*/bootstrap.js"
                ]
            ]
        ]""")

    when:
    List<CacheHeadersUrlMatcher> matcherList = PluginSetupHelper.constructMatcherList(configObject)

    then:
    matcherList.size() == 0
  }

  def "should not construct matcher when pathList is missing in configuration"() {
    given:
    ConfigObject configObject = new ConfigSlurper().parse("""
        grails.plugins.cacheHeadersFilter.mappingList = [
            [
                cacheHeadersPreset: "noCache",
                type: "ant",
                pathList: [
                ]
            ],
            [
                cacheHeadersPreset: "noCache",
                type: "ant",
            ],
        ]""")

    when:
    List<CacheHeadersUrlMatcher> matcherList = PluginSetupHelper.constructMatcherList(configObject)

    then:
    matcherList.size() == 0
  }
}
