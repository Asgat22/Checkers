import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.swing.Swing
import kotlinx.datetime.*
import org.jetbrains.skija.*
import org.jetbrains.skiko.SkiaLayer
import org.jetbrains.skiko.SkiaRenderer
import org.jetbrains.skiko.SkiaWindow
import java.awt.Dimension
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import java.awt.event.MouseListener
import java.lang.String.format
import javax.swing.WindowConstants
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.time.ExperimentalTime


val blacks: MutableList<Pair<Int, Int>> = mutableListOf(
    Pair(0, 1),
    Pair(1, 0),
    Pair(1, 2),
    Pair(2, 1),
    Pair(3, 0),
    Pair(3, 2),
    Pair(4, 1),
    Pair(5, 0),
    Pair(5, 2),
    Pair(6, 1),
    Pair(7, 0),
    Pair(7, 2)
)
val whites: MutableList<Pair<Int, Int>> = mutableListOf(
    Pair(0, 7),
    Pair(0, 5),
    Pair(1, 6),
    Pair(2, 7),
    Pair(2, 5),
    Pair(3, 6),
    Pair(4, 7),
    Pair(4, 5),
    Pair(5, 6),
    Pair(6, 7),
    Pair(6, 5),
    Pair(7, 6)
)
val whiteQueens: MutableList<Pair<Int, Int>> = mutableListOf()
val blackQueens: MutableList<Pair<Int, Int>> = mutableListOf()

//var blacks : MutableList<Pair<Int, Int>> = mutableListOf(Pair(2, 1), Pair(5, 2))
//var whites : MutableList<Pair<Int, Int>> = mutableListOf(Pair(1, 2), Pair(1, 4), Pair(1, 6), Pair(0, 7), Pair(2, 7), Pair(4, 7))


var current: String = "white"
var currentChecker: Pair<Int, Int> = Pair(-100, -100)
var isFinish: Boolean = false
var bots : List<String> = listOf("black")

var currentEated = false
var contentScale: Float = 0.0f
var w: Int = 0
var h: Int = 0

var centerX: Float = 0.0f
var centerY: Float = 0.0f
var boardWidth: Float = 0.0f

fun main() {
    createWindow("Русские шашки")
}

fun createWindow(title: String) = runBlocking(Dispatchers.Swing) {
    val window = SkiaWindow()
    window.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
    window.title = title

    window.layer.renderer = Renderer(window.layer)
    window.layer.addMouseMotionListener(MouseMotionAdapter)
    window.layer.addMouseListener(MouseListener)

    window.preferredSize = Dimension(800, 600)
    window.minimumSize = Dimension(100, 100)
    window.pack()
    window.layer.awaitRedraw()
    window.isVisible = true
}

class Renderer(val layer: SkiaLayer) : SkiaRenderer {
    val typeface = Typeface.makeFromFile("fonts/JetBrainsMono-Regular.ttf")
    val font = Font(typeface, 40f)
    val paint = Paint().apply {
        color = 0xff9BC730L.toInt()
        mode = PaintMode.FILL
        strokeWidth = 1f
    }
    val fillWhiteCell = Paint().apply {
        color = 0xFFEED5A7.toInt()
    }
    val fillBlackCell = Paint().apply {
        color = 0xFF8C4415.toInt()
    }
    val fillFond = Paint().apply {
        color = 0xFF878e95.toInt()
    }
    val whiteCheckerPaint = Paint().apply {
        color = 0xFFFFFFFF.toInt()
    }
    val blackCheckerPaint = Paint().apply {
        color = 0xFF000000.toInt()
    }
    val fillSymbols = Paint().apply {
        color = 0xFFE6DCCE.toInt()
    }
    val fillCurrentCell = Paint().apply {
        color = 0xFFff80ff.toInt()
    }
    val fillCurrentPlayer = Paint().apply {
        color = 0xFF0000a3.toInt()
    }


