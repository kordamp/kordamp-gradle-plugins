package org.example

import griffon.core.artifact.GriffonController
import griffon.metadata.ArtifactProviderFor

@ArtifactProviderFor(GriffonController)
class SampleController {
    SampleModel model

    void click() {
        model.clickCount++
    }
}