package libsudoku.wrapping;

import java.util.Optional;
import scala.jdk.javaapi.OptionConverters;

public final class SudokuGenerator {
    private final libsudoku.SudokuGenerator delegate;

    public enum Difficulty {
        RANDOM,
        EASY,
        MEDIUM,
        HARD,
        IMPOSSIBLE
    }

    public SudokuGenerator() {
        this.delegate = new libsudoku.SudokuGenerator(
            OptionConverters.toScala(Optional.<Object>empty())
        );
    }

    public Sudoku generate(Difficulty difficulty) {
        if (difficulty == null) {
            throw new IllegalArgumentException("difficulty cannot be null");
        }
        return new Sudoku(this.delegate.generate(toScalaDifficulty(difficulty)));
    }

    public Sudoku generate() {
        return new Sudoku(this.delegate.generate(this.delegate.generate$default$1()));
    }

    private libsudoku.SudokuGenerator.Difficulty toScalaDifficulty(Difficulty difficulty) {
        libsudoku.SudokuGenerator.Difficulty$ d = this.delegate.Difficulty();
        return switch (difficulty) {
            case RANDOM -> d.Random();
            case EASY -> d.Easy();
            case MEDIUM -> d.Medium();
            case HARD -> d.Hard();
            case IMPOSSIBLE -> d.Impossible();
        };
    }
}