class CacheHeadersFilterGrailsPlugin {
  def version = "0.0.1.BUILD-SNAPSHOT"
  def grailsVersion = "2.0 > *"
  def pluginExcludes = []

  def title = "Cache Headers Filter Plugin"
  def author = "Damir Murat"
  def authorEmail = "damir.murat@gmail.com"
  def description = '''\
Servlet filter for setting HTTP cache headers on HTTP responses. Plugin implementation is based on cache-headers plugin but it allows setting cache headers for any resource, not only for controller
responses.
'''

  def documentation = " https://github.com/dmurat/cache-headers-filter/blob/master/README.md"

  def license = "APACHE"
  def organization = [name: "CROZ d.o.o.", url: "http://www.croz.net/"]
  def issueManagement = [ system: "github", url: "https://github.com/dmurat/cache-headers-filter/issues" ]
  def scm = [ url: 'https://github.com/dmurat/cache-headers-filter' ]

  def doWithWebDescriptor = { webXml ->
  }
}