    @ExperimentalTime
    override fun onRender(canvas: Canvas, width: Int, height: Int, nanoTime: Long) {
        contentScale = layer.contentScale
        canvas.scale(contentScale, contentScale)
        w = (width / contentScale).toInt()
        h = (height / contentScale).toInt()

        centerX = w / 2f
        centerY = h / 2f
        boardWidth = min(w, h) / 2f - 50

        if (!isFinish) {
            displayBoard(canvas)
            displayCurrentPlayer(canvas)
            displayCurrentCell(canvas)
            if (current in bots) {
                botMove(current)
            }
            displayPossibleMoves(canvas)
            displayCheckers(canvas)
            displayQueens(canvas)

        } else {
            println(current)
            displayFinal(canvas)
            return
        }
        isFinish = finished()


        layer.needRedraw()
    }


    private fun displayBoard(canvas: Canvas) {

        val x = centerX - boardWidth
        val y = centerY - boardWidth
        val cellWidth = boardWidth / 4
        val myFont = Font(typeface, boardWidth / 10)

        val fond: Rect = Rect.makeXYWH(
            x - boardWidth / 8,
            y - boardWidth / 8,
            2 * boardWidth + boardWidth / 4,
            2 * boardWidth + boardWidth / 4
        )
        canvas.drawRect(fond, fillFond)

        for (i in 0..7) {
            canvas.drawString(
                ('a' + i).toString(),
                x + i * cellWidth + cellWidth / 2 - cellWidth / 8,
                y - boardWidth / 32,
                myFont,
                fillSymbols
            )
            canvas.drawString(
                ('a' + i).toString(),
                x + i * cellWidth + cellWidth / 2 - cellWidth / 8,
                y + 2 * boardWidth + boardWidth / 11,
                myFont,
                fillSymbols
            )
            canvas.drawString(
                ('8' - i).toString(),
                x - boardWidth / 12,
                y + i * cellWidth + cellWidth / 2 + cellWidth / 8,
                myFont,
                fillSymbols
            )
            canvas.drawString(
                ('8' - i).toString(),
                x + 2 * boardWidth + boardWidth / 30,
                y + i * cellWidth + cellWidth / 2 + cellWidth / 8,
                myFont,
                fillSymbols
            )
            for (j in 0..7) {
                val curRect: Rect = Rect.makeXYWH(x + i * cellWidth, y + j * cellWidth, cellWidth, cellWidth)
                if ((i + j) % 2 == 0) {
                    canvas.drawRect(curRect, fillWhiteCell)
                } else {
                    canvas.drawRect(curRect, fillBlackCell)
                }
            }
        }
    }

    private fun displayCheckers(canvas: Canvas) {
        val x = centerX - boardWidth
        val y = centerY - boardWidth
        val cellWidth = boardWidth / 4
        val checkerRadius = cellWidth * 3 / 7

        for (i in whites.indices) {
            canvas.drawCircle(
                x + cellWidth / 2 + whites[i].first * cellWidth,
                y + cellWidth / 2 + whites[i].second * cellWidth,
                checkerRadius,
                whiteCheckerPaint
            )
        }

        for (i in blacks.indices) {
            canvas.drawCircle(
                x + cellWidth / 2 + blacks[i].first * cellWidth,
                y + cellWidth / 2 + blacks[i].second * cellWidth,
                checkerRadius,
                blackCheckerPaint
            )
        }
    }

    private fun displayQueens(canvas: Canvas) {
        val x = centerX - boardWidth
        val y = centerY - boardWidth
        val cellWidth = boardWidth / 4
        val checkerRadius = cellWidth * 3 / 7

        for (i in whiteQueens.indices) {
            canvas.drawCircle(
                x + cellWidth / 2 + whiteQueens[i].first * cellWidth,
                y + cellWidth / 2 + whiteQueens[i].second * cellWidth,
                checkerRadius,
                whiteCheckerPaint
            )
            canvas.drawCircle(
                x + cellWidth / 2 + whiteQueens[i].first * cellWidth,
                y + cellWidth / 2 + whiteQueens[i].second * cellWidth,
                checkerRadius * 2 / 3,
                blackCheckerPaint
            )
        }

        for (i in blackQueens.indices) {
            canvas.drawCircle(
                x + cellWidth / 2 + blackQueens[i].first * cellWidth,
                y + cellWidth / 2 + blackQueens[i].second * cellWidth,
                checkerRadius,
                blackCheckerPaint
            )
            canvas.drawCircle(
                x + cellWidth / 2 + blackQueens[i].first * cellWidth,
                y + cellWidth / 2 + blackQueens[i].second * cellWidth,
                checkerRadius * 2 / 3,
                whiteCheckerPaint
            )
        }
    }

