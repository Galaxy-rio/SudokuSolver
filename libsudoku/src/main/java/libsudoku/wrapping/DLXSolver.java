package libsudoku.wrapping;

public final class DLXSolver {
    private DLXSolver() {}

    public static SolverState solve(Sukaku sudoku) {
        if (sudoku == null) {
            throw new IllegalArgumentException("sudoku cannot be null");
        }
        return SolverState.asJava(libsudoku.DLXSolver$.MODULE$.solve(sudoku.unwrap()));
    }
}
