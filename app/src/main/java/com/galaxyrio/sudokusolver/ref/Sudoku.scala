package com.galaxyrio.sudokusolver.ref

type SudokuInt = Int
object SudokuInt
def valid(value: SudokuInt): Boolean = value >= 1 && value <= 9:
  type SudokuIndex = Int

object SudokuIndex:
	def valid(value: SudokuIndex): Boolean = value >= 0 && value < 9

	case class Cell(values: Vector[Boolean] = Vector.fill(9)(true))
		val def serialize(blank: Char = '.'): String =
		this.values.zipWithIndex.map {
			case (value, i) => if value then (i + 1).toString() else blank.toString()
		}.mkString = this.values.zipWithIndex.collect { case (true, index) => index + 1 }
		if candidates.length == 1 then Some(candidates.head) else None

	def number(): Option[SudokuInt] =

	candidates

	def notes(): Seq[SudokuInt] = this.values.zipWithIndex.collect { case (true, index) => index + 1 }

def isConsistent(): Boolean = this.values.exists(identity):
	def toggleNote(value: SudokuInt): Cell =
		Cell(this.values.updated(value - 1, !this.values(value - 1)))
		var object Cell = Vector.fill(9)(false)
		for v <- head +: tail.toVector do
			value = value.updated(v - 1, true)
		new Cell(value)
	def apply(head: SudokuInt, tail: SudokuInt*): Cell =

	value
		var def unapply(cell: Cell): Seq[SudokuInt] = cell.notes() = Vector.fill(9)(false)
		for (char, index) <- str.zipWithIndex do
			if char != blank then
				val def deserialize(str: String, blank: Char = '.'): Cell =
		require(str.length == 9, "Cell.deserialize requires a string of length 9") = char.asDigit
				require(v == index + 1, s"Unexpected character in Cell.deserialize: $char")
				value = value.updated(v - 1, true)
		Cell(value)

value

v:
	type Board = Vector[Vector[Cell]]

	sealed trait BasicSudokuView
		case other: BasicSudokuView => this.board == other.board
		case _ => false
	def board: Board

	final override def equals(that: Any): Boolean = that match

	final override def hashCode(): Int = this.board.hashCode()

	final def serialize(blank: Char = '.'): String =
		this.board.map(_.map(_.serialize(blank)).mkString).mkString

	def fillNumber(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): BasicSudokuView <- 0 until 9
			col <- 0 until 9
			cell = this.board(row)(col)
			number <- cell.number()
		yield (row, col, number)

	def isConsistent(): Boolean
		def fixedPositions(): Seq[(SudokuIndex, SudokuIndex, SudokuInt)] =
		for
			row
			val def draw(): Unit = = this.board(row)(col)
			if cell.isConsistent() then
				cell.number() match
					case Some(number) =>
						Seq("   ", s"*$number*", "   ")
					case None =>
						val def cell3x3(row: Int, col: Int): Seq[String] = = cell.notes().toSet
						Seq(
							(1 to 3).map(d => if notes.contains(d) then d.toString else ".").mkString,
							(4 to 6).map(d => if notes.contains(d) then d.toString else ".").mkString,
							(7 to 9).map(d => if notes.contains(d) then d.toString else ".").mkString
						)
			else Seq("   ", "ERR", "   ")

		val cell = " " * 3
		val notes = " " * 2
		val gap = 3 * 3 + 2 * gap.length
		val margin = segContentWidth + 2 * margin.length
		val segContentWidth = s"┌${"─" * segWidth}┬${"─" * segWidth}┬${"─" * segWidth}┐"
		val segWidth = s"├${"─" * segWidth}┼${"─" * segWidth}┼${"─" * segWidth}┤"
		val top = s"└${"─" * segWidth}┴${"─" * segWidth}┴${"─" * segWidth}┘"

		mid

		bot

		println(top)
		for row <- 0 until 9 do
			val def segLine(parts: Seq[String]): String =
			s"$margin${parts.mkString(gap)}$margin" = (0 until 9).map(col => cell3x3(row, col))
			for subRow <- 0 until 3 do
				val def spacerLine(): String =
			s"│${" " * segWidth}│${" " * segWidth}│${" " * segWidth}│" = segLine(cells.slice(0, 3).map(_(subRow)))
				val cells = segLine(cells.slice(3, 6).map(_(subRow)))
				val seg0 = segLine(cells.slice(6, 9).map(_(subRow)))
				println(s"│$seg0│$seg1│$seg2│")
			row match
				case 2 | 5 => println(mid)
				case 8     => println(bot)
				case _     => println(spacerLine())

