package Sudoku

import scala.collection.mutable
import scala.util.Random

class cache[A, B](f: A => B) extends (A => B):
	private val memo = mutable.Map.empty[A, B]
	def apply(a: A): B = this.memo.getOrElseUpdate(a, f(a))
object cache:
	def apply[A, B](f: A => B): A => B = new cache(f)

class cache1[A, B](f: A => B) extends (A => B):
	private var lastArg: Option[A] = None
	private var lastResult: Option[B] = None
	def apply(a: A): B =
		if this.lastArg.contains(a) then this.lastResult.get
		else
			val result = f(a)
			this.lastArg = Some(a)
			this.lastResult = Some(result)
			result
object cache1:
	def apply[A, B](f: A => B): A => B = new cache1(f)

type SudokuInt = Int
object SudokuInt:
	def valid(value: SudokuInt): Boolean = (1 to 9).contains(value)
type SudokuIndex = Int
object SudokuIndex:
	def valid(value: SudokuIndex): Boolean = (0 until 9).contains(value)

case class Cell(values: Vector[Boolean] = Vector.fill(9)(true)):
	def serialize(blank: Char = '.'): String =
		this.values.zipWithIndex.map {
			case (value, i) => if value then (i + 1).toString() else blank.toString()
		}.mkString

	def number(): Option[SudokuInt] =
		val candidates = this.values.zipWithIndex.collect { case (true, index) => index + 1 }
		if candidates.length == 1 then Some(candidates.head) else None

	def notes(): Seq[SudokuInt] = this.values.zipWithIndex.collect { case (true, index) => index + 1 }

	def isConsistent(): Boolean = this.values.exists(identity)

	def toggleNote(value: SudokuInt): Cell =
		Cell(this.values.updated(value - 1, !this.values(value - 1)))

object Cell:
	def apply(head: SudokuInt, tail: SudokuInt*): Cell =
		var value = Vector.fill(9)(false)
		for v <- head +: tail.toVector do
			value = value.updated(v - 1, true)
		new Cell(value)
	def unapply(cell: Cell): Seq[SudokuInt] = cell.notes()

	def deserialize(str: String, blank: Char = '.'): Cell =
		require(str.length == 9, "Cell.deserialize requires a string of length 9")
		var value = Vector.fill(9)(false)
		for (char, index) <- str.zipWithIndex do
			if char != blank then
				val v = char.asDigit
				require(v == index + 1, s"Unexpected character in Cell.deserialize: $char")
				value = value.updated(v - 1, true)
		Cell(value)

type Board = Vector[Vector[Cell]]

sealed trait BasicSudokuView:
	def board: Board

	final override def equals(that: Any): Boolean = that match
		case other: BasicSudokuView => this.board == other.board
		case _ => false
	final override def hashCode(): Int = this.board.hashCode()

	final def serialize(blank: Char = '.'): String =
		this.board.map(_.map(_.serialize(blank)).mkString).mkString

	def fillNumber(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): BasicSudokuView

	def isConsistent(): Boolean

	def fixedPositions(): Seq[(SudokuIndex, SudokuIndex, SudokuInt)] =
		for
			row <- 0 until 9
			col <- 0 until 9
			cell = this.board(row)(col)
			number <- cell.number()
		yield (row, col, number)

	def draw(): Unit =
		def cell3x3(row: Int, col: Int): Seq[String] =
			val cell = this.board(row)(col)
			if cell.isConsistent() then
				cell.number() match
					case Some(number) =>
						Seq("   ", s"*$number*", "   ")
					case None =>
						val notes = cell.notes().toSet
						Seq(
							(1 to 3).map(d => if notes.contains(d) then d.toString else ".").mkString,
							(4 to 6).map(d => if notes.contains(d) then d.toString else ".").mkString,
							(7 to 9).map(d => if notes.contains(d) then d.toString else ".").mkString
						)
			else Seq("   ", "ERR", "   ")

		val gap = " " * 3
		val margin = " " * 2
		val segContentWidth = 3 * 3 + 2 * gap.length
		val segWidth = segContentWidth + 2 * margin.length
		val top = s"┌${"─" * segWidth}┬${"─" * segWidth}┬${"─" * segWidth}┐"
		val mid = s"├${"─" * segWidth}┼${"─" * segWidth}┼${"─" * segWidth}┤"
		val bot = s"└${"─" * segWidth}┴${"─" * segWidth}┴${"─" * segWidth}┘"

		def segLine(parts: Seq[String]): String =
			s"$margin${parts.mkString(gap)}$margin"

		def spacerLine(): String =
			s"│${" " * segWidth}│${" " * segWidth}│${" " * segWidth}│"

		println(top)
		for row <- 0 until 9 do
			val cells = (0 until 9).map(col => cell3x3(row, col))
			for subRow <- 0 until 3 do
				val seg0 = segLine(cells.slice(0, 3).map(_(subRow)))
				val seg1 = segLine(cells.slice(3, 6).map(_(subRow)))
				val seg2 = segLine(cells.slice(6, 9).map(_(subRow)))
				println(s"│$seg0│$seg1│$seg2│")
			row match
				case 2 | 5 => println(mid)
				case 8     => println(bot)
				case _     => println(spacerLine())

