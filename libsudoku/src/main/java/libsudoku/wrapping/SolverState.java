package libsudoku.wrapping;

import scala.jdk.javaapi.FunctionConverters;

public sealed interface SolverState
    permits SolverState.Invalid, SolverState.Partial, SolverState.Solved, SolverState.MultiSolution {

    record Invalid() implements SolverState {}
    record MultiSolution() implements SolverState {}
    record Partial(Sukaku sudoku) implements SolverState {}
    record Solved(Sukaku sudoku) implements SolverState {}

    public static SolverState asJava(libsudoku.SudokuSolver.State state) {
        if (state == null) {
            throw new IllegalArgumentException("state cannot be null");
        }

        return state.fold(
            FunctionConverters.asScalaFromSupplier(() -> new Invalid()),
            FunctionConverters.asScalaFromFunction((libsudoku.Sukaku su) -> new Partial(new Sukaku(su))),
            FunctionConverters.asScalaFromFunction((libsudoku.Sukaku su) -> new Solved(new Sukaku(su))),
            FunctionConverters.asScalaFromSupplier(() -> new MultiSolution())
        );
    }
}
