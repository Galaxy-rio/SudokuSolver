package libsudoku

import scala.language.strictEquality
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

case class SudokuInt(value: Int) extends AnyVal derives CanEqual:
	override def toString(): String = value.toString
object SudokuInt:
	def valid(value: Int): Boolean = (1 to 9).contains(value)
	implicit def fromInt(value: Int): SudokuInt =
		require(valid(value))
		SudokuInt(value)
	implicit def toInt(sudokuInt: SudokuInt): Int = sudokuInt.value
	def iter() = (1 to 9).map(SudokuInt(_))

case class SudokuIndex(value: Int) extends AnyVal derives CanEqual:
	override def toString() = value.toString
object SudokuIndex:
	def valid(value: Int): Boolean = (0 until 9).contains(value)
	implicit def fromInt(value: Int): SudokuIndex =
		require(valid(value))
		SudokuIndex(value)
	implicit def toInt(sudokuIndex: SudokuIndex): Int = sudokuIndex.value
	def iter() = (0 until 9).map(SudokuIndex(_))

case class CellNotes(values: Vector[Boolean]):
	require(values.length == 9, "CellNotes must have exactly 9 boolean values")

	def serialize(blank: Char = '.'): String =
		this.values.zipWithIndex.map {
			case (value, i) => if value then (i + 1).toString() else blank.toString()
		}.mkString

	def toggleNote(value: SudokuInt): CellNotes =
		this.copy(values = this.values.updated(value - 1, !this.values(value - 1)))

object CellNotes:
	def apply(candidates: SudokuInt*): CellNotes =
		var value = Vector.fill(9)(false)
		for v <- candidates do
			value = value.updated(v - 1, true)
		CellNotes(value)
	def unapply(cellNotes: CellNotes): Option[Seq[SudokuInt]] =
		val notes: Seq[SudokuInt] = cellNotes.values.zipWithIndex.collect { case (true, index) => index + 1 }
		if notes.nonEmpty then Some(notes) else None

	def deserialize(str: String, blank: Char = '.'): CellNotes =
		require(str.length == 9, "CellNotes.deserialize requires a string of length 9")
		var value = Vector.fill(9)(false)
		for (char, index) <- str.zipWithIndex do
			if char != blank then
				val v = char.asDigit
				require(v == index + 1, s"Unexpected character in CellNotes.deserialize: $char")
				value = value.updated(v - 1, true)
		CellNotes(value)

enum Cell derives CanEqual:
	case Fixed(value: SudokuInt)
	case Notes(values: CellNotes)

	def serialize(blank: Char = '.'): String = this match
		case Fixed(value) => CellNotes(value).serialize(blank)
		case Notes(values) => values.serialize(blank)
	override def toString(): String = this match
		case Fixed(_) => "F" + this.serialize()
		case Notes(_) => "N" + this.serialize()

	def isFixed(): Boolean = this match
		case Fixed(_) => true
		case Notes(_) => false

	def number(): Option[SudokuInt] = this match
		case Fixed(value) => Some(value)
		case Notes(_)     => None

	def candidates(): Seq[SudokuInt] = this match
		case Fixed(value)            => Seq(value)
		case Notes(CellNotes(notes)) => notes
		case Notes(_)                => Seq.empty

	def isConsistent(): Boolean = this match
		case Fixed(_)     => true
		case Notes(notes) => notes.values.exists(identity)

	def toggleNote(value: SudokuInt): Cell = this match
		case Fixed(fixed) => if fixed == value then Cell.invalid else this
		case Notes(notes) => Notes(notes.toggleNote(value))

	def reduce(): Cell = this match
		case Fixed(_)                      => this
		case Notes(CellNotes(Seq(single))) => Fixed(single)
		case Notes(_)                      => this

object Cell:
	def deserialize(str: String, blank: Char = '.'): Cell =
		require(str.length == 9, "Cell.deserialize requires a string of length 9")
		var value = Vector.fill(9)(false)
		for (char, index) <- str.zipWithIndex do
			if char != blank then
				val v = char.asDigit
				require(v == index + 1, s"Unexpected character in Cell.deserialize: $char")
				value = value.updated(v - 1, true)
		Notes(CellNotes(value)).reduce()
	def fromString(str: String): Cell =
		require(str.length == 10, "Cell.fromString requires a string of length 10")
		str.head match
			case 'F' => Cell.deserialize(str.tail)
			case 'N' => Cell.deserialize(str.tail)
			case other => throw new IllegalArgumentException(s"Unexpected prefix in Cell.fromString: $other")

	def invalid: Cell = Notes(CellNotes(Vector.fill(9)(false)))
	def blank: Cell = Notes(CellNotes(Vector.fill(9)(true)))