object BasicSudokuView:
	def deserialize[S <: BasicSudokuView](str: String, blank: Char = '.')(applier: Board => S): S =
		require(str.length == 729, "BasicSudokuView.deserialize requires a string of length 729")
		val board = str.grouped(81).toVector.map { row =>
			row.grouped(9).toVector.map { cellStr =>
				Cell.deserialize(cellStr, blank)
			}
		}
		applier(board)

	def iterUnits(): Iterator[IndexedSeq[(SudokuIndex, SudokuIndex)]] = Iterator(
		// Rows
		for row <- 0 until 9 yield (0 until 9).map(col => (row, col)),
		// Columns
		for col <- 0 until 9 yield (0 until 9).map(row => (row, col)),
		// Boxes
		for boxRow <- 0 until 3; boxCol <- 0 until 3 yield
			for row <- boxRow * 3 until boxRow * 3 + 3; col <- boxCol * 3 until boxCol * 3 + 3 yield
				(row, col)
	).flatten

	def peersOf(row: SudokuIndex, col: SudokuIndex): Set[(SudokuIndex, SudokuIndex)] =
		iterUnits().filter(_.contains((row, col))).flatten.toSet - ((row, col))

case class PartialSudoku(val board: Board = Vector.fill(9)(Vector.fill(9)(Cell()))) extends BasicSudokuView:
	def fillNumber(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): PartialSudoku =
		PartialSudoku(this.board.updated(row, this.board(row).updated(col, Cell(value))))

	def isConsistent(): Boolean =
		(for unit <- PartialSudoku.iterUnits() yield
			val seen = mutable.Set[SudokuInt]()
			for (row, col) <- unit yield
				this.board(row)(col).number() match
					case Some(value) =>
						if seen.contains(value) then false
						else { seen.add(value); true }
					case None => true
		).flatten.forall(identity)

	def rebuildNotes(): Sudoku =
		Sudoku.fromFixedNumbers(this)

object PartialSudoku:
	export BasicSudokuView.{deserialize => _, *}
	def deserialize(str: String, blank: Char = '.'): PartialSudoku =
		BasicSudokuView.deserialize(str, blank)(PartialSudoku.apply)