    private fun displayCurrentCell(canvas: Canvas) {
        val x = centerX - boardWidth
        val y = centerY - boardWidth
        val cellWidth = boardWidth / 4
        if ((current == "white" && (currentChecker in whites || currentChecker in whiteQueens)) || (current == "black" && (currentChecker in blacks || currentChecker in blackQueens))) {
            val mouseRect: Rect = Rect.makeXYWH(
                x + currentChecker.first * cellWidth,
                y + currentChecker.second * cellWidth,
                cellWidth,
                cellWidth
            )
            canvas.drawRect(mouseRect, fillCurrentCell)
        }
    }

    private fun displayCurrentPlayer(canvas: Canvas) {
        val x = centerX - boardWidth
        val y = centerY - boardWidth
        val cellWidth = boardWidth / 4
        val myFont = Font(typeface, boardWidth / 10)
        for (i in current.indices) {
            canvas.drawString(
                current[i].toString(),
                x - boardWidth / 4,
                8 * cellWidth / 5 + y + i * cellWidth + cellWidth / 2 + cellWidth / 8,
                myFont,
                fillCurrentPlayer
            )
            canvas.drawString(
                current[i].toString(),
                x + 2 * boardWidth + boardWidth / 6,
                8 * cellWidth / 5 + y + i * cellWidth + cellWidth / 2 + cellWidth / 8,
                myFont,
                fillCurrentPlayer
            )
        }
    }

    private fun displayPossibleMoves(canvas: Canvas) {
        val x = centerX - boardWidth
        val y = centerY - boardWidth
        val cellWidth = boardWidth / 4
        for (cordX in 0..7) {
            for (cordY in 0..7) {
                val move: Pair<Int, Int> = Pair(cordX, cordY)
                var canEatOrNot = false
                if (current == "white") {
                    for (i in whites.indices) {
                        if (canEat(whites[i], current))
                            canEatOrNot = true
                    }
                    for (i in whiteQueens.indices) {
                        if (canEat(whiteQueens[i], current))
                            canEatOrNot = true
                    }
                } else {
                    for (i in blacks.indices) {
                        if (canEat(blacks[i], current))
                            canEatOrNot = true
                    }
                    for (i in blackQueens.indices) {
                        if (canEat(blackQueens[i], current))
                            canEatOrNot = true
                    }
                }
                if ((currentChecker in whites || currentChecker in whiteQueens && current == "white") || (currentChecker in blacks || currentChecker in blackQueens && current == "black"))
                    if ((isMoveCorrect(currentChecker, move, current) && !canEatOrNot) || isMoveCorrectEating(
                            currentChecker,
                            move,
                            current
                        ) != null
                    ) {
                        canvas.drawCircle(
                            x + cellWidth / 2 + move.first * cellWidth,
                            y + cellWidth / 2 + move.second * cellWidth,
                            cellWidth / 6,
                            fillCurrentCell
                        )
                    }
            }
        }
    }

    private fun displayFinal(canvas: Canvas) {
        if (current == "white") {
            current = "black"
        } else {
            current = "white"
        }
        val x = centerX - boardWidth/2
        val y = centerY
        val myFont = Font(typeface, boardWidth / 4)
        canvas.drawString("$current won!", x, y, myFont, fillCurrentPlayer)
    }
}

object State {
    var mouseX = 0f
    var mouseY = 0f
}

object Click {
    var mouseX = 0f
    var mouseY = 0f
}

object MouseMotionAdapter : MouseMotionAdapter() {
    override fun mouseMoved(event: MouseEvent) {
        State.mouseX = event.x.toFloat()
        State.mouseY = event.y.toFloat()
    }
}

object MouseListener : MouseListener {
    override fun mouseClicked(e: MouseEvent?) {
        //TODO("Not yet implemented")
    }

