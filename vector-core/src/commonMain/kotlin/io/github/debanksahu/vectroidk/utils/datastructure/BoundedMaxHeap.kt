package io.github.debanksahu.vectroidk.utils.datastructure

import io.github.sooniln.fastcollect.ints.getValue
import io.github.sooniln.fastcollect.ints.mutableInt2FloatMapOf
import io.github.sooniln.fastcollect.ints.mutableIntListOf

class BoundedMaxHeap(
    val size: Int
) {

    init {
        require(size > 0) { "Size must be greater than zero." }
    }

    val minHeap = mutableIntListOf()
    val index2ValueMap = mutableInt2FloatMapOf()

    fun add(index: Int, value: Float) {
        if (value.isInfinite()) return
        if (minHeap.size < size) {
            minHeap.add(index)
            index2ValueMap[index] = value
            shiftUp(minHeap.lastIndex)
            return
        }

        if (value <= index2ValueMap.getValue(minHeap.getAt(0))) return

        index2ValueMap.removeKey(minHeap.getAt(0))
        minHeap.setAt(0, index)
        index2ValueMap[index] = value
        shiftDown(0)
    }

    fun popMinEle(): Int {
        if (minHeap.isEmpty()) throw NoSuchElementException("Heap is empty.")

        val topElement = minHeap.getAt(0)
        index2ValueMap.removeKey(topElement)

        if (minHeap.size == 1) minHeap.removeAt(0)
        else {
            val lastElement = minHeap.last()
            minHeap.setAt(0, lastElement)
            minHeap.removeLast()
            shiftDown(0)
        }

        return topElement
    }

    fun clear() {
        minHeap.clear()
        index2ValueMap.clear()
    }

    // ---- Private Functions ----

    private fun shiftUp(index: Int) {
        var currChild = index
        while (currChild > 0) {
            val currParent = (currChild - 1) / 2

            val parentScore = index2ValueMap.getValue(minHeap.getAt(currParent))
            val childScore = index2ValueMap.getValue(minHeap.getAt(currChild))
            if (parentScore <= childScore) break
            swap(currParent, currChild)
            currChild = currParent
        }
    }

    private fun shiftDown(index: Int) {
        var currParent = index
        while (true) {
            val currLeftChild = currParent * 2 + 1

            val currRightChild = currParent * 2 + 2
            var currSmallest = currParent

            if (currLeftChild < minHeap.size) {
                val currLeftChildScore = index2ValueMap.getValue(minHeap.getAt(currLeftChild))
                if (index2ValueMap.getValue(minHeap.getAt(currSmallest)) > currLeftChildScore) {
                    currSmallest = currLeftChild
                }
            }
            if (currRightChild < minHeap.size) {
                val currRightChildScore = index2ValueMap.getValue(minHeap.getAt(currRightChild))
                if (index2ValueMap.getValue(minHeap.getAt(currSmallest)) > currRightChildScore) {
                    currSmallest = currRightChild
                }
            }

            if (currSmallest == currParent) break
            swap(currParent, currSmallest)
            currParent = currSmallest
        }
    }

    private fun swap(index1: Int, index2: Int) {
        minHeap.setAt(index1, minHeap.getAt(index1) + minHeap.getAt(index2))
        minHeap.setAt(index2, minHeap.getAt(index1) - minHeap.getAt(index2))
        minHeap.setAt(index1, minHeap.getAt(index1) - minHeap.getAt(index2))
    }


}