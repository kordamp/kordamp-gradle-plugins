package org.kordamp.gradle.plugin.base.extensions

import com.agorapulse.testing.fixt.Fixt
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.TempDir

class ConfigExtensionSpec extends Specification {

    @Shared Fixt fixt = Fixt.create(ConfigExtensionSpec)

    @TempDir File projectRoot

    void 'verify dsl'() {
        given:
            new File(projectRoot, 'build.gradle').write(fixt.readText('build.gradle'))
        when:
            BuildResult result = GradleRunner.create()
                    .withProjectDir(projectRoot)
                    .forwardOutput()
                    .withPluginClasspath()
                    .withArguments('extensionTest', '--stacktrace')
                    .build()
        then:
            result.task(':extensionTest').outcome == TaskOutcome.SUCCESS
    }
}
