package com.richodemus.reader.mock.youtube

import io.dropwizard.Application
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment

class YoutubemockApplication : Application<YoutubemockConfiguration>() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            YoutubemockApplication().run(*args)
        }
    }

    override fun initialize(bootstrap: Bootstrap<YoutubemockConfiguration>) {

    }

    override fun run(configuration: YoutubemockConfiguration, environment: Environment) =
            environment.jersey().register(MockResource())
}
