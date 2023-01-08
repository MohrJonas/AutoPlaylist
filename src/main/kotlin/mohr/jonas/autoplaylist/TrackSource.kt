package mohr.jonas.autoplaylist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import mohr.jonas.autoplaylist.strategies.SelectionStrategy

@Serializable
data class TrackSource(
    @SerialName("id") val trackId: String,
    @SerialName("type") val trackType: String,
    @SerialName("strategy") val selectionStrategy: SelectionStrategy
)
