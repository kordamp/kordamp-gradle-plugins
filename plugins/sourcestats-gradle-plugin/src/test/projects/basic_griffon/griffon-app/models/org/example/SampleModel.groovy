package org.example

import griffon.core.artifact.GriffonModel
import griffon.metadata.ArtifactProviderFor

@ArtifactProviderFor(GriffonModel)
class SampleModel {
    @Observable int clickCount = 0
}