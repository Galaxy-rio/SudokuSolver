package libsudoku.wrapping;

final class ScalaTypes {
    private ScalaTypes() {}

    static int toInt(Integer i) {
        if (i == null) {
            throw new IllegalArgumentException("Integer value cannot be null");
        }
        return libsudoku.SudokuInt$.MODULE$.fromInt(i.intValue());
    }

    static Integer fromInt(int i) {
        return Integer.valueOf(i);
    }

    static libsudoku.SudokuInt toScalaInt(Integer i) {
        if (i == null) {
            throw new IllegalArgumentException("Integer value cannot be null");
        }
        return new libsudoku.SudokuInt(libsudoku.SudokuInt$.MODULE$.fromInt(i.intValue()));
    }

    static Integer fromScalaInt(Object i) {
        if (i == null) {
            return null;
        }
        if (!(i instanceof libsudoku.SudokuInt)) {
            throw new IllegalArgumentException("Expected a SudokuInt, got: " + i.getClass().getName());
        }
        return Integer.valueOf(((libsudoku.SudokuInt) i).value());
    }

    static int toIndex(Integer i) {
        if (i == null) {
            throw new IllegalArgumentException("Integer value cannot be null");
        }
        return libsudoku.SudokuIndex$.MODULE$.fromInt(i.intValue());
    }

    static Integer fromIndex(int i) {
        return Integer.valueOf(i);
    }

    static libsudoku.SudokuIndex toScalaIndex(Integer i) {
        if (i == null) {
            throw new IllegalArgumentException("Integer value cannot be null");
        }
        return new libsudoku.SudokuIndex(libsudoku.SudokuIndex$.MODULE$.fromInt(i.intValue()));
    }

    static Integer fromScalaIndex(Object i) {
        if (i == null) {
            return null;
        }
        if (!(i instanceof libsudoku.SudokuIndex)) {
            throw new IllegalArgumentException("Expected a SudokuIndex, got: " + i.getClass().getName());
        }
        return Integer.valueOf(((libsudoku.SudokuIndex) i).value());
    }
}
