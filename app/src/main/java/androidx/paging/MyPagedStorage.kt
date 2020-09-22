package androidx.paging

/**
 * @author liangyuying.lyy75@bytedance.com
 * @date 2020/9/15
 * <p>
 * todo 描述
 */
open class MyPagedStorage<T> : AbstractList<T?> {
    private var mLeadingNullCount: Int
    private val mPages: ArrayList<MutableList<T>?>
    private var mTrailingNullCount: Int
    private var mPositionOffset: Int

    private var mLoadedCount: Int

    private var mStorageCount: Int

    private var mPageSize: Int
    private var mNumberPrepended: Int
    private var mNumberAppended: Int

    constructor() {
        mLeadingNullCount = 0
        mPages = ArrayList()
        mTrailingNullCount = 0
        mPositionOffset = 0
        mLoadedCount = 0
        mStorageCount = 0
        mPageSize = 1
        mNumberPrepended = 0
        mNumberAppended = 0
    }

    constructor(leadingNulls: Int, page: MutableList<T>, trailingNulls: Int) : this() {
        init(leadingNulls, page, trailingNulls, 0)
    }

    private constructor(other: MyPagedStorage<T>) {
        mLeadingNullCount = other.mLeadingNullCount
        mPages = ArrayList(other.mPages)
        mTrailingNullCount = other.mTrailingNullCount
        mPositionOffset = other.mPositionOffset
        mLoadedCount = other.mLoadedCount
        mStorageCount = other.mStorageCount
        mPageSize = other.mPageSize
        mNumberPrepended = other.mNumberPrepended
        mNumberAppended = other.mNumberAppended
    }

    internal fun snapshot(): MyPagedStorage<T> = MyPagedStorage(this)

    private fun init(leadingNulls: Int, page: MutableList<T>, trailingNulls: Int, positionOffset: Int) {
        mLeadingNullCount = leadingNulls
        mPages.clear()
        mPages.add(page)
        mTrailingNullCount = trailingNulls
        mPositionOffset = positionOffset
        mLoadedCount = page.size
        mStorageCount = mLoadedCount
        mPageSize = page.size
        mNumberPrepended = 0
        mNumberAppended = 0
    }

    fun init(leadingNulls: Int, page: MutableList<T>, trailingNulls: Int, positionOffset: Int, callback: Callback) {
        init(leadingNulls, page, trailingNulls, positionOffset)
        callback.onInitialized(size)
    }

    override fun get(index: Int): T? {
        if (index < 0 || index >= size) {
            throw IndexOutOfBoundsException("Index: $index, Size: $size")
        }
        val localIndex = index - mLeadingNullCount
        if (localIndex < 0 || localIndex >= mStorageCount) {
            return null
        }
        var localPageIndex: Int
        var pageInternalIndex: Int
        if (isTiled()) {
            localPageIndex = localIndex / mPageSize
            pageInternalIndex = localIndex % mPageSize
        } else {
            pageInternalIndex = localIndex
            val localPageCount = mPages.size
            localPageIndex = 0
            while (localPageIndex < localPageCount) {
                val pageSize = mPages[localPageIndex]!!.size
                if (pageSize > pageInternalIndex) {
                    break
                }
                pageInternalIndex -= pageSize
                localPageIndex++
            }
        }
        val page = mPages[localPageIndex]
        return if (page == null || page.isEmpty()) {
            null
        } else page[pageInternalIndex]
    }

    fun isTiled(): Boolean {
        return mPageSize > 0
    }

    fun getLeadingNullCount(): Int {
        return mLeadingNullCount
    }

    fun getTrailingNullCount(): Int {
        return mTrailingNullCount
    }

    fun getStorageCount(): Int {
        return mStorageCount
    }

    fun getNumberAppended(): Int {
        return mNumberAppended
    }

    fun getNumberPrepended(): Int {
        return mNumberPrepended
    }

    fun getPageCount(): Int {
        return mPages.size
    }

    fun getLoadedCount(): Int {
        return mLoadedCount
    }

    interface Callback {
        fun onInitialized(count: Int)
        fun onPagePrepended(leadingNulls: Int, changed: Int, added: Int)
        fun onPageAppended(endPosition: Int, changed: Int, added: Int)
        fun onPagePlaceholderInserted(pageIndex: Int, timeStamp: Long = 0L)
        fun onPageInserted(start: Int, count: Int)
        fun onPagesRemoved(startOfDrops: Int, count: Int)
        fun onPagesSwappedToPlaceholder(startOfDrops: Int, count: Int)
        fun onEmptyPrepend()
        fun onEmptyAppend()
    }