type Board = Vector[Vector[Cell]]

sealed trait BasicSudokuView:
	def board: Board

	final override def equals(that: Any): Boolean = that match
		case other: BasicSudokuView => this.board == other.board
		case _ => false
	final override def hashCode(): Int = this.board.hashCode()

	final def serialize(blank: Char = '.'): String =
		this.board.flatten.map(_.serialize(blank)).mkString
	override final def toString(): String = this.board.flatten.map(_.toString()).mkString("|")

	def fillNumber(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): BasicSudokuView

	def deleteNote(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): BasicSudokuView

	def isConsistent(): Boolean

	def isBoardComplete(): Boolean = this.board.flatten.forall(_.isFixed())

	def fixedPositions(): Seq[(SudokuIndex, SudokuIndex, SudokuInt)] =
		for
			row <- SudokuIndex.iter()
			col <- SudokuIndex.iter()
			cell = this.board(row)(col)
			number <- cell.number()
		yield (row, col, number)

	def draw(): Unit =
		def cell3x3(row: Int, col: Int): Seq[String] =
			this.board(row)(col) match
				case Cell.Fixed(number) =>
					Seq("   ", s"*$number*", "   ")
				case Cell.Notes(CellNotes(notes)) =>
					Seq(
						(1 to 3).map(SudokuInt.fromInt).map(d => if notes.toSet.contains(d) then d.toString else ".").mkString,
						(4 to 6).map(SudokuInt.fromInt).map(d => if notes.toSet.contains(d) then d.toString else ".").mkString,
						(7 to 9).map(SudokuInt.fromInt).map(d => if notes.toSet.contains(d) then d.toString else ".").mkString
					)
				case Cell.Notes(_) => Seq("   ", "ERR", "   ")

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
		for row <- SudokuIndex.iter() do
			val cells = SudokuIndex.iter().map(col => cell3x3(row, col))
			for subRow <- 0 until 3 do
				val seg0 = segLine(cells.slice(0, 3).map(_(subRow)))
				val seg1 = segLine(cells.slice(3, 6).map(_(subRow)))
				val seg2 = segLine(cells.slice(6, 9).map(_(subRow)))
				println(s"│$seg0│$seg1│$seg2│")
			row.value match
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
	def fromString[S <: BasicSudokuView](str: String)(applier: Board => S): S =
		require(str.length == 890, "BasicSudokuView.fromString requires a string of length 890")
		val board = str.split("\\|").grouped(9).map { row =>
			row.map { cellStr =>
				Cell.fromString(cellStr)
			}.toVector
		}.toVector
		applier(board)

	def iterUnits(): IndexedSeq[IndexedSeq[(SudokuIndex, SudokuIndex)]] =
		def idx = SudokuIndex.fromInt(_)
		val rows = for row <- SudokuIndex.iter() yield SudokuIndex.iter().map(col => (row, col))
		val cols = for col <- SudokuIndex.iter() yield SudokuIndex.iter().map(row => (row, col))
		val boxes = for boxRow <- 0 until 3; boxCol <- 0 until 3 yield
			for row <- boxRow * 3 until boxRow * 3 + 3; col <- boxCol * 3 until boxCol * 3 + 3 yield
				(idx(row), idx(col))
		rows ++ cols ++ boxes

	def peersOf(row: SudokuIndex, col: SudokuIndex): Set[(SudokuIndex, SudokuIndex)] =
		iterUnits().filter(_.contains((row, col))).flatten.toSet - ((row, col))

