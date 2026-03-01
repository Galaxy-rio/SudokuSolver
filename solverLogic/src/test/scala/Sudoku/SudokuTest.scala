package Sudoku

class Tests extends munit.FunSuite:
	private val board = "12.45.7.....4.67.9.2.4.67..1..4.6..9.......8...3.......2..567.9...4567.9.2.45....1..4..7.....4.6789...4.678.....5....1..4.6....2.........3..67.9...4.67.9..34...8..2.45.......4.6.89..3.........4.6..9...4.6.........7..1...........456..9.2.45..8...34..7......5............91..4.6.8.1..4.6...1..4.6.8...3...7...2.......1.3.............8...34..7.....4..7...2...............91..4.......3.5.7..1...5.7.......6........6....2.......1..........3............7......5.......4............8.........9........91............5.......4.6....2..........4.6..........8...3............7...234..7....34.67...2.4.67..1......8.....5....1......8..2...6..9...4.6..9.2.4......2.4........4.6.8..2.4.6.8.......7....3..............9.2..56...1..456...12.45...."
	private val solved = "......7.......6....2.......1...............8...3..............9...4.........5....1................9...4.........5.........6....2.........3............7.........8.....5...........8...3..............9...4...........7..1.............6....2..........4.........5............9.....6...1...............8.......7...2.........3.............8...3............7...2...............9...4.........5....1.............6........6....2.......1..........3............7......5.......4............8.........9........91............5.......4......2............6..........8...3............7....3............7.......6..........8.....5....1.........2...............9...4......2..........4............8.......7....3..............9.....6.......5....1........"
	private val multi = "12.............7......5....12..............8.........9.....6......4.......3......12............6......4.....12.........3............7......5...........8.........9........9..3.............8.....5.......4..........6.........7...2.......1..........3......1................9.......8.......7......5.......4..........6....2............6.......5..........7..........9.2..........4.....1..........3.............8....4............8..2............6...1..........3..............9....5..........7.........8..2............6......4.............91..........3............7......5........5............9..3............7.......6..........8..2.......1...........4...........7.....4.....1..........3..........5.....2..............8.........9.....6..."

	test("serialize and equal") {
		val partialSudoku = PartialSudoku.deserialize(board)
		val sudoku = Sudoku.deserialize(board)
		assert(partialSudoku.isConsistent())
		assert(sudoku.isConsistent())

		assertEquals(sudoku.board(0)(4).number(), Some(8))
		assertEquals(sudoku.board(1)(3).number(), Some(5))
		assertEquals(sudoku.board(1)(5).number(), Some(2))
		assertEquals(sudoku.board(2)(2).number(), Some(3))
		assertEquals(sudoku.board(2)(6).number(), Some(1))
		assertEquals(sudoku.board(3)(1).number(), Some(5))
		assertEquals(sudoku.board(3)(7).number(), Some(2))
		assertEquals(sudoku.board(4)(0).number(), Some(8))
		assertEquals(sudoku.board(4)(4).number(), Some(9))
		assertEquals(sudoku.board(4)(8).number(), Some(6))
		assertEquals(sudoku.board(5)(0).number(), Some(6))
		assertEquals(sudoku.board(5)(3).number(), Some(3))
		assertEquals(sudoku.board(5)(4).number(), Some(7))
		assertEquals(sudoku.board(5)(5).number(), Some(5))
		assertEquals(sudoku.board(5)(8).number(), Some(9))
		assertEquals(sudoku.board(6)(1).number(), Some(1))
		assertEquals(sudoku.board(6)(2).number(), Some(5))
		assertEquals(sudoku.board(6)(4).number(), Some(2))
		assertEquals(sudoku.board(6)(6).number(), Some(8))
		assertEquals(sudoku.board(6)(7).number(), Some(3))
		assertEquals(sudoku.board(7)(4).number(), Some(5))
		assertEquals(sudoku.board(8)(3).number(), Some(7))
		assertEquals(sudoku.board(8)(4).number(), Some(3))
		assertEquals(sudoku.board(8)(5).number(), Some(9))

		assertEquals(sudoku.serialize(), board)

		assert(sudoku == partialSudoku)
	}

	test("fill and consistent") {
		var sudoku = Sudoku.deserialize(board)
		sudoku = sudoku.fillNumber(0, 0, 1)
		assert(sudoku.board(0)(0).number() == Some(1))
		assert(sudoku.isConsistent())
		assert(PartialSudoku(sudoku.board).isConsistent())
		sudoku = sudoku.fillNumber(1, 1, 2)
		assert(!sudoku.isConsistent())
	}

	test("DLX solve") {
		val sudoku = Sudoku.deserialize(board)
		val result = DLXSolver.solve(sudoku)
		assertMatches(result) {
			case DLXSolver.State.Solved(solution) => solution == Sudoku.deserialize(solved)
			case _ => false
		}
		val multiResult = DLXSolver.solve(Sudoku.deserialize(multi))
		assertEquals(multiResult, DLXSolver.State.MultiSolution)
	}

	test("puzzle generation") {
		val puzzle = SudokuGenerator().generate()
		assert(puzzle.isConsistent())
		assert(DLXSolver.solve(puzzle).isInstanceOf[DLXSolver.State.Solved])
	}
