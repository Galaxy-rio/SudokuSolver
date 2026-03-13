package com.galaxyrio.sudokusolver.game.serializer

import libsudoku.wrapping.Sudoku
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object SudokuSerializer : KSerializer<Sudoku> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Sudoku", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Sudoku) =
        encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): Sudoku =
        Sudoku.fromString(decoder.decodeString())
}