case class Sudoku(board: Vector[Vector[Cell]]) extends BasicSudokuView:
	def fillNumber(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Sudoku =
		var s = Sudoku(this.board.updated(row, this.board(row).updated(col, Cell(value))), validated = true)
		for (r, c) <- Sudoku.peersOf(row, col) do
			s = s.deleteNote(r, c, value)
		s

	def deleteNote(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Sudoku =
		type Early[A] = Either[Sudoku, A]
		inline def ok(s: Sudoku): Early[Nothing] = Left(s)
		inline def continue[A](a: A): Early[A] = Right(a)
		val res: Early[Sudoku] =
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

	def isConsistent(): Boolean =
		val inCellValid = this.board.flatten.forall(_.isConsistent())
		val crossCellValid =
			Sudoku.iterUnits().forall { unit =>
				val seen = unit.iterator.flatMap { (r, c) => this.board(r)(c).notes() }.toSet
				seen == (1 to 9).toSet
			}
		inCellValid && crossCellValid

	def isCandidate(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Boolean =
		this.board(row)(col).notes().contains(value)

object Sudoku:
	export BasicSudokuView.{deserialize => _, *}
	def apply(): Sudoku = new Sudoku(Vector.fill(9)(Vector.fill(9)(Cell())))
	def apply(board: Board): Sudoku = apply(board, validated = false)
	def apply(board: Board, validated: Boolean = false): Sudoku =
		if validated then new Sudoku(board)
		else
			val sudoku = PartialSudoku(board)
			require(sudoku.isConsistent(), "Invalid board: contains contradictions")
			Sudoku(sudoku.rebuildNotes().board, validated = true)

	def deserialize(str: String, blank: Char = '.'): Sudoku =
		BasicSudokuView.deserialize(str, blank)(Sudoku.apply(_, validated = true))

	def fromFixedNumbers(board: PartialSudoku): Sudoku =
		val fixedNumbers = for
			row <- 0 until 9
			col <- 0 until 9
			cell = board.board(row)(col)
			number <- cell.number()
		yield (row, col, number)
		var sudoku = Sudoku()
		for (row, col, number) <- fixedNumbers do
			sudoku = sudoku.fillNumber(row, col, number)
		sudoku

trait SudokuSolver:
	enum State:
		case InValid
		case Partial(sudoku: Sudoku)
		case Solved(sudoku: Sudoku)
		case MultiSolution
	def solve(sudoku: Sudoku): State

object DLXSolver extends SudokuSolver:
	sealed class Node(var left: Node, var right: Node, var up: Node, var down: Node, var column: ColumnNode)
	final class DataNode(val r: SudokuIndex, val c: SudokuIndex, val n: SudokuInt) extends Node(null, null, null, null, null)
	final class ColumnNode(var size: Int = 0) extends Node(null, null, null, null, null):
		this.left = this
		this.right = this
		this.up = this
		this.down = this
		this.column = this
	def linkLR(left: Node, right: Node): Unit =
		right.right = left.right
		right.left = left
		left.right.left = right
		left.right = right
	def linkUD(col: ColumnNode, node: Node): Unit =
		node.down = col
		node.up = col.up
		col.up.down = node
		col.up = node
		node.column = col
		col.size += 1
	def cover(col: ColumnNode): Unit =
		col.right.left = col.left
		col.left.right = col.right
		var row = col.down
		while row != col do
			var node = row.right
			while node != row do
				node.down.up = node.up
				node.up.down = node.down
				node.column.size -= 1
				node = node.right
			row = row.down
	def uncover(col: ColumnNode): Unit =
		var row = col.up
		while row != col do
			var node = row.left
			while node != row do
				node.column.size += 1
				node.down.up = node
				node.up.down = node
				node = node.left
			row = row.up
		col.right.left = col
		col.left.right = col

	def solve(sudoku: Sudoku): State = 
		val root = new ColumnNode()
		val columns: Vector[ColumnNode] = Vector.fill(324)(new ColumnNode())
		var last: Node = root
		for col <- columns do
			linkLR(last, col)
			last = col

		inline def cellCol(r: Int, c: Int): Int = r * 9 + c
		inline def rowNumCol(r: Int, n: Int): Int = 81 + r * 9 + (n - 1)
		inline def colNumCol(c: Int, n: Int): Int = 162 + c * 9 + (n - 1)
		inline def boxNumCol(r: Int, c: Int, n: Int): Int = 243 + ((r / 3) * 3 + (c / 3)) * 9 + (n - 1)

		def addCandidateRow(r: Int, c: Int, n: Int): Unit =
			val colIdxs = Array(
				cellCol(r, c),
				rowNumCol(r, n),
				colNumCol(c, n),
				boxNumCol(r, c, n)
			)
			val nodes: Array[DataNode] = colIdxs.map { idx =>
				val dn = new DataNode(r, c, n)
				linkUD(columns(idx), dn)
				dn
			}
			nodes(0).left = nodes(0)
			nodes(0).right = nodes(0)
			var rowLast: Node = nodes(0)
			var i = 1
			while i < nodes.length do
				linkLR(rowLast, nodes(i))
				rowLast = nodes(i)
				i += 1

		for
			r <- 0 until 9
			c <- 0 until 9
			n <- sudoku.board(r)(c).notes()
		do addCandidateRow(r, c, n)

		val partial = mutable.ArrayBuffer.empty[Node]
		var solutionCount = 0
		var firstSolution: Vector[(Int, Int, Int)] = Vector.empty

		def chooseMinColumn(): ColumnNode =
			var n: Node = root.right
			var best: ColumnNode = n.asInstanceOf[ColumnNode]
			n = n.right
			while n != root do
				val col = n.asInstanceOf[ColumnNode]
				if col.size < best.size then best = col
				n = n.right
			best

		def search(): Unit =
			if solutionCount > 1 then return

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
			var row: Node = col.down
			while row != col do
				val nextRow = row.down
				partial.addOne(row)

				var node = row.right
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
			var p = PartialSudoku()
			for (r, c, n) <- firstSolution do
				p = p.fillNumber(r, c, n)
			State.Solved(p.rebuildNotes())

object HumanFriendlySolver extends SudokuSolver:
	enum InternalState:
		case InValid
		case Progressed(sudoku: Sudoku)
		case Unchanged(sudoku: Sudoku)
	type Step = Sudoku => InternalState

	extension (step: Step)
		def or(steps: Step*): Step = sudoku =>
			(step +: steps).iterator.map(_(sudoku)).find {
				case InternalState.InValid => true
				case InternalState.Progressed(_) => true
				case InternalState.Unchanged(_) => false
			}.getOrElse(InternalState.Unchanged(sudoku))
		def many(): Step = sudoku =>
			@annotation.tailrec
			def loop(cur: Sudoku, progressed: Boolean): InternalState =
				step(cur) match
					case InternalState.InValid => InternalState.InValid
					case InternalState.Progressed(next) => loop(next, true)
					case InternalState.Unchanged(_) =>
						if progressed then InternalState.Progressed(cur)
						else InternalState.Unchanged(cur)
			loop(sudoku, false)
	def choice(step: Step, steps: Step*): Step = step.or(steps*)

	def identity: Step = InternalState.Unchanged(_)
	def bottomStep: Step = _ => InternalState.InValid
	def topStep: Step = sudoku =>
		DLXSolver.solve(sudoku) match
			case DLXSolver.State.Solved(su) => InternalState.Progressed(su)
			case _                          => InternalState.InValid

	def simpleTechnique: Step = choice(
		hiddenSubset,
		nakedSubset,
		lockedCandidate,
	)
	def mediumTechnique: Step = choice(
		simpleTechnique,
		bottomStep,
	)
	def hardTechnique: Step = choice(
		mediumTechnique,
		bottomStep,
	)
	def uniquenessTechnique: Step = choice(
		bottomStep,
	)

	def solveStepByStep(sudoku: Sudoku): Iterator[InternalState] =
		new Iterator[InternalState]:
			private var cur: Sudoku = sudoku
			private var done = false

			override def hasNext: Boolean = !done
			override def next(): InternalState =
				if done then throw new NoSuchElementException("solved or no progress can be made")
				identity(cur) match
					case s @ InternalState.InValid => done = true; s
					case s @ InternalState.Progressed(next) => cur = next; s
					case s @ InternalState.Unchanged(_) => done = true; s

	def solve(sudoku: Sudoku): State =
		val last = solveStepByStep(sudoku).foldLeft(InternalState.Unchanged(sudoku)) { (_, state) => state }
		last match
			case InternalState.InValid => State.InValid
			case InternalState.Progressed(su) =>
				if su.board.flatten.forall(_.number().isDefined) then State.Solved(su)
				else State.Partial(su)
			case InternalState.Unchanged(su) => State.Partial(su)

	// Steps and their helper functions
	type Links = Map[(SudokuInt, (SudokuIndex, SudokuIndex)), Set[(SudokuInt, (SudokuIndex, SudokuIndex))]]
	extension (map: Iterator[(SudokuInt, Map[(SudokuIndex, SudokuIndex), Set[(SudokuIndex, SudokuIndex)]])])
		def collectToLinks: Links =
			map.flatMap { case (d, maps) => maps.map {
				case (cell, peers) => ((d, cell), peers.map(peer => (d, peer))) }
			}.toMap
	private val candidateCells: Sudoku => Map[SudokuInt, Vector[(SudokuIndex, SudokuIndex)]] =
		cache1 { (sudoku: Sudoku) => (1 to 9).iterator.map { n =>
			val cells = for
				row <- 0 until 9
				col <- 0 until 9
				if sudoku.isCandidate(row, col, n)
			yield (row, col)
			n -> cells.toVector
		}.toMap }
	private val singleDigitStrongLinks: Sudoku => Links =
		cache1 { (sudoku: Sudoku) => (1 to 9).iterator.map { n =>
			var links = Map.empty[(SudokuIndex, SudokuIndex), Set[(SudokuIndex, SudokuIndex)]]
			def addLink(cell1: (SudokuIndex, SudokuIndex), cell2: (SudokuIndex, SudokuIndex)): Unit =
				links = links.updated(cell1, links.getOrElse(cell1, Set.empty) + cell2)
				links = links.updated(cell2, links.getOrElse(cell2, Set.empty) + cell1)
			for (row, col) <- candidateCells(sudoku)(n) do
				val peers = Sudoku.peersOf(row, col).filter { case (pr, pc) => sudoku.isCandidate(pr, pc, n) }
				if peers.size == 1 then addLink((row, col), peers.head)
			n -> links
		}.collectToLinks }
	private val singleDigitWeakLinks: Sudoku => Links =
		cache1 { (sudoku: Sudoku) => (1 to 9).iterator.map { n =>
			var links = Map.empty[(SudokuIndex, SudokuIndex), Set[(SudokuIndex, SudokuIndex)]]
			def addLink(cell1: (SudokuIndex, SudokuIndex), cell2: (SudokuIndex, SudokuIndex)): Unit =
				links = links.updated(cell1, links.getOrElse(cell1, Set.empty) + cell2)
				links = links.updated(cell2, links.getOrElse(cell2, Set.empty) + cell1)
			for (row, col) <- candidateCells(sudoku)(n) do
				val peers = Sudoku.peersOf(row, col).filter { case (pr, pc) => sudoku.isCandidate(pr, pc, n) }
				peers.foreach(peer => addLink((row, col), peer))
			n -> links
		}.collectToLinks }
	
	def hiddenSubset: Step = sudoku =>
		/// Hidden Subset is a technique that identifies a subset of k digits that only appear in k cells within a unit (row, column, or box).
		/// If such a pattern is found, those k cells must contain those k digits, and any other candidates can be removed from those cells.
		val result: Option[Sudoku] =
			(for
				// k can be at most 9 // 2
				k <- (1 to 4).iterator
				unit <- Sudoku.iterUnits()
				digits2CellsInUnit = (1 to 9).iterator.map { case n =>
					n -> unit.filter { case (r, c) => sudoku.isCandidate(r, c, n) }.toSet
				}.toMap
				if digits2CellsInUnit.size >= k
				ds <- (1 to 9).combinations(k)
				cells = ds.iterator.flatMap(digits2CellsInUnit).toSet
				if cells.size == k
				toRemove = cells.iterator.flatMap { case (r, c) =>
					sudoku.board(r)(c).notes().filterNot(ds.contains).map((r, c, _))
				}.toVector
				if toRemove.nonEmpty
			yield
				toRemove.foldLeft(sudoku) { case (su, (r, c, n)) => su.deleteNote(r, c, n) }
			).nextOption
		result match
			case Some(res) =>
				if res.isConsistent() then InternalState.Progressed(res) else InternalState.InValid
			case None =>
				InternalState.Unchanged(sudoku)

	def nakedSubset: Step = sudoku =>
		/// Naked Subset is a technique that identifies a subset of k cells within a unit (row, column, or box) that contain only k candidates in total.
		/// If such a pattern is found, those k candidates must be placed in those k cells, and any other candidates can be removed from those cells.
		val result: Option[Sudoku] =
			(for
				// k can be at most 9 // 2
				// k == 1 (which is naked single) is already handled by Cell
				k <- (2 to 4).iterator
				unit <- Sudoku.iterUnits()
				cells = unit.filter { case (r, c) =>
					val cell = sudoku.board(r)(c)
					cell.number().isEmpty && (1 to k).contains(cell.notes().size)
				}
				if cells.size >= k
				cs <- cells.combinations(k)
				unionDigits = cs.iterator.flatMap { case (r, c) => sudoku.board(r)(c).notes() }.toSet
				if unionDigits.size == k
				toRemove = unit.iterator.filterNot(cs.toSet.contains)
					.flatMap { case (r, c) =>
						sudoku.board(r)(c).notes().filter(unionDigits.contains).map((r, c, _))
					}.toVector
				if toRemove.nonEmpty
			yield
				toRemove.foldLeft(sudoku) { case (su, (r, c, n)) => su.deleteNote(r, c, n) }
			).nextOption
		result match
			case Some(res) =>
				if res.isConsistent() then InternalState.Progressed(res) else InternalState.InValid
			case None =>
				InternalState.Unchanged(sudoku)

	def lockedCandidate: Step = sudoku =>
		/// Locked Candidate is a technique that identifies a candidate digit that is confined to a single row or column within a box.
		/// If such a pattern is found, it can be removed from the corresponding row or column outside of that box.
		val result: Option[Sudoku] =
			(for
				boxRow <- (0 until 3).iterator
				boxCol <- (0 until 3).iterator
				boxCells =
					(for
						r <- boxRow * 3 until boxRow * 3 + 3
						c <- boxCol * 3 until boxCol * 3 + 3
					yield (r, c)).toVector
				n <- (1 to 9).iterator
				candsInBox = boxCells.filter { case (r, c) => sudoku.isCandidate(r, c, n) }
				if candsInBox.nonEmpty
				rows = candsInBox.iterator.map(_._1).toSet
				cols = candsInBox.iterator.map(_._2).toSet

				rowElims =
					if rows.size == 1 then
						val r = rows.head
						(0 until 9).iterator
							.filter(c => c < boxCol * 3 || c >= boxCol * 3 + 3) // 宫外
							.filter { c =>
								sudoku.board(r)(c).number().isEmpty && sudoku.isCandidate(r, c, n)
							}
							.map(c => (r, c, n))
					else Iterator.empty

				colElims =
					if cols.size == 1 then
						val c = cols.head
						(0 until 9).iterator
							.filter(r => r < boxRow * 3 || r >= boxRow * 3 + 3) // 宫外
							.filter { r =>
								sudoku.board(r)(c).number().isEmpty && sudoku.isCandidate(r, c, n)
							}
							.map(r => (r, c, n))
					else Iterator.empty

				toRemove = (rowElims ++ colElims).toVector
				if toRemove.nonEmpty
			yield
				toRemove.foldLeft(sudoku) { case (su, (r, c, d)) => su.deleteNote(r, c, d) }
			).nextOption
		result match
			case Some(res) => if res.isConsistent() then InternalState.Progressed(res) else InternalState.InValid
			case None      => InternalState.Unchanged(sudoku)

final class SudokuGenerator(var seed: Option[Long] = None):
	enum Difficulty:
		case Easy, Medium, Hard, Impossible

	private val random = seed match
		case Some(seed) => new Random(seed)
		case None       => new Random()

	private def isSolved(sudoku: Sudoku): Boolean =
		DLXSolver.solve(sudoku) match
			case DLXSolver.State.Solved(_) => true
			case _                         => false

	private def generateUniquePuzzle(): Sudoku =
		val positions = (for r <- 0 until 9; c <- 0 until 9 yield (r, c)).toVector

		@annotation.tailrec
		def loop(su: Sudoku, remaining: Vector[(Int, Int)]): Sudoku =
			if isSolved(su) then su
			else remaining match
				case (r, c) +: tail =>
					if su.board(r)(c).number().isDefined then loop(su, tail)
					else
						val candidates = su.board(r)(c).notes()
						if candidates.isEmpty then
							throw new IllegalStateException(s"Internal Sudoku state error: cell at ($r, $c) has no candidates")
						else
							val n = candidates(random.nextInt(candidates.length))
							val next = su.fillNumber(r, c, n)
							val kept = if next.isConsistent() then next else su
							loop(kept, tail)
				case _ =>
					generateUniquePuzzle()

		loop(Sudoku(), random.shuffle(positions).take(36).toVector)

	private def furtherRemoveGivens(sudoku: Sudoku): Sudoku =
		random
			.shuffle(sudoku.fixedPositions())
			.foldLeft(sudoku) { case (su, (r, c, _)) =>
				val removed = Sudoku(su.board.updated(r, su.board(r).updated(c, Cell())))
				if !isSolved(removed) then su else removed
			}

	private def canBeSolvedBy(step: HumanFriendlySolver.Step)(sudoku: Sudoku): Boolean =
		step.many()(sudoku) match
			case HumanFriendlySolver.InternalState.InValid => false
			case HumanFriendlySolver.InternalState.Progressed(_) => true
			case HumanFriendlySolver.InternalState.Unchanged(_) => false

	@annotation.tailrec
	def generate(difficulty: Difficulty = Difficulty.Easy): Sudoku =
		val initPuzzle = generateUniquePuzzle()
		val maybePuzzle = furtherRemoveGivens(initPuzzle)
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