    override fun mousePressed(e: MouseEvent) {
        if (e.button == MouseEvent.BUTTON1) {
            if (current in bots) {
                return
            }
            Click.mouseX = State.mouseX
            Click.mouseY = State.mouseY
            val x = centerX - boardWidth
            val y = centerY - boardWidth
            val cellWidth = boardWidth / 4
            val cordX = ((Click.mouseX - x) / cellWidth).toInt()
            val cordY = ((Click.mouseY - y) / cellWidth).toInt()

            if (!currentEated) {
                if (current == "white") {
                    if (Pair(cordX, cordY) in whites || Pair(cordX, cordY) in whiteQueens) {
                        currentChecker = Pair(cordX, cordY)
                    }
                } else {
                    if (Pair(cordX, cordY) in blacks || Pair(cordX, cordY) in blackQueens) {
                        currentChecker = Pair(cordX, cordY)
                    }
                }
            }
            val move = Pair(cordX, cordY)

            if (isMoveCorrect(currentChecker, move, current) && !canSomebodyEat(current)) {
                if (current == "white") {
                    if (currentChecker in whiteQueens || move.second == 0) {
                        whites.remove(currentChecker)
                        whiteQueens.remove(currentChecker)
                        whiteQueens.add(move)
                    } else {
                        whites.remove(currentChecker)
                        whites.add(move)
                    }
                    current = "black"
                } else {
                    if (currentChecker in blackQueens || move.second == 7) {
                        blacks.remove(currentChecker)
                        blackQueens.remove(currentChecker)
                        blackQueens.add(move)
                    } else {
                        blacks.remove(currentChecker)
                        blacks.add(move)
                    }
                    current = "white"
                }
            }

            if (isMoveCorrectEating(currentChecker, move, current) != null) {
                val eaten = isMoveCorrectEating(currentChecker, move, current)
                currentEated = true
                if (currentChecker in whiteQueens) {
                    blacks.remove(eaten)
                    blackQueens.remove(eaten)
                    whiteQueens.remove(currentChecker)
                    whites.remove(currentChecker)
                    whiteQueens.add(move)
                }
                if (currentChecker in whites) {
                    blacks.remove(eaten)
                    blackQueens.remove(eaten)
                    whites.remove(currentChecker)
                    if (move.second != 0)
                        whites.add(move)
                    else
                        whiteQueens.add(move)
                }
                if (currentChecker in blackQueens) {
                    whites.remove(eaten)
                    whiteQueens.remove(eaten)
                    blacks.remove(currentChecker)
                    blackQueens.remove(currentChecker)
                    blackQueens.add(move)
                }
                if (currentChecker in blacks) {
                    whites.remove(eaten)
                    whiteQueens.remove(eaten)
                    blackQueens.remove(currentChecker)
                    blacks.remove(currentChecker)
                    if (move.second != 7)
                        blacks.add(move)
                    else
                        blackQueens.add(move)
                }
                currentChecker = move
                if (!canEat(move, current)) {
                    currentEated = false
                    if (current == "white")
                        current = "black"
                    else
                        current = "white"
                }
            }
        }
    }

    override fun mouseReleased(e: MouseEvent?) {
        //TODO("Not yet implemented")
    }

    override fun mouseEntered(e: MouseEvent?) {
        //TODO("Not yet implemented")
    }

    override fun mouseExited(e: MouseEvent?) {
        // TODO("Not yet implemented")
    }
}

