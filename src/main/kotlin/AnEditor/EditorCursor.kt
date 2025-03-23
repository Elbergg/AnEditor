package AnEditor

class EditorCursor (private val editor: AnEditor) {
    fun moveCursor(key: Int){
        var row = if (editor.cursor_y >= editor.num_rows) null else editor.in_rows[editor.cursor_y]
        when (key){
            KEYS.ARROW_DOWN.key->{
                if(editor.cursor_y<editor.num_rows)
                    editor.cursor_y++
            }
            KEYS.ARROW_UP.key->{
                if(editor.cursor_y!=0)
                    editor.cursor_y--
            }
            KEYS.ARROW_LEFT.key->{
                if(editor.cursor_x!= 0) {
                    editor.cursor_x--
                }else if(editor.cursor_y > 0){
                    editor.cursor_y--
                    editor.cursor_x=editor.in_rows[editor.cursor_y].length
                }
            }
            KEYS.ARROW_RIGHT.key-> {
                if(row != null && editor.cursor_x < row.length) {
                    editor.cursor_x++
                }else if (row != null && editor.cursor_x == row.length){
                    editor.cursor_y++
                    editor.cursor_x = 0
                }
            }
        }
        row = if(editor.cursor_y >= editor.num_rows) null else editor.in_rows[editor.cursor_y]
        val rowlen = row?.length ?: 0
        if(editor.cursor_x > rowlen){
            editor.cursor_x = rowlen
        }
    }
    fun CxToRx(row_idx: Int, cx: Int ): Int
    {
        var rx = 0
        for(j in 0..<cx){
            if (editor.in_rows[row_idx][j] == '\t')
                rx += (RENDER_CONSTANTS.TAB_STOP.value - 1) - (rx % RENDER_CONSTANTS.TAB_STOP.value)
            rx++
        }
        return rx
    }

    fun RxToCx(row_idx: Int, rx: Int): Int
    {
        var curr_rx = 0
        var cx = 0
       while(cx < editor.in_rows[row_idx].length) {
            if(editor.in_rows[row_idx][cx] == '\t')
                curr_rx += (RENDER_CONSTANTS.TAB_STOP.value - 1) - (curr_rx % RENDER_CONSTANTS.TAB_STOP.value)
            curr_rx++
            if (curr_rx > rx)
                return cx
            cx++
        }
        return cx
    }
}