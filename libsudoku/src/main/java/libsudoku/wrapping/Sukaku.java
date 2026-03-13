package libsudoku.wrapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import scala.Tuple2;
import scala.Tuple3;
import scala.jdk.javaapi.CollectionConverters;

public class Sukaku {
    private final libsudoku.Sukaku delegate;

    libsudoku.Sukaku unwrap() {
        return this.delegate;
    }

    public Sukaku() {
        this.delegate = libsudoku.Sukaku.apply();
    }

    public Sukaku(List<List<Cell>> board) {
        List<scala.collection.immutable.Vector<libsudoku.Cell>> scalaRows = board.stream()
            .map(row -> CollectionConverters.asScala(
                row.stream()
                    .map(Cell::unwrap)
                    .collect(Collectors.toList())
            ).toVector())
            .collect(Collectors.toList());
        scala.collection.immutable.Vector<scala.collection.immutable.Vector<libsudoku.Cell>> scalaBoard =
            CollectionConverters.asScala(scalaRows).toVector();
        this.delegate = libsudoku.Sukaku.apply(scalaBoard);
    }

    public record FixedPosition(Integer row, Integer col, Integer value) {
        public Integer component1() {
            return this.row;
        }

        public Integer component2() {
            return this.col;
        }

        public Integer component3() {
            return this.value;
        }
    }

    public record Position(Integer row, Integer col) {
        public Integer component1() {
            return this.row;
        }

        public Integer component2() {
            return this.col;
        }
    }
    public Sukaku(libsudoku.Sukaku delegate) {
        this.delegate = delegate;
    }

    public List<List<Cell>> board() {
        return CollectionConverters.asJava(this.delegate.board())
            .stream()
            .map(row -> CollectionConverters.asJava((scala.collection.Seq<libsudoku.Cell>) row)
                .stream()
                .map(cell -> new Cell(cell))
                .collect(Collectors.toList())
            )
            .collect(Collectors.toList());
    }

    public String serialize(char blank) {
        return this.delegate.serialize(blank);
    }

    public String serialize() {
        return this.delegate.serialize(this.delegate.serialize$default$1());
    }

    public String toString() {
        return this.delegate.toString();
    }

    public List<FixedPosition> fixedPositions() {
        return CollectionConverters.asJava(this.delegate.fixedPositions())
            .stream()
            .map(v -> (Tuple3<?, ?, ?>) v)
            .map(t -> new FixedPosition(ScalaTypes.fromScalaIndex(t._1()), ScalaTypes.fromScalaIndex(t._2()), ScalaTypes.fromScalaInt(t._3())))
            .collect(Collectors.toList());
    }

    public Sukaku fillNumber(Integer row, Integer col, Integer value) {
        return new Sukaku(this.delegate.fillNumber(row, col, value));
    }

    public Sukaku deleteNote(Integer row, Integer col, Integer value) {
        return new Sukaku(this.delegate.deleteNote(row, col, value));
    }

    public boolean isConsistent() {
        return this.delegate.isConsistent();
    }

    public boolean isBoardComplete() {
        return this.delegate.isBoardComplete();
    }

    public boolean isCandidate(Integer row, Integer col, Integer value) {
        return this.delegate.isCandidate(row, col, value);
    }

    public static Sukaku deserialize(String str, char blank) {
        return new Sukaku(libsudoku.Sukaku$.MODULE$.deserialize(str, blank));
    }

    public static Sukaku deserialize(String str) {
        return new Sukaku(libsudoku.Sukaku$.MODULE$.deserialize(
            str,
            libsudoku.Sukaku$.MODULE$.deserialize$default$2()
        ));
    }

    public static Sukaku fromString(String str) {
        return new Sukaku(libsudoku.Sukaku$.MODULE$.fromString(str));
    }

    public static List<List<Position>> iterUnits() {
        return CollectionConverters.asJava(libsudoku.Sudoku$.MODULE$.iterUnits()).stream()
            .map(unit -> CollectionConverters.asJava((scala.collection.IndexedSeq<?>) unit).stream()
                .map(p -> (Tuple2<?, ?>) p)
                .map(t -> new Position(ScalaTypes.fromScalaIndex(t._1()), ScalaTypes.fromScalaIndex(t._2())))
                .collect(Collectors.toList()))
            .collect(Collectors.toList());
    }

    public static Set<Position> peersOf(Integer row, Integer col) {
        return CollectionConverters.asJava(libsudoku.Sukaku$.MODULE$.peersOf(row, col))
            .stream()
            .map(p -> (Tuple2<?, ?>) p)
            .map(t -> new Position(ScalaTypes.fromScalaIndex(t._1()), ScalaTypes.fromScalaIndex(t._2())))
            .collect(Collectors.toSet());
    }

    public static Sukaku fromFixedNumbers(Sudoku sudoku) {
        return new Sukaku(libsudoku.Sukaku$.MODULE$.fromFixedNumbers(sudoku.unwrap()));
    }
}