fun isMoveCorrect(from: Pair<Int, Int>, to: Pair<Int, Int>, color: String): Boolean {
    if (from.first == to.first || from.second == to.second || to in whites || to in blacks || to in whiteQueens || to in blackQueens || abs(from.first - to.first) != abs(from.second - to.second))
        return false
    if (color == "white") {
        if (from in whites) {
            if (to.second == from.second - 1 && (abs(from.first - to.first) == 1)) {
                return true
            }
        }
        if (from in whiteQueens) {
            var count = 0
            val deltaX = abs(from.first - to.first) / (to.first - from.first)
            val deltaY = abs(from.second - to.second) / (to.second - from.second)
            for (i in 1 until abs(from.first - to.first)) {
                if (Pair(from.first + i * deltaX, from.second + i * deltaY) in blacks || Pair(
                        from.first + i * deltaX,
                        from.second + i * deltaY
                    ) in blackQueens || Pair(from.first + i * deltaX, from.second + i * deltaY) in whites || Pair(
                        from.first + i * deltaX,
                        from.second + i * deltaY
                    ) in whiteQueens
                ) {
                    count++
                }
            }
            return count == 0
        }
    }
    if (color == "black") {
        if (from in blacks) {
            if (to.second == from.second + 1 && (abs(from.first - to.first) == 1)) {
                return true
            }
        }
        if (from in blackQueens) {
            var count = 0
            val deltaX = abs(from.first - to.first) / (to.first - from.first)
            val deltaY = abs(from.second - to.second) / (to.second - from.second)
            for (i in 1 until abs(from.first - to.first)) {
                if (Pair(from.first + i * deltaX, from.second + i * deltaY) in whites || Pair(
                        from.first + i * deltaX,
                        from.second + i * deltaY
                    ) in whiteQueens || Pair(from.first + i * deltaX, from.second + i * deltaY) in blacks || Pair(
                        from.first + i * deltaX,
                        from.second + i * deltaY
                    ) in blackQueens
                )
                    count++
            }
            return count == 0
        }
    }
    return false
}

fun isMoveCorrectEating(from: Pair<Int, Int>, to: Pair<Int, Int>, color: String): Pair<Int, Int>? {
    if (color == "white") {
        if (from !in whites && from !in whiteQueens)
            return null
    } else {
        if (from !in blacks && from !in blackQueens)
            return null
    }
    if (from.first == to.first || from.second == to.second || to in whites || to in whiteQueens || to in blackQueens || to in blacks || to.first !in 0..7 || to.second !in 0..7)
        return null
    if (from in whites || from in blacks) {
        if (abs(from.first - to.first) != 2 || abs(from.second - to.second) != 2)
            return null
        val mid: Pair<Int, Int> = Pair((from.first + to.first) / 2, (from.second + to.second) / 2)
        if (color == "white") {
            if (mid in blacks || mid in blackQueens) {
                return mid
            }
        }
        if (color == "black") {
            if (mid in whites || mid in whiteQueens) {
                return mid
            }
        }
    }
    if (from in whiteQueens || from in blackQueens) {
        if (abs(from.first - to.first) != abs(from.second - to.second))
            return null
        val deltaX = abs(from.first - to.first) / (to.first - from.first)
        val deltaY = abs(from.second - to.second) / (to.second - from.second)
        if (from in whiteQueens) {
            var count = 0
            var eaten = Pair(0, 0)
            for (i in 1 until abs(from.first - to.first)) {
                if (Pair(from.first + i * deltaX, from.second + i * deltaY) in blacks || Pair(
                        from.first + i * deltaX,
                        from.second + i * deltaY
                    ) in blackQueens
                ) {
                    count++
                    eaten = Pair(from.first + i * deltaX, from.second + i * deltaY)
                }
                if(Pair(from.first + i * deltaX, from.second + i * deltaY) in whites || Pair(
                        from.first + i * deltaX,
                        from.second + i * deltaY
                    ) in whiteQueens)
                    return null
            }
            if (count == 1)
                return eaten
        }
        if (from in blackQueens) {
            var count = 0
            var eaten = Pair(0, 0)
            for (i in 1 until abs(from.first - to.first)) {
                if (Pair(from.first + i * deltaX, from.second + i * deltaY) in whites || Pair(
                        from.first + i * deltaX,
                        from.second + i * deltaY
                    ) in whiteQueens
                ) {
                    count++
                    eaten = Pair(from.first + i * deltaX, from.second + i * deltaY)
                }
                if (Pair(from.first + i * deltaX, from.second + i * deltaY) in blacks || Pair(
                        from.first + i * deltaX,
                        from.second + i * deltaY
                    ) in blackQueens
                )
                    return null
            }
            if (count == 1)
                return eaten
        }
    }
    return null
}