    fun getPositionOffset(): Int {
        return mPositionOffset
    }

    fun getMiddleOfLoadedRange(): Int {
        return mLeadingNullCount + mPositionOffset + mStorageCount / 2
    }

    override val size: Int
        get() = mLeadingNullCount + mStorageCount + mTrailingNullCount

    fun computeLeadingNulls(): Int {
        var total = mLeadingNullCount
        val pageCount = mPages.size
        for (i in 0 until pageCount) {
            val page: List<*>? = mPages[i]
            if (page != null && page !== PLACEHOLDER_LIST) {
                break
            }
            total += mPageSize
        }
        return total
    }

    fun computeTrailingNulls(): Int {
        var total = mTrailingNullCount
        for (i in mPages.indices.reversed()) {
            val page: List<*>? = mPages[i]
            if (page != null && page !== PLACEHOLDER_LIST) {
                break
            }
            total += mPageSize
        }
        return total
    }

    private fun needsTrim(maxSize: Int, requiredRemaining: Int, localPageIndex: Int): Boolean {
        val page = mPages[localPageIndex]
        return page == null || mLoadedCount > maxSize && mPages.size > 2 && page !== PLACEHOLDER_LIST && mLoadedCount - page.size >= requiredRemaining
    }

    fun needsTrimFromFront(maxSize: Int, requiredRemaining: Int): Boolean {
        return needsTrim(maxSize, requiredRemaining, 0)
    }

    fun needsTrimFromEnd(maxSize: Int, requiredRemaining: Int): Boolean {
        return needsTrim(maxSize, requiredRemaining, mPages.size - 1)
    }

    fun shouldPreTrimNewPage(maxSize: Int, requiredRemaining: Int, countToBeAdded: Int): Boolean {
        return mLoadedCount + countToBeAdded > maxSize && mPages.size > 1 && mLoadedCount >= requiredRemaining
    }

    fun trimFromFront(insertNulls: Boolean, maxSize: Int, requiredRemaining: Int, callback: Callback): Boolean {
        var totalRemoved = 0
        while (needsTrimFromFront(maxSize, requiredRemaining)) {
            val page: List<*>? = mPages.removeAt(0)
            val removed = page?.size ?: mPageSize
            totalRemoved += removed
            mStorageCount -= removed
            mLoadedCount -= page?.size ?: 0
        }
        if (totalRemoved > 0) {
            if (insertNulls) {
                // replace removed items with nulls
                val previousLeadingNulls = mLeadingNullCount
                mLeadingNullCount += totalRemoved
                callback.onPagesSwappedToPlaceholder(previousLeadingNulls, totalRemoved)
            } else {
                // simply remove, and handle offset
                mPositionOffset += totalRemoved
                callback.onPagesRemoved(mLeadingNullCount, totalRemoved)
            }
        }
        return totalRemoved > 0
    }

    fun trimFromEnd(insertNulls: Boolean, maxSize: Int, requiredRemaining: Int, callback: Callback): Boolean {
        var totalRemoved = 0
        while (needsTrimFromEnd(maxSize, requiredRemaining)) {
            val page: List<*>? = mPages.removeAt(mPages.size - 1)
            val removed = page?.size ?: mPageSize
            totalRemoved += removed
            mStorageCount -= removed
            mLoadedCount -= page?.size ?: 0
        }
        if (totalRemoved > 0) {
            val newEndPosition = mLeadingNullCount + mStorageCount
            if (insertNulls) {
                mTrailingNullCount += totalRemoved
                callback.onPagesSwappedToPlaceholder(newEndPosition, totalRemoved)
            } else {
                callback.onPagesRemoved(newEndPosition, totalRemoved)
            }
        }
        return totalRemoved > 0
    }

    // ---------------- Contiguous API -------------------
    fun getFirstLoadedItem(): T {
        return mPages[0]!![0]
    }

    fun getLastLoadedItem(): T {
        val page = mPages[mPages.size - 1]
        return page!![page.size - 1]
    }

