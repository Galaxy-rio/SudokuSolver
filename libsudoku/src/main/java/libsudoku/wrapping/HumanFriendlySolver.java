package libsudoku.wrapping;

import java.util.List;
import java.util.Iterator;
import java.util.stream.Collectors;
import scala.jdk.javaapi.CollectionConverters;

public final class HumanFriendlySolver {
    private HumanFriendlySolver() {}

    @FunctionalInterface
    public interface Step extends scala.Function1<libsudoku.Sukaku, libsudoku.HumanFriendlySolver.InternalState> {
        static Step of(scala.Function1<libsudoku.Sukaku, libsudoku.HumanFriendlySolver.InternalState> f) {
            return f::apply;
        }
    }

    public static Step or(Step step, Step... steps) {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.or(step, toScalaSeq(steps)));
    }

    public static Step many(Step step) {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.many(step));
    }

    public static Step choice(Step step, Step... steps) {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.choice(step, toScalaSeq(steps)));
    }

    public static Step identity() {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.identity());
    }

    public static Step bottomStep() {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.bottomStep());
    }

    public static Step topStep() {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.topStep());
    }

    public static Step simpleTechnique() {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.simpleTechnique());
    }

    public static Step mediumTechnique() {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.mediumTechnique());
    }

    public static Step hardTechnique() {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.hardTechnique());
    }

    public static Step uniquenessTechnique() {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.uniquenessTechnique());
    }

    private static scala.collection.immutable.Seq<
        scala.Function1<libsudoku.Sukaku, libsudoku.HumanFriendlySolver.InternalState>
    > toScalaSeq(Step... steps) {
        List<scala.Function1<libsudoku.Sukaku, libsudoku.HumanFriendlySolver.InternalState>> list =
            java.util.Arrays.stream(steps)
                .map(s -> (scala.Function1<libsudoku.Sukaku, libsudoku.HumanFriendlySolver.InternalState>) s)
                .collect(Collectors.toList());
        return CollectionConverters.asScala(list).toList();
    }

    public static SolverState solve(Sukaku sudoku) {
        return SolverState.asJava(libsudoku.HumanFriendlySolver$.MODULE$.solve(sudoku.unwrap()));
    }

    public static Iterator<libsudoku.HumanFriendlySolver.InternalState> solveStepByStep(Sukaku sudoku) {
        return CollectionConverters.asJava(
            libsudoku.HumanFriendlySolver$.MODULE$.solveStepByStep(sudoku.unwrap())
        );
    }

    public static Step hiddenSubset() {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.hiddenSubset());
    }

    public static Step nakedSubset() {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.nakedSubset());
    }

    public static Step lockedCandidate() {
        return Step.of(libsudoku.HumanFriendlySolver$.MODULE$.lockedCandidate());
    }
}