fun canEat(checker: Pair<Int, Int>, color: String): Boolean {
    if (checker in whites || checker in blacks) {
        var move: Pair<Int, Int> = Pair(checker.first - 2, checker.second - 2)
        if (isMoveCorrectEating(checker, move, color) != null)
            return true
        move = Pair(checker.first - 2, checker.second + 2)
        if (isMoveCorrectEating(checker, move, color) != null)
            return true
        move = Pair(checker.first + 2, checker.second - 2)
        if (isMoveCorrectEating(checker, move, color) != null)
            return true
        move = Pair(checker.first + 2, checker.second + 2)
        if (isMoveCorrectEating(checker, move, color) != null)
            return true
        return false
    }
    if (checker in whiteQueens || checker in blackQueens) {
        var deltaX = 1
        var deltaY = 1
        for (i in 0..8) {
            val move: Pair<Int, Int> = Pair(checker.first + i * deltaX, checker.second + i * deltaY)
            if (isMoveCorrectEating(checker, move, color) != null)
                return true
        }
        deltaY = -1
        for (i in 0..8) {
            val move: Pair<Int, Int> = Pair(checker.first + i * deltaX, checker.second + i * deltaY)
            if (isMoveCorrectEating(checker, move, color) != null)
                return true
        }
        deltaX = -1
        for (i in 0..8) {
            val move: Pair<Int, Int> = Pair(checker.first + i * deltaX, checker.second + i * deltaY)
            if (isMoveCorrectEating(checker, move, color) != null)
                return true
        }
        deltaY = 1
        for (i in 0..8) {
            val move: Pair<Int, Int> = Pair(checker.first + i * deltaX, checker.second + i * deltaY)
            if (isMoveCorrectEating(checker, move, color) != null)
                return true
        }
    }
    return false
}

fun finished(): Boolean {
    for (i in 0..7) {
        for (j in 0..7) {
            val move = Pair(i, j)
            if (current == "white") {
                for (cur in whites) {
                    if (isMoveCorrect(cur, move, current))
                        return false
                    if (isMoveCorrectEating(cur, move, current) != null)
                        return false
                }
                for (cur in whiteQueens) {
                    if (isMoveCorrect(cur, move, current))
                        return false
                    if (isMoveCorrectEating(cur, move, current) != null)
                        return false
                }
            } else {
                for (cur in blacks) {
                    if (isMoveCorrect(cur, move, current))
                        return false
                    if (isMoveCorrectEating(cur, move, current) != null)
                        return false
                }
                for (cur in blackQueens) {
                    if (isMoveCorrect(cur, move, current))
                        return false
                    if (isMoveCorrectEating(cur, move, current) != null)
                        return false
                }
            }
        }
    }
    return true
}

fun canSomebodyEat(color : String) : Boolean{
    if (color == "white") {
        for (i in whites) {
            if (canEat(i, current))
                return true
        }
        for (i in whiteQueens) {
            if (canEat(i, current))
                return true
        }
    } else {
        for (i in blacks) {
            if (canEat(i, current))
                return true
        }
        for (i in blackQueens) {
            if (canEat(i, current))
                return true
        }
    }
    return false
}

