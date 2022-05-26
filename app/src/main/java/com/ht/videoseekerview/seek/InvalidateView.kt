package com.ht.videoseekerview.seek


fun SeekVideoView.calculatePositionThumb() {
    var offset = 0f
    listThumb.forEach {
        it.currentScale = currentScale
        it.dirty = true
        it.xOffSetWithParent = offset
        offset += it.getDesiredWidth()
    }
}