    fun prependPage(page: MutableList<T>, callback: Callback) {
        val count = page.size
        if (count == 0) {
            callback.onEmptyPrepend()
            return
        }
        if (mPageSize > 0 && count != mPageSize) {
            mPageSize = if (mPages.size == 1 && count > mPageSize) {
                count
            } else {
                -1
            }
        }
        mPages.add(0, page)
        mLoadedCount += count
        mStorageCount += count
        val changedCount = Math.min(mLeadingNullCount, count)
        val addedCount = count - changedCount
        if (changedCount != 0) {
            mLeadingNullCount -= changedCount
        }
        mPositionOffset -= addedCount
        mNumberPrepended += count
        callback.onPagePrepended(mLeadingNullCount, changedCount, addedCount)
    }

    fun appendPage(page: MutableList<T>, callback: Callback) {
        val count = page.size
        if (count == 0) {
            callback.onEmptyAppend()
            return
        }
        if (mPageSize > 0) {
            if (mPages[mPages.size - 1]!!.size != mPageSize || count > mPageSize) {
                mPageSize = -1
            }
        }
        mPages.add(page)
        mLoadedCount += count
        mStorageCount += count
        val changedCount = Math.min(mTrailingNullCount, count)
        val addedCount = count - changedCount
        if (changedCount != 0) {
            mTrailingNullCount -= changedCount
        }
        mNumberAppended += count
        callback.onPageAppended(mLeadingNullCount + mStorageCount - count,
                changedCount, addedCount)
    }

    fun insertPage2(about: T, page: MutableList<T>, callback: Callback) {
        var start = 0
        for (page2 in mPages) {
            if (page2 != null) {
                if (page2.contains(about)) {
                    val index = page2.indexOf(about) + 1
                    page2.addAll(index, page)
                    callback.onPageInserted(start + index, page.size)
                    break
                } else {
                    start += page2.size
                }
            }
        }
    }

    // ------------------ Non-Contiguous API (tiling required) ----------------------

    fun pageWouldBeBoundary(positionOfPage: Int, trimFromFront: Boolean): Boolean {
        check(!(mPageSize < 1 || mPages.size < 2)) { "Trimming attempt before sufficient load" }
        if (positionOfPage < mLeadingNullCount) {
            // position represent page in leading nulls
            return trimFromFront
        }
        if (positionOfPage >= mLeadingNullCount + mStorageCount) {
            // position represent page in trailing nulls
            return !trimFromFront
        }
        val localPageIndex = (positionOfPage - mLeadingNullCount) / mPageSize

        // walk outside in, return false if we find non-placeholder page before localPageIndex
        if (trimFromFront) {
            for (i in 0 until localPageIndex) {
                if (mPages[i] != null) {
                    return false
                }
            }
        } else {
            for (i in mPages.size - 1 downTo localPageIndex + 1) {
                if (mPages[i] != null) {
                    return false
                }
            }
        }

        // didn't find another page, so this one would be a boundary
        return true
    }

    fun initAndSplit(leadingNulls: Int, multiPageList: MutableList<T>, trailingNulls: Int, positionOffset: Int, pageSize: Int, callback: Callback) {
        val pageCount = (multiPageList.size + (pageSize - 1)) / pageSize
        for (i in 0 until pageCount) {
            val beginInclusive = i * pageSize
            val endExclusive = Math.min(multiPageList.size, (i + 1) * pageSize)
            val sublist = multiPageList.subList(beginInclusive, endExclusive)
            if (i == 0) {
                // Trailing nulls for first page includes other pages in multiPageList
                val initialTrailingNulls = trailingNulls + multiPageList.size - sublist.size
                init(leadingNulls, sublist, initialTrailingNulls, positionOffset)
            } else {
                val insertPosition = leadingNulls + beginInclusive
                insertPage(insertPosition, sublist, null)
            }
        }
        callback.onInitialized(size)
    }

    fun tryInsertPageAndTrim(position: Int, page: MutableList<T>, lastLoad: Int, maxSize: Int, requiredRemaining: Int, callback: Callback) {
        val trim = maxSize != PagedList.Config.MAX_SIZE_UNBOUNDED
        val trimFromFront = lastLoad > getMiddleOfLoadedRange()
        val pageInserted = (!trim || !shouldPreTrimNewPage(maxSize, requiredRemaining, page.size) || !pageWouldBeBoundary(position, trimFromFront))
        if (pageInserted) {
            insertPage(position, page, callback)
        } else {
            val localPageIndex = (position - mLeadingNullCount) / mPageSize
            mPages[localPageIndex] = null
            mStorageCount -= page.size
            if (trimFromFront) {
                mPages.removeAt(0)
                mLeadingNullCount += page.size
            } else {
                mPages.removeAt(mPages.size - 1)
                mTrailingNullCount += page.size
            }
        }
        if (trim) {
            if (trimFromFront) {
                trimFromFront(true, maxSize, requiredRemaining, callback)
            } else {
                trimFromEnd(true, maxSize, requiredRemaining, callback)
            }
        }
    }