fun botMove(color : String) {
    val possibleMoves : MutableList<Pair<Pair<Int, Int>, Pair<Int, Int>>> = mutableListOf()
    if (color == "white") {
        for (i in 0..7) {
            for (j in 0..7) {
                val move = Pair(i, j)
                for (cur in whites) {
                    if (canSomebodyEat(color)) {
                        if (isMoveCorrectEating(cur, move, color) != null) {
                            possibleMoves.add(Pair(cur, move))
                        }
                    } else {
                        if (isMoveCorrect(cur, move, color)) {
                            possibleMoves.add(Pair(cur, move))
                        }
                    }
                }
                for (cur in whiteQueens) {
                    if (canSomebodyEat(color)) {
                        if (isMoveCorrectEating(cur, move, color) != null) {
                            possibleMoves.add(Pair(cur, move))
                        }
                    } else {
                        if (isMoveCorrect(cur, move, color)) {
                            possibleMoves.add(Pair(cur, move))
                        }
                    }
                }
            }
        }
    } else {
        for (i in 0..7) {
            for (j in 0..7) {
                val move = Pair(i, j)
                for (cur in blacks) {
                    if (canSomebodyEat(color)) {
                        if (isMoveCorrectEating(cur, move, color) != null) {
                            possibleMoves.add(Pair(cur, move))
                        }
                    } else {
                        if (isMoveCorrect(cur, move, color)) {
                            possibleMoves.add(Pair(cur, move))
                        }
                    }
                }
                for (cur in blackQueens) {
                    if (canSomebodyEat(color)) {
                        if (isMoveCorrectEating(cur, move, color) != null) {
                            possibleMoves.add(Pair(cur, move))
                        }
                    } else {
                        if (isMoveCorrect(cur, move, color)) {
                            possibleMoves.add(Pair(cur, move))
                        }
                    }
                }
            }
        }
    }
    if (possibleMoves.size == 0) {
        return
    }
    val rnd = (0 until possibleMoves.size).random()
    if (isMoveCorrect(possibleMoves[rnd].first, possibleMoves[rnd].second, color)) {
        if (color == "white") {
            if (possibleMoves[rnd].first in whiteQueens || possibleMoves[rnd].second.second == 0) {
                whiteQueens.remove(possibleMoves[rnd].first)
                whites.remove(possibleMoves[rnd].first)
                whiteQueens.add(possibleMoves[rnd].second)
            } else {
                whites.remove(possibleMoves[rnd].first)
                whites.add(possibleMoves[rnd].second)
            }
        } else {
            if (possibleMoves[rnd].first in blackQueens || possibleMoves[rnd].second.second == 7) {
                blackQueens.remove(possibleMoves[rnd].first)
                blacks.remove(possibleMoves[rnd].first)
                blackQueens.add(possibleMoves[rnd].second)
            } else {
                blacks.remove(possibleMoves[rnd].first)
                blacks.add(possibleMoves[rnd].second)
            }
        }
    } else {
        if (isMoveCorrectEating(possibleMoves[rnd].first, possibleMoves[rnd].second, color) != null) {
            var currentChecker = possibleMoves[rnd].first
            var to = possibleMoves[rnd].second
            var eaten = isMoveCorrectEating(currentChecker, to, color)
            if (color == "white") {
                blacks.remove(eaten)
                blackQueens.remove(eaten)
                if (currentChecker in whiteQueens || to.second == 0) {
                    whiteQueens.remove(currentChecker)
                    whites.remove(currentChecker)
                    whiteQueens.add(to)
                } else {
                    whites.remove(currentChecker)
                    whites.add(to)
                }
            } else {
                whites.remove(eaten)
                whiteQueens.remove(eaten)
                if (currentChecker in blackQueens || to.second == 0) {
                    blackQueens.remove(currentChecker)
                    blacks.remove(currentChecker)
                    blackQueens.add(to)
                } else {
                    blacks.remove(currentChecker)
                    blacks.add(to)
                }
            }
            currentChecker = to
            while(canEat(currentChecker, color)) {
                to = possibleEatByChecker(currentChecker, color)
                eaten = isMoveCorrectEating(currentChecker, to, color)
                if (color == "white") {
                    blacks.remove(eaten)
                    blackQueens.remove(eaten)
                    if (currentChecker in whiteQueens || to.second == 0) {
                        whiteQueens.remove(currentChecker)
                        whites.remove(currentChecker)
                        whiteQueens.add(to)
                    } else {
                        whites.remove(currentChecker)
                        whites.add(to)
                    }
                } else {
                    whites.remove(eaten)
                    whiteQueens.remove(eaten)
                    if (currentChecker in blackQueens || to.second == 0) {
                        blackQueens.remove(currentChecker)
                        blacks.remove(currentChecker)
                        blackQueens.add(to)
                    } else {
                        blacks.remove(currentChecker)
                        blacks.add(to)
                    }
                }
                currentChecker = to
            }
        }
    }

    if (current == "white") {
        current = "black"
    } else {
        current = "white"
    }
}

fun possibleEatByChecker(checker: Pair<Int, Int>, color: String) : Pair<Int, Int> {
    val moves : MutableList<Pair<Int, Int>> = mutableListOf()
    for (i in 0..7) {
        for (j in 0..7) {
            val move = Pair(i, j)
            if (isMoveCorrectEating(checker, move, color) != null) {
                moves.add(move)
            }
        }
    }
    val rnd = (0 until moves.size).random()
    return moves[rnd]
}
