package mohr.jonas.autoplaylist.strategies

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure

object SelectionStrategySerializer : KSerializer<SelectionStrategy> {

    override val descriptor = buildClassSerialDescriptor("TrackSelectionStrategySerializer") {
        element<String>("type")
        element<Int>("minTimes", isOptional = true)
        element<Int>("maxTimes", isOptional = true)
        element<Int>("amount", isOptional = true)
        element<Int>("maxDays", isOptional = true)
    }

    override fun deserialize(decoder: Decoder): SelectionStrategy =
        decoder.decodeStructure(descriptor) {
            var type: String? = null
            var minTimes = 1
            var maxTimes = 1
            var maxDays = Int.MAX_VALUE
            var amount = 1
            while (true) {
                when (val index = decodeElementIndex(descriptor)) {
                    0 -> type = decodeStringElement(descriptor, 0)
                    1 -> minTimes = decodeIntElement(descriptor, 1)
                    2 -> maxTimes = decodeIntElement(descriptor, 2)
                    3 -> amount = decodeIntElement(descriptor, 3)
                    4 -> maxDays = decodeIntElement(descriptor, 4)
                    CompositeDecoder.DECODE_DONE -> break
                    else -> throw SerializationException("Unexpected index: $index")
                }
            }
            when (type) {
                "random" -> RandomSelectionStrategy(minTimes, maxTimes, maxDays)
                "latest" -> LatestSelectionStrategy(amount)
                "latestNotHeard" -> LatestNotHeardSelectionStrategy(amount)
                "popular" -> PopularSelectionStrategy(minTimes, maxTimes)
                "mostHeard" -> MostHeardSelectionStrategy(minTimes, maxTimes)
                else -> throw SerializationException("No fitting strategy for $type")
            }
        }

    override fun serialize(encoder: Encoder, value: SelectionStrategy) {
        throw UnsupportedOperationException("Selection strategies can't be serialized")
    }
}