case class Sudoku(board: Board = Vector.fill(9)(Vector.fill(9)(Cell.invalid))) extends BasicSudokuView:
	def fillNumber(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Sudoku =
		setCell(row, col, Cell.Fixed(value))

	def toggleNote(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Sudoku =
		setCell(row, col, this.board(row)(col).toggleNote(value))

	def deleteNote(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Sudoku =
		if this.board(row)(col).candidates().contains(value) then
			toggleNote(row, col, value)
		else this

	def setCell(row: SudokuIndex, col: SudokuIndex, value: Cell): Sudoku =
		Sudoku(this.board.updated(row, this.board(row).updated(col, value)))

	def isConsistent(): Boolean =
		(for unit <- Sudoku.iterUnits() yield
			val seen = mutable.Set[SudokuInt]()
			for (row, col) <- unit yield
				this.board(row)(col).number() match
					case Some(value) =>
						if seen.contains(value) then false
						else { seen.add(value); true }
					case None => true
		).flatten.forall(identity)

	def rebuildNotes(): Sukaku =
		Sukaku.fromFixedNumbers(this)

object Sudoku:
	export BasicSudokuView.{deserialize => _, fromString => _, *}
	def apply(sudoku: Sukaku): Sudoku = Sudoku(sudoku.board)
	def deserialize(str: String, blank: Char = '.'): Sudoku =
		BasicSudokuView.deserialize(str, blank)(Sudoku.apply)
	def fromString(str: String): Sudoku =
		BasicSudokuView.fromString(str)(Sudoku.apply)

case class Sukaku(board: Vector[Vector[Cell]]) extends BasicSudokuView:
	def fillNumber(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Sukaku =
		var s = Sukaku(this.board.updated(row, this.board(row).updated(col, Cell.Fixed(value))), validated = true)
		for (r, c) <- Sukaku.peersOf(row, col) do
			s = s.deleteNote(r, c, value)
		s

	def deleteNote(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Sukaku =
		val cell = this.board(row)(col)
		if !cell.candidates().contains(value) then this
		else cell.toggleNote(value).reduce() match
			case Cell.Fixed(newValue) =>
				this.fillNumber(row, col, newValue)
			case newCell: Cell.Notes =>
				Sukaku(this.board.updated(row, this.board(row).updated(col, newCell)), validated = true)

	def isConsistent(): Boolean =
		val inCellValid = this.board.flatten.forall(_.isConsistent())
		val crossCellValid =
			Sukaku.iterUnits().forall { unit =>
				val seen = unit.iterator.flatMap { (r, c) => this.board(r)(c).candidates() }.toSet
				seen == SudokuInt.iter().toSet
			}
		inCellValid && crossCellValid

	def isCandidate(row: SudokuIndex, col: SudokuIndex, value: SudokuInt): Boolean =
		this.board(row)(col) match
			case Cell.Fixed(_)                => false
			case Cell.Notes(CellNotes(notes)) => notes.contains(value)
			case Cell.Notes(_)                => false

object Sukaku:
	export BasicSudokuView.{deserialize => _, *}
	def apply(): Sukaku = new Sukaku(Vector.fill(9)(Vector.fill(9)(Cell.blank)))
	def apply(board: Board): Sukaku = apply(board, validated = false)
	def apply(board: Board, validated: Boolean = false): Sukaku =
		if validated then new Sukaku(board)
		else
			val sudoku = Sudoku(board)
			require(sudoku.isConsistent(), "Invalid board: contains contradictions")
			Sukaku(sudoku.rebuildNotes().board, validated = true)
	def deserialize(str: String, blank: Char = '.'): Sukaku =
		BasicSudokuView.deserialize(str, blank)(Sukaku.apply(_, validated = true))
	def fromString(str: String): Sukaku =
		BasicSudokuView.fromString(str)(Sukaku.apply(_, validated = true))

	def fromFixedNumbers(board: Sudoku): Sukaku =
		val fixedNumbers = for
			row <- SudokuIndex.iter()
			col <- SudokuIndex.iter()
			cell = board.board(row)(col)
			number <- cell.number()
		yield (row, col, number)
		var sudoku = Sukaku()
		for (row, col, number) <- fixedNumbers do
			sudoku = sudoku.fillNumber(row, col, number)
		sudoku

trait SudokuSolver:
	enum State derives CanEqual:
		case InValid
		case Partial(sudoku: Sukaku)
		case Solved(sudoku: Sukaku)
		case MultiSolution

		def fold[A](ifInvalid: => A, ifPartial: Sukaku => A, ifSolved: Sukaku => A, ifMulti: => A): A =
			this match
				case State.InValid => ifInvalid
				case State.Partial(sudoku) => ifPartial(sudoku)
				case State.Solved(sudoku) => ifSolved(sudoku)
				case State.MultiSolution => ifMulti

	def solve(sudoku: Sukaku): State

object DLXSolver extends SudokuSolver:
	sealed class Node(var left: Node, var right: Node, var up: Node, var down: Node, var column: ColumnNode) derives CanEqual
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

	def solve(sudoku: BasicSudokuView): State =
		sudoku match
			case su: Sukaku => solve(su)
			case su: Sudoku => solve(Sukaku.fromFixedNumbers(su))
	def solve(sudoku: Sukaku): State =
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
			n <- sudoku.board(r)(c).candidates()
		do addCandidateRow(r, c, n)

		val partial = mutable.ArrayBuffer.empty[Node]
		var solutionCount = 0
		var firstSolution: Vector[(SudokuIndex, SudokuIndex, SudokuInt)] = Vector.empty

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
			var p = Sudoku()
			for (r, c, n) <- firstSolution do
				p = p.fillNumber(r, c, n)
			State.Solved(p.rebuildNotes())

object HumanFriendlySolver extends SudokuSolver:
	enum InternalState derives CanEqual:
		case InValid
		case Progressed(sudoku: Sukaku)
		case Unchanged(sudoku: Sukaku)
	type Step = Sukaku => InternalState

	extension (step: Step)
		def or(steps: Step*): Step = sudoku =>
			(step +: steps).iterator.map(_(sudoku)).find {
				case InternalState.InValid => true
				case InternalState.Progressed(_) => true
				case InternalState.Unchanged(_) => false
			}.getOrElse(InternalState.Unchanged(sudoku))
		def many(): Step = sudoku =>
			@annotation.tailrec
			def loop(cur: Sukaku, progressed: Boolean): InternalState =
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

	def solveStepByStep(sudoku: Sukaku): Iterator[InternalState] =
		new Iterator[InternalState]:
			private var cur: Sukaku = sudoku
			private var done = false

			override def hasNext: Boolean = !done
			override def next(): InternalState =
				if done then throw new NoSuchElementException("solved or no progress can be made")
				identity(cur) match
					case s @ InternalState.InValid => done = true; s
					case s @ InternalState.Progressed(next) => cur = next; s
					case s @ InternalState.Unchanged(_) => done = true; s

	def solve(sudoku: Sukaku): State =
		val last = solveStepByStep(sudoku).foldLeft(InternalState.Unchanged(sudoku)) { (_, state) => state }
		last match
			case InternalState.InValid => State.InValid
			case InternalState.Progressed(su) =>
				if su.isBoardComplete() then State.Solved(su)
				else State.Partial(su)
			case InternalState.Unchanged(su) => State.Partial(su)

	// Steps and their helper functions
	type Links = Map[(SudokuInt, (SudokuIndex, SudokuIndex)), Set[(SudokuInt, (SudokuIndex, SudokuIndex))]]
	extension (map: Iterator[(SudokuInt, Map[(SudokuIndex, SudokuIndex), Set[(SudokuIndex, SudokuIndex)]])])
		def collectToLinks: Links =
			map.flatMap { case (d, maps) => maps.map {
				case (cell, peers) => ((d, cell), peers.map(peer => (d, peer))) }
			}.toMap
	private val candidateCells: Sukaku => Map[SudokuInt, Vector[(SudokuIndex, SudokuIndex)]] =
		cache1 { (sudoku: Sukaku) => SudokuInt.iter().iterator.map { n =>
			val cells = for
				row <- SudokuIndex.iter()
				col <- SudokuIndex.iter()
				if sudoku.isCandidate(row, col, n)
			yield (row, col)
			n -> cells.toVector
		}.toMap }
	private val singleDigitStrongLinks: Sukaku => Links =
		cache1 { (sudoku: Sukaku) => SudokuInt.iter().iterator.map { n =>
			var links = Map.empty[(SudokuIndex, SudokuIndex), Set[(SudokuIndex, SudokuIndex)]]
			def addLink(cell1: (SudokuIndex, SudokuIndex), cell2: (SudokuIndex, SudokuIndex)): Unit =
				links = links.updated(cell1, links.getOrElse(cell1, Set.empty) + cell2)
				links = links.updated(cell2, links.getOrElse(cell2, Set.empty) + cell1)
			for (row, col) <- candidateCells(sudoku)(n) do
				val peers = Sukaku.peersOf(row, col).filter { case (pr, pc) => sudoku.isCandidate(pr, pc, n) }
				if peers.size == 1 then addLink((row, col), peers.head)
			n -> links
		}.collectToLinks }
	private val singleDigitWeakLinks: Sukaku => Links =
		cache1 { (sudoku: Sukaku) => SudokuInt.iter().iterator.map { n =>
			var links = Map.empty[(SudokuIndex, SudokuIndex), Set[(SudokuIndex, SudokuIndex)]]
			def addLink(cell1: (SudokuIndex, SudokuIndex), cell2: (SudokuIndex, SudokuIndex)): Unit =
				links = links.updated(cell1, links.getOrElse(cell1, Set.empty) + cell2)
				links = links.updated(cell2, links.getOrElse(cell2, Set.empty) + cell1)
			for (row, col) <- candidateCells(sudoku)(n) do
				val peers = Sukaku.peersOf(row, col).filter { case (pr, pc) => sudoku.isCandidate(pr, pc, n) }
				peers.foreach(peer => addLink((row, col), peer))
			n -> links
		}.collectToLinks }

	def hiddenSubset: Step = sudoku =>
		/// Hidden Subset is a technique that identifies a subset of k digits that only appear in k cells within a unit (row, column, or box).
		/// If such a pattern is found, those k cells must contain those k digits, and any other candidates can be removed from those cells.
		val result: Option[Sukaku] =
			(for
				// k can be at most 9 // 2
				k <- (1 to 4).iterator
				unit <- Sukaku.iterUnits()
				digits2CellsInUnit = SudokuInt.iter().iterator.map { case n =>
					n -> unit.filter { case (r, c) => sudoku.isCandidate(r, c, n) }.toSet
				}.filter { case (_, cells) => cells.nonEmpty }.toMap
				if digits2CellsInUnit.size >= k
				ds <- digits2CellsInUnit.keysIterator.toVector.combinations(k)
				cells = ds.iterator.flatMap(digits2CellsInUnit).toSet
				if cells.size == k
				toRemove = cells.iterator.flatMap { case (r, c) =>
					sudoku.board(r)(c).candidates().filterNot(ds.contains).map((r, c, _))
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
		val result: Option[Sukaku] =
			(for
				// k can be at most 9 // 2
				// k == 1 (which is naked single) is already handled by Cell
				k <- (2 to 4).iterator
				unit <- Sukaku.iterUnits()
				cells = unit.filter { case (r, c) =>
					(2 to k).contains(sudoku.board(r)(c).candidates().size)
				}
				if cells.size >= k
				cs <- cells.combinations(k)
				unionDigits = cs.iterator.flatMap { case (r, c) => sudoku.board(r)(c).candidates() }.toSet
				if unionDigits.size == k
				toRemove = unit.iterator.filterNot(cs.toSet.contains)
					.flatMap { case (r, c) =>
						sudoku.board(r)(c).candidates().filter(unionDigits.contains).map((r, c, _))
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
		val result: Option[Sukaku] =
			(for
				boxRow <- (0 until 3).iterator
				boxCol <- (0 until 3).iterator
				boxCells =
					(for
						r <- boxRow * 3 until boxRow * 3 + 3
						c <- boxCol * 3 until boxCol * 3 + 3
					yield (SudokuIndex.fromInt(r), SudokuIndex.fromInt(c))).toVector
				n <- SudokuInt.iter().iterator
				candsInBox = boxCells.filter { case (r, c) => sudoku.isCandidate(r, c, n) }
				if candsInBox.nonEmpty
				rows = candsInBox.iterator.map(_._1).toSet
				cols = candsInBox.iterator.map(_._2).toSet

				rowElims =
					if rows.size == 1 then
						val r = rows.head
						SudokuIndex.iter().iterator
							.filter(c => c < boxCol * 3 || c >= boxCol * 3 + 3)
							.filter { sudoku.isCandidate(r, _, n) }
							.map(c => (r, c, n))
					else Iterator.empty

				colElims =
					if cols.size == 1 then
						val c = cols.head
						SudokuIndex.iter().iterator
							.filter(r => r < boxRow * 3 || r >= boxRow * 3 + 3)
							.filter { sudoku.isCandidate(_, c, n) }
							.map(r => (r, c, n))
					else Iterator.empty

				toRemove = (rowElims ++ colElims).toVector
				if toRemove.nonEmpty
			yield
				toRemove.foldLeft(sudoku) { case (su, (r, c, d)) => su.deleteNote(r, c, d) }
			).nextOption
		result match
			case Some(res)
				=> if res.isConsistent() then InternalState.Progressed(res) else InternalState.InValid
			case None
				=> InternalState.Unchanged(sudoku)

final class SudokuGenerator(var seed: Option[Long] = None):
	enum Difficulty derives CanEqual:
		case Random, Easy, Medium, Hard, Impossible

	private val random = seed match
		case Some(seed) => new Random(seed)
		case None       => new Random()

	private def isSolved(sudoku: BasicSudokuView): Boolean =
		DLXSolver.solve(sudoku) match
			case DLXSolver.State.Solved(_) => true
			case _                         => false

	private def generateUniquePuzzle(): Sukaku =
		val positions = (for r <- SudokuIndex.iter(); c <- SudokuIndex.iter() yield (r, c)).toVector

		@annotation.tailrec
		def loop(su: Sukaku, remaining: Vector[(SudokuIndex, SudokuIndex)]): Sukaku =
			if isSolved(su) then su
			else remaining match
				case (r, c) +: tail =>
					if su.board(r)(c).isFixed() then loop(su, tail)
					else
						val candidates = su.board(r)(c).candidates()
						if candidates.isEmpty then
							throw new IllegalStateException(s"Internal Sudoku state error: cell at ($r, $c) has no candidates")
						else
							val n = candidates(random.nextInt(candidates.length))
							val next = su.fillNumber(r, c, n)
							val kept = if next.isConsistent() then next else su
							loop(kept, tail)
				case _ =>
					generateUniquePuzzle()

		loop(Sukaku(), random.shuffle(positions).take(36).toVector)

	private def clearNotes(sudoku: Sukaku): Sudoku =
		Sudoku(sudoku.board.map(_.map {
			case c: Cell.Fixed => c
			case _: Cell.Notes => Cell.blank
		}))

	private def furtherRemoveGivens(sudoku: Sukaku): Sudoku =
		var su = clearNotes(sudoku)
		random
			.shuffle(su.fixedPositions())
			.foldLeft(su) { case (su, (r, c, _)) =>
				val removed = Sudoku(su.board.updated(r, su.board(r).updated(c, Cell.invalid)))
				if !isSolved(removed) then su else removed
			}

	private def canBeSolvedBy(step: HumanFriendlySolver.Step)(sudoku: Sukaku): Boolean =
		step.many()(sudoku) match
			case HumanFriendlySolver.InternalState.InValid => false
			case HumanFriendlySolver.InternalState.Progressed(_) => true
			case HumanFriendlySolver.InternalState.Unchanged(_) => false

	@annotation.tailrec
	def generate(difficulty: Difficulty = Difficulty.Easy): Sudoku =
		val initPuzzle = generateUniquePuzzle()
		val maybePuzzle = furtherRemoveGivens(initPuzzle)
		val sukakuPuzzle = Sukaku.fromFixedNumbers(maybePuzzle)
		difficulty match
			case Difficulty.Random => maybePuzzle
			case Difficulty.Easy =>
				if canBeSolvedBy(HumanFriendlySolver.simpleTechnique)(sukakuPuzzle)
				then maybePuzzle else generate(difficulty)
			case Difficulty.Medium =>
				if canBeSolvedBy(HumanFriendlySolver.mediumTechnique)(sukakuPuzzle) && !canBeSolvedBy(HumanFriendlySolver.simpleTechnique)(sukakuPuzzle)
				then maybePuzzle else generate(difficulty)
			case Difficulty.Hard =>
				if canBeSolvedBy(HumanFriendlySolver.hardTechnique)(sukakuPuzzle) && !canBeSolvedBy(HumanFriendlySolver.mediumTechnique)(sukakuPuzzle)
				then maybePuzzle else generate(difficulty)
			case Difficulty.Impossible =>
				if !canBeSolvedBy(HumanFriendlySolver.hardTechnique)(sukakuPuzzle)
				then maybePuzzle else generate(difficulty)
