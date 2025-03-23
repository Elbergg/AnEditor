package AnEditor

class EditorFinder(private val editor: AnEditor) {
    var last_match = -1
    var direction = 1
    fun findCallback(query: String, key: Int){
        if (key == '\r'.code || key == ''.code) {
            last_match = -1
            direction = 1
            return
        }
        else if(key == KEYS.ARROW_RIGHT.key || key == KEYS.ARROW_DOWN.key){
            direction = 1
        }
        else if(key == KEYS.ARROW_LEFT.key || key == KEYS.ARROW_UP.key){
            direction = -1
        }
        else{
            last_match = -1
            direction = 1
        }
        if(last_match == -1)
            direction = 1
        var current = last_match
        for(i in 0 until editor.renders.size){
            current += direction
            if(current == -1)
                current = editor.num_rows -1
            else if(current == editor.num_rows)
                current = 0
            if (query in editor.renders[current]){
                last_match = current
                editor.cursor_y = current
                editor.cursor_x = editor.cursor.RxToCx(current, editor.renders[current].indexOf(query))
                editor.rowOffset = editor.num_rows
                break
            }
        }
    }
    fun find(){
        val saved_cx = editor.cursor_x
        val saved_cy = editor.cursor_y
        val saved_coloffset = editor.colOffset
        val saved_rowoffset = editor.rowOffset
        var query = editor.gui.prompt("Search: ", ::findCallback)
        if(query == "")
        {
            editor.cursor_x = saved_cx
            editor.cursor_y = saved_cy
            editor.colOffset = saved_coloffset
            editor.rowOffset = saved_rowoffset
            editor.gui.refreshScreen()
        }
        last_match = -1
        direction = 1
    }
}