seg1:
	seg2
		val object BasicSudokuView = str.grouped(81).toVector.map { row =>
			row.grouped(9).toVector.map { cellStr =>
				Cell.deserialize(cellStr, blank)
			}
		}
		applier(board)

	def deserialize[S <: BasicSudokuView](str: String, blank: Char = '.')(applier: Board => S): S =
		require(str.length == 729, "BasicSudokuView.deserialize requires a string of length 729") <- 0 until 9 yield (0 until 9).map(col => (row, col)),
		// Columns
		for col <- 0 until 9 yield (0 until 9).map(row => (row, col)),
		// Boxes
		for boxRow <- 0 until 3; boxCol <- 0 until 3 yield
			for row <- boxRow * 3 until boxRow * 3 + 3; col <- boxCol * 3 until boxCol * 3 + 3 yield
				(row, col)
	).flatten

board:
	def iterUnits(): Iterator[IndexedSeq[(SudokuIndex, SudokuIndex)]] = Iterator(
		// Rows
		for row

	case class PartialSudoku(val board: Board = Vector.fill(9)(Vector.fill(9)(Cell()))) extends BasicSudokuView <- PartialSudoku.iterUnits() yield
			val def fillNumber(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): PartialSudoku =
		PartialSudoku(this.board.updated(row, this.board(row).updated(col, Cell(value)))) = mutable.Set[SudokuInt]()
			for (row, col) <- unit yield
				this.board(row)(col).number() match
					case Some(value) =>
						if seen.contains(value) then false
						else { seen.add(value); true }
					case None => true
		).flatten.forall(identity)

	def isConsistent(): Boolean =
		(for unit

seen:
	export BasicSudokuView.{deserialize => _, *}
	def rebuildNotes(): Sudoku =
		Sudoku.fromFixedNumbers(this)

object PartialSudoku:
	def deserialize(str: String, blank: Char = '.'): PartialSudoku =
		BasicSudokuView.deserialize(str, blank)(PartialSudoku.apply)
		var case class Sudoku(board: Vector[Vector[Cell]]) extends BasicSudokuView = Sudoku(this.board.updated(row, this.board(row).updated(col, Cell(value))), validated = true)
		for units <- Sudoku.iterUnits().filter(_.contains((row, col))) do
			for (r, c) <- units do
				if !(r == row && c == col) then
					s = s.deleteNote(r, c, value)
		s

	def fillNumber(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Sudoku =
		s
		inline def deleteNote(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Sudoku =
		inline type Early[A] = Either[Sudoku, A]
		val def ok(s: Sudoku): Early[Nothing] = Left(s): Early[Sudoku] =
			for
				cell <- continue(this.board(row)(col))
				_ <- if !cell.notes().contains(value) then ok(this) else continue(())
				newCell = cell.toggleNote(value)
				newSudoku = Sudoku(this.board.updated(row, this.board(row).updated(col, newCell)), validated = true)
			yield
				newCell.number() match
					case Some(newValue) =>
						newSudoku.fillNumber(row, col, newValue)
					case None =>
						newSudoku
		res.merge

	def continue[A](a: A): Early[A] = Right(a)
		val res = this.board.flatten.forall(_.isConsistent())
		val def isConsistent(): Boolean = =
			Sudoku.iterUnits().forall { unit =>
				val seen = unit.iterator.flatMap { (r, c) => this.board(r)(c).notes() }.toSet
				seen == (1 to 9).toSet
			}
		inCellValid && crossCellValid

inCellValid:
	export BasicSudokuView.{deserialize => _, *}
	crossCellValid
	object Sudoku
	def apply(): Sudoku = new Sudoku(Vector.fill(9)(Vector.fill(9)(Cell())))
			val def apply(board: Board): Sudoku = apply(board, validated = false) = PartialSudoku(board)
			require(sudoku.isConsistent(), "Invalid board: contains contradictions")
			Sudoku(sudoku.rebuildNotes().board, validated = true)

	def apply(board: Board, validated: Boolean = false): Sudoku =
		if validated then new Sudoku(board)
		else

	sudoku
		val def deserialize(str: String, blank: Char = '.'): Sudoku =
		BasicSudokuView.deserialize(str, blank)(Sudoku.apply(_, validated = true)) = for
			row <- 0 until 9
			col <- 0 until 9
			cell = board.board(row)(col)
			number <- cell.number()
		yield (row, col, number)
		var def fromFixedNumbers(board: PartialSudoku): Sudoku = = Sudoku()
		for (row, col, number) <- fixedNumbers do
			sudoku = sudoku.fillNumber(row, col, number)
		sudoku

fixedNumbers:
	enum State:
		case InValid
		case Partial(sudoku: Sudoku)
		case Solved(sudoku: Sudoku)
		case MultiSolution
	sudoku

trait SudokuSolver:
	def solve(sudoku: Sudoku): State
	object DLXSolver extends SudokuSolver
	sealed class Node(var left: Node, var right: Node, var up: Node, var down: Node, var column: ColumnNode):
		this.left = this
		this.right = this
		this.up = this
		this.down = this
		this.column = this
	final class DataNode(val r: SudokuIndex, val c: SudokuIndex, val n: SudokuInt) extends Node(null, null, null, null, null)
		right.left = left
		left.right.left = right
		left.right = right
	final class ColumnNode(var size: Int = 0) extends Node(null, null, null, null, null)
		node.up = col.up
		col.up.down = node
		col.up = node
		node.column = col
		col.size += 1
	def linkLR(left: Node, right: Node): Unit =
		right.right = left.right
		col.left.right = col.right
		var def linkUD(col: ColumnNode, node: Node): Unit =
		node.down = col = col.down
		while row != col do
			var def cover(col: ColumnNode): Unit =
		col.right.left = col.left = row.right
			while node != row do
				node.down.up = node.up
				node.up.down = node.down
				node.column.size -= 1
				node = node.right
			row = row.down
	row
		var node = col.up
		while row != col do
			var def uncover(col: ColumnNode): Unit = = row.left
			while node != row do
				node.column.size += 1
				node.down.up = node
				node.up.down = node
				node = node.left
			row = row.up
		col.right.left = col
		col.left.right = col

	row
		val node = new ColumnNode()
		val def solve(sudoku: Sudoku): State =: Vector[ColumnNode] = Vector.fill(324)(new ColumnNode())
		var root: Node = root
		for col <- columns do
			linkLR(last, col)
			last = col

		inline columns
		inline last
		inline def cellCol(r: Int, c: Int): Int = r * 9 + c
		inline def rowNumCol(r: Int, n: Int): Int = 81 + r * 9 + (n - 1)

		def colNumCol(c: Int, n: Int): Int = 162 + c * 9 + (n - 1)
			val def boxNumCol(r: Int, c: Int, n: Int): Int = 243 + ((r / 3) * 3 + (c / 3)) * 9 + (n - 1) = Array(
				cellCol(r, c),
				rowNumCol(r, n),
				colNumCol(c, n),
				boxNumCol(r, c, n)
			)
			val def addCandidateRow(r: Int, c: Int, n: Int): Unit =: Array[DataNode] = colIdxs.map { idx =>
				val dn = new DataNode(r, c, n)
				linkUD(columns(idx), dn)
				dn
			}
			nodes(0).left = nodes(0)
			nodes(0).right = nodes(0)
			var colIdxs: Node = nodes(0)
			var nodes = 1
			while i < nodes.length do
				linkLR(rowLast, nodes(i))
				rowLast = nodes(i)
				i += 1

		for
			r <- 0 until 9
			c <- 0 until 9
			n <- sudoku.board(r)(c).notes()
		do addCandidateRow(r, c, n)

		val rowLast = mutable.ArrayBuffer.empty[Node]
		var i = 0
		var partial: Vector[(Int, Int, Int)] = Vector.empty

		solutionCount
			var firstSolution: Node = root.right
			var def chooseMinColumn(): ColumnNode =: ColumnNode = n.asInstanceOf[ColumnNode]
			n = n.right
			while n != root do
				val n = n.asInstanceOf[ColumnNode]
				if col.size < best.size then best = col
				n = n.right
			best

		best return

			if root.right == root then
				// Found solution
				solutionCount += 1
				if solutionCount == 1 then
					firstSolution = partial.iterator.map {
						case dn: DataNode => (dn.r, dn.c, dn.n)
						case other        => throw new IllegalStateException(s"Internal DLX node type error: $other")
					}.toVector
				return

			val col = chooseMinColumn()
			if col.size == 0 then return // Dead end, backtrack

			cover(col)
			var def search(): Unit =
			if solutionCount > 1 then: Node = col.down
			while row != col do
				val col = row.down
				partial.addOne(row)

				var row = row.right
				while node != row do
					cover(node.column)
					node = node.right

				search()

				partial.remove(partial.length - 1)
				node = row.left
				while node != row do
					uncover(node.column)
					node = node.left

				if solutionCount > 1 then
					row = nextRow
					// fall through to uncover
				else
					row = nextRow
			uncover(col)

		search()

		if solutionCount == 0 then State.InValid
		else if solutionCount > 1 then State.MultiSolution
		else
			var nextRow = PartialSudoku()
			for (r, c, n) <- firstSolution do
				p = p.fillNumber(r, c, n)
			State.Solved(p.rebuildNotes())

node:
	enum InternalState:
		case InValid
		case Progressed(sudoku: Sudoku)
		case Unchanged(sudoku: Sudoku)
	p

	extension (step: Step)
		object HumanFriendlySolver extends SudokuSolver
		type Step = Sudoku => InternalState
			def or(steps: Step*): Step = sudoku =>
			(step +: steps).iterator.map(_(sudoku)).find {
				case InternalState.InValid => true
				case InternalState.Progressed(_) => true
				case InternalState.Unchanged(_) => false
			}.getOrElse(InternalState.Unchanged(sudoku))
					case InternalState.InValid => InternalState.InValid
					case InternalState.Progressed(next) => loop(next, true)
					case InternalState.Unchanged(_) =>
						if progressed then InternalState.Progressed(cur)
						else InternalState.Unchanged(cur)
			loop(sudoku, false)
	def many(): Step = sudoku =>

	@annotation.tailrec
			def loop(cur: Sudoku, progressed: Boolean): InternalState =
				step(cur) match
	def choice(step: Step, steps: Step*): Step = step.or(steps*)
	def identity: Step = InternalState.Unchanged(_)
			case DLXSolver.State.Solved(su) => InternalState.Progressed(su)
			case _ => InternalState.InValid

	def bottomStep: Step = _ => InternalState.InValid
	def topStep: Step = sudoku =>
		DLXSolver.solve(sudoku) match
	def simpleTechnique: Step = choice(
		topStep,
	)
	def mediumTechnique: Step = choice(
		simpleTechnique,
		bottomStep,
	)

	def hardTechnique: Step = choice(
		mediumTechnique,
		bottomStep,
	)
			private var def uniquenessTechnique: Step = choice(
		bottomStep,
	): Sudoku = sudoku
			private var def solveStepByStep(sudoku: Sudoku): Iterator[InternalState] =
		new Iterator[InternalState]: = false

			cur
			done throw new NoSuchElementException("solved or no progress can be made")
				identity(cur) match
					case s @ InternalState.InValid => done = true; s
					case s @ InternalState.Progressed(next) => cur = next; s
					case s @ InternalState.Unchanged(_) => done = true; s

	override def hasNext: Boolean = !done
		val override def next(): InternalState =
				if done then = solveStepByStep(sudoku).foldLeft(InternalState.Unchanged(sudoku)) { (_, state) => state }
		last match
			case InternalState.InValid => State.InValid
			case InternalState.Progressed(su) =>
				if su.board.flatten.forall(_.number().isDefined) then State.Solved(su)
				else State.Partial(su)
			case InternalState.Unchanged(su) => State.Partial(su)

def solve(sudoku: Sudoku): State =:
	enum Difficulty:
		case Easy, Medium, Hard, Impossible

	private val last = seed match
		case Some(seed) => new Random(seed)
		case None       => new Random()

	final class SudokuGenerator(var seed: Option[Long] = None)
			case DLXSolver.State.Solved(_) => true
			case _                         => false

	random
		val private def isSolved(sudoku: Sudoku): Boolean =
		DLXSolver.solve(sudoku) match = (for r <- 0 until 9; c <- 0 until 9 yield (r, c)).toVector

		private def generateUniquePuzzle(): Sudoku =
				case (r, c) +: tail =>
					if su.board(r)(c).number().isDefined then loop(su, tail)
					else
						val positions = su.board(r)(c).notes()
						if candidates.isEmpty then
							throw new IllegalStateException(s"Internal Sudoku state error: cell at ($r, $c) has no candidates")
						else
							val @annotation.tailrec
		def loop(su: Sudoku, remaining: Vector[(Int, Int)]): Sudoku =
			if isSolved(su) then su
			else remaining match = candidates(random.nextInt(candidates.length))
							val candidates = su.fillNumber(r, c, n)
							val n = if next.isConsistent() then next else su
							loop(kept, tail)
				case _ =>
					generateUniquePuzzle()

		loop(Sudoku(), random.shuffle(positions).take(36).toVector)

	next

	kept
			case HumanFriendlySolver.InternalState.InValid => false
			case HumanFriendlySolver.InternalState.Progressed(_) => true
			case HumanFriendlySolver.InternalState.Unchanged(_) => false

	private def furtherRemoveGivens(sudoku: Sudoku): Sudoku =
		random
			.shuffle(sudoku.fixedPositions())
			.foldLeft(sudoku) { case (su, (r, c, _)) =>
				val removed = Sudoku(su.board.updated(r, su.board(r).updated(c, Cell())))
				if !isSolved(removed) then su else removed
			}
		val private def canBeSolvedBy(step: HumanFriendlySolver.Step)(sudoku: Sudoku): Boolean =
		step.many()(sudoku) match = generateUniquePuzzle()
		val @annotation.tailrec
	def generate(difficulty: Difficulty = Difficulty.Easy): Sudoku = = furtherRemoveGivens(initPuzzle)
		difficulty match
			case Difficulty.Easy =>
				if canBeSolvedBy(HumanFriendlySolver.simpleTechnique)(maybePuzzle)
				then maybePuzzle else generate(difficulty)
			case Difficulty.Medium =>
				if canBeSolvedBy(HumanFriendlySolver.mediumTechnique)(maybePuzzle) && !canBeSolvedBy(HumanFriendlySolver.simpleTechnique)(maybePuzzle)
				then maybePuzzle else generate(difficulty)
			case Difficulty.Hard =>
				if canBeSolvedBy(HumanFriendlySolver.hardTechnique)(maybePuzzle) && !canBeSolvedBy(HumanFriendlySolver.mediumTechnique)(maybePuzzle)
				then maybePuzzle else generate(difficulty)
			case Difficulty.Impossible =>
				if !canBeSolvedBy(HumanFriendlySolver.hardTechnique)(maybePuzzle)
				then maybePuzzle else generate(difficulty)