    fun insertPage(position: Int, page: MutableList<T>, callback: Callback?) {
        val newPageSize = page.size
        if (newPageSize != mPageSize) {
            val size = size
            val addingLastPage = (position == size - size % mPageSize && newPageSize < mPageSize)
            val onlyEndPagePresent = mTrailingNullCount == 0 && mPages.size == 1 && newPageSize > mPageSize
            require(!(!onlyEndPagePresent && !addingLastPage)) { "page introduces incorrect tiling" }
            if (onlyEndPagePresent) {
                mPageSize = newPageSize
            }
        }
        val pageIndex = position / mPageSize
        allocatePageRange(pageIndex, pageIndex)
        val localPageIndex = pageIndex - mLeadingNullCount / mPageSize
        val oldPage = mPages[localPageIndex]
        require(!(oldPage != null && oldPage !== PLACEHOLDER_LIST)) { "Invalid position $position: data already loaded" }
        mPages[localPageIndex] = page
        mLoadedCount += newPageSize
        callback?.onPageInserted(position, newPageSize)
    }

    fun allocatePageRange(minimumPage: Int, maximumPage: Int) {
        var leadingNullPages = mLeadingNullCount / mPageSize
        if (minimumPage < leadingNullPages) {
            for (i in 0 until leadingNullPages - minimumPage) {
                mPages.add(0, null)
            }
            val newStorageAllocated = (leadingNullPages - minimumPage) * mPageSize
            mStorageCount += newStorageAllocated
            mLeadingNullCount -= newStorageAllocated
            leadingNullPages = minimumPage
        }
        if (maximumPage >= leadingNullPages + mPages.size) {
            val newStorageAllocated = Math.min(mTrailingNullCount,
                    (maximumPage + 1 - (leadingNullPages + mPages.size)) * mPageSize)
            for (i in mPages.size..maximumPage - leadingNullPages) {
                mPages.add(mPages.size, null)
            }
            mStorageCount += newStorageAllocated
            mTrailingNullCount -= newStorageAllocated
        }
    }

    fun allocatePlaceholders(index: Int, prefetchDistance: Int, pageSize: Int, callback: Callback, timeStamp: Long = 0L) {
        if (pageSize != mPageSize) {
            require(pageSize >= mPageSize) { "Page size cannot be reduced" }
            require(!(mPages.size != 1 || mTrailingNullCount != 0)) { "Page size can change only if last page is only one present" }
            mPageSize = pageSize
        }
        val maxPageCount = (size + mPageSize - 1) / mPageSize
        val minimumPage = ((index - prefetchDistance) / mPageSize).coerceAtLeast(0)
        val maximumPage = ((index + prefetchDistance) / mPageSize).coerceAtMost(maxPageCount - 1)
        allocatePageRange(minimumPage, maximumPage)
        val leadingNullPages = mLeadingNullCount / mPageSize
        for (pageIndex in minimumPage..maximumPage) {
            val localPageIndex = pageIndex - leadingNullPages
            if (mPages[localPageIndex] == null) {
                mPages[localPageIndex] = (PLACEHOLDER_LIST as MutableList<T>)
                callback.onPagePlaceholderInserted(pageIndex)
            } else if (timeStamp != 0L) {
                callback.onPagePlaceholderInserted(pageIndex, timeStamp)
            }
        }
    }

    fun hasPage(pageSize: Int, index: Int): Boolean {
        val leadingNullPages = mLeadingNullCount / pageSize
        if (index < leadingNullPages || index >= leadingNullPages + mPages.size) {
            return false
        }
        val page = mPages[index - leadingNullPages]
        return page != null && page !== PLACEHOLDER_LIST
    }

    override fun toString(): String {
        val ret = StringBuilder("leading " + mLeadingNullCount + ", storage " + mStorageCount + ", trailing " + getTrailingNullCount())
        for (i in mPages.indices) {
            ret.append(" ").append(mPages[i])
        }
        return ret.toString()
    }

    companion object {
        private val PLACEHOLDER_LIST: List<*> = ArrayList<Any?>()
    }
}
