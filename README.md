# cache-headers-filter
Grails plugin which brings in a servlet filter for setting HTTP cache headers on HTTP responses.

Plugin implementation is based on `cache-headers` plugin. Difference is that a servlet filter can be applied on any request, not only on controller outputs like
with `cache-headers` plugin.

## Introduction
When first needed caching filter, it was surprise for me that finding one is pretty hard. There are many related options like `cache-headers` plugin, `jawr`
(and corresponding grails plugin), `asset pipeline` etc. `Jawr` and `asset pipeline` do lot more than caching, but they are doing so for resources known at
compile or package time. On the other hand, `cache-headers` is applicable at runtime, but it can setup HTTP cache headers for controller outputs only.

I was in situation when I needed setting up caching headers at runtime, but not for controller outputs, but rather for requests that originated from a client
which was requesting resources only (JavaScript and CSS files). And that leads me to implementing this plugin.

`cache-headers-filter` plugin can work along with `cache-headers` (in fact, `cache-headers` is very strong dependency of `cache-headers-filter`
implementation), `jawr` and `asset pipeline`.

## Installation
Add the plugin as runtime dependency under plugins section in your `BuildConfig.groovy`:

    runtime ":cache-headers-filter:0.0.1"

## Usage
As said before, `cache-headers-filter` relies heavily on `cache-headers` plugin. More specifically, `cache-headers-filter` expects and relies on presets
configuration of `cache-header` plugin. For example, suppose that we have `cache-headers` presets configured this way in `Config.groovy`:

    cache.headers.presets = [
      "noCache": false,  // do not cache
      "cache6Months": [shared: true, validFor: 1 * 60 * 60 * 24 * 30 * 6] // cache for 6 months
    ]

With this in place, `cache-headers-filter` configuration part can be specified, which might look something like this:

    grails.plugins.cacheHeadersFilter.mappingList = [
        [
            cacheHeadersPreset: "noCache",
            type: "ant",
            pathList: [
                "/senchaWorkspace/build/production/**/*-classic-*.json",
                "/senchaWorkspace/build/production/**/*-modern-*.json",
                "/senchaWorkspace/build/production/**/bootstrap.js"
            ]
        ],
        [
            cacheHeadersPreset: "cache6Months",
            type: "ant",
            pathList: [
                "/senchaWorkspace/build/production/**/*.*"
            ]
        ]
    ]

To be able to use `cache-headers-filter` plugin, `grails.plugins.cacheHeadersFilter.mappingList` must be specified. `mappingList` is a list of maps, where each
map consist of several keys:
* `cacheHeadersPreset` - this is a name of a caching preset defined for `cache-headers` plugin. Name is used as a reference to the group of caching options to be set.
* `type` - optional parameter which defines type of matcher and, implicitly, the syntax to be used in `pathList` config. Allowed values are `ant` and `regex` (or just `re`). Default is `ant`.
* `pathList` - list of paths on which caching headers will be applied.

Note that ordering of `mappingList` elements is significant since first found match will be activated. Therefore, when creating `mappingList` configuration, start with most specific paths.

## Other config options
* `grails.plugins.cacheHeadersFilter.enabled` - boolean to enable or disable plugin. Default value is `true`.
* `grails.plugins.cacheHeadersFilter.filterMappingUrlPattern` - String for defining filter mapping url pattern for this filter. Default value is `/*`.

## Logging
To see log output of a plugin, turn on desired level of logging for a package `org.grails.plugins.cacheheadersfilter`:

    debug "org.grails.plugins.cacheheadersfilter"

## Version note
Although the plugin is currently at version 0.0.1, it is feature complete. It will go at version 1.0.0 after some passed time and production usage just to make sure there is no blockers present.
