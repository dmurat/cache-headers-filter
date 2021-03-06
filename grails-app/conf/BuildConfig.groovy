grails.project.work.dir = 'target'

grails.project.fork = [
    //  compile: [maxMemory: 256, minMemory: 64, debug: false, maxPerm: 256, daemon:true],
    test: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, daemon: true],
    run: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve: false],
    war: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256, forkReserve: false],
    console: [maxMemory: 768, minMemory: 64, debug: false, maxPerm: 256]
]

grails.project.dependency.resolver = "maven"
grails.project.dependency.resolution = {
  inherits("global") {
  }

  log "warn"

  repositories {
    grailsCentral()
    mavenLocal()
    mavenCentral()
  }

  dependencies {
  }

  plugins {
    build(":release:3.1.1", ":rest-client-builder:2.1.1") {
      export = false
    }

    compile ":cache-headers:1.1.7"
  }
}
