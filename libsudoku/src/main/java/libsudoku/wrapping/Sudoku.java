package libsudoku.wrapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import scala.Tuple2;
import scala.Tuple3;
import scala.jdk.javaapi.CollectionConverters;

public class Sudoku {
    private final libsudoku.Sudoku delegate;

    libsudoku.Sudoku unwrap() {
        return this.delegate;
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

    public Sudoku(libsudoku.Sudoku delegate) {
        this.delegate = delegate;
    }

    public Sudoku() {
        this.delegate = libsudoku.Sudoku.apply(
            libsudoku.Sudoku.$lessinit$greater$default$1()
        );
    }

    public Sudoku(Sukaku sudoku) {
        this.delegate = libsudoku.Sudoku.apply(sudoku.unwrap());
    }

    public Sudoku(List<List<Cell>> board) {
        List<scala.collection.immutable.Vector<libsudoku.Cell>> scalaRows = board.stream()
            .map(row -> CollectionConverters.asScala(
                row.stream()
                    .map(Cell::unwrap)
                    .collect(Collectors.toList())
            ).toVector())
            .collect(Collectors.toList());
        scala.collection.immutable.Vector<scala.collection.immutable.Vector<libsudoku.Cell>> scalaBoard =
            CollectionConverters.asScala(scalaRows).toVector();
        this.delegate = libsudoku.Sudoku.apply(scalaBoard);
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

    public Sudoku fillNumber(Integer row, Integer col, Integer value) {
        return new Sudoku(this.delegate.fillNumber(row, col, value));
    }

    public Sudoku deleteNote(Integer row, Integer col, Integer value) {
        return new Sudoku(this.delegate.deleteNote(row, col, value));
    }

    public Sudoku toggleNote(Integer row, Integer col, Integer value) {
        return new Sudoku(this.delegate.toggleNote(row, col, value));
    }

    public Sudoku setCell(Integer row, Integer col, Cell cell) {
        return new Sudoku(this.delegate.setCell(row, col, cell.unwrap()));
    }

    public boolean isConsistent() {
        return this.delegate.isConsistent();
    }

    public boolean isBoardComplete() {
        return this.delegate.isBoardComplete();
    }

    public Sukaku rebuildNotes() {
        return new Sukaku(this.delegate.rebuildNotes());
    }

    public static Sudoku deserialize(String str, char blank) {
        return new Sudoku(libsudoku.Sudoku$.MODULE$.deserialize(str, blank));
    }

    public static Sudoku deserialize(String str) {
        return new Sudoku(libsudoku.Sudoku$.MODULE$.deserialize(
            str,
            libsudoku.Sudoku$.MODULE$.deserialize$default$2()
        ));
    }

    public static Sudoku fromString(String str) {
        return new Sudoku(libsudoku.Sudoku$.MODULE$.fromString(str));
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
        return CollectionConverters.asJava(libsudoku.Sudoku$.MODULE$.peersOf(row, col))
            .stream()
            .map(p -> (Tuple2<?, ?>) p)
            .map(t -> new Position(ScalaTypes.fromScalaIndex(t._1()), ScalaTypes.fromScalaIndex(t._2())))
            .collect(Collectors.toSet());
    }
}
