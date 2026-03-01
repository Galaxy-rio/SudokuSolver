import Sudoku.*

@main
def main(): Unit =
	val puzzle = SudokuGenerator().generate()
	assert(puzzle.isConsistent())
	assert(DLXSolver.solve(puzzle).isInstanceOf[DLXSolver.State.Solved])
	puzzle.draw()
