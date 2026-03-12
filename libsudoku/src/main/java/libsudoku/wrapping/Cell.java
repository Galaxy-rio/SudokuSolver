package libsudoku.wrapping;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import scala.jdk.javaapi.OptionConverters;
import scala.jdk.javaapi.CollectionConverters;

public class Cell {
    private final libsudoku.Cell delegate;

    libsudoku.Cell unwrap() {
        return this.delegate;
    }

    public Cell(libsudoku.Cell delegate) {
        this.delegate = delegate;
    }

    public static Cell Fixed(Integer value) {
        return new Cell(libsudoku.Cell$.Fixed.apply(ScalaTypes.toInt(value)));
    }

    public static Cell Notes(Integer... candidates) {
        return new Cell(libsudoku.Cell$.Notes.apply(libsudoku.CellNotes$.MODULE$.apply(
            CollectionConverters.asScala(List.of(candidates)
                .stream()
                .map(ScalaTypes::toScalaInt)
                .collect(Collectors.toList())
            ).toSeq()
        )));
    }

    public String serialize(char blank) {
        return this.delegate.serialize(blank);
    }

    public String serialize() {
        return this.delegate.serialize(this.delegate.serialize$default$1());
    }

    public boolean isFixed() {
        return this.delegate.isFixed();
    }

    public Integer number() {
        Object raw = OptionConverters.toJava(this.delegate.number()).orElse(null);
        if (raw == null) return null;
        return ScalaTypes.fromScalaInt((libsudoku.SudokuInt) raw);
    }

    public Set<Integer> candidates() {
        return CollectionConverters.asJava(this.delegate.candidates())
            .stream()
            .map(v -> ScalaTypes.fromScalaInt(v))
            .collect(Collectors.toSet());
    }

    public boolean isConsistent() {
        return this.delegate.isConsistent();
    }

    public Cell toggleNote(Integer value) {
        return new Cell(this.delegate.toggleNote(ScalaTypes.toInt(value)));
    }

    public Cell reduce() {
        return new Cell(this.delegate.reduce());
    }

    public static Cell deserialize(String str, char blank) {
        return new Cell(libsudoku.Cell$.MODULE$.deserialize(str, blank));
    }

    public static Cell deserialize(String str) {
        return new Cell(libsudoku.Cell$.MODULE$.deserialize(
            str,
            libsudoku.Cell$.MODULE$.deserialize$default$2()
        ));
    }

    public static Cell invalid() {
        return new Cell(libsudoku.Cell$.MODULE$.invalid());
    }

    public static Cell blank() {
        return new Cell(libsudoku.Cell$.MODULE$.blank());
    }
}
