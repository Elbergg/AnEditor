package AnEditor


enum class KEYS(val key: Int){
    ARROW_LEFT(5000), ARROW_RIGHT(5001), ARROW_UP(5002),
    ARROW_DOWN(5003), PAGE_UP(2000), PAGE_DOWN(2001),
    HOME_KEY(2002), END_KEY(2003), DEL_KEY(2004),
    BACKSPACE(127)
}
class EditorKeyProcessor(private val editor: AnEditor) {
    private fun ctrl(key: Int): Int{
        return key and 0x1f
    }
    fun readKey() : Int {
        val key = System.`in`.read()
        if (key == 27)
        {
            val seq  = ByteArray(3)
            System.`in`.read(seq)
            if(seq[0].toInt() !=0 && seq[1].toInt() != 1)
            {
                if(seq[0].toInt().toChar() =='['){
                    if (seq[1].toInt().toChar() in '0'..'9')
                    {
                        if(seq[2].toInt().toChar() == '~'){
                            when(seq[1].toInt().toChar()){
                                '1'->return KEYS.HOME_KEY.key
                                '3'->return KEYS.DEL_KEY.key
                                '4'->return KEYS.END_KEY.key
                                '5'->return KEYS.PAGE_UP.key
                                '6'->return KEYS.PAGE_DOWN.key
                                '7'->return KEYS.HOME_KEY.key
                                '8'->return KEYS.END_KEY.key
                            }
                        }
                    }
                    when(seq[1].toInt().toChar()){
                        'A'->return KEYS.ARROW_UP.key
                        'B'->return KEYS.ARROW_DOWN.key
                        'C'->return KEYS.ARROW_RIGHT.key
                        'D'->return KEYS.ARROW_LEFT.key
                        'H'->return KEYS.HOME_KEY.key
                        'F'->return KEYS.END_KEY.key
                    }
                }
            }
        }
        return key
    }
    fun processKey() : Int {
        val c = readKey()
        when (c) {
            '\r'.code -> {editor.writer.insertNewLine()}
            ctrl(81)->{
                if(editor.notSaved) {
                    editor.gui.setStatusMessage(arrayOf("The file has unsaved changes, are you sure you want to quit? [y]"))
                    editor.gui.refreshScreen()
                    var status = readKey()
                    if(status == 'y'.code){
                        return 1
                    }
                    else{
                        editor.gui.setStatusMessage(arrayOf("HELP: Ctrl-Q = quit | Ctrl-S = save"))
                        return 0
                    }
                }
                else{
                    return 1
                }
            }
            KEYS.ARROW_UP.key ->editor.cursor.moveCursor(KEYS.ARROW_UP.key)
            KEYS.ARROW_DOWN.key ->editor.cursor.moveCursor(KEYS.ARROW_DOWN.key)
            KEYS.ARROW_LEFT.key ->editor.cursor.moveCursor(KEYS.ARROW_LEFT.key)
            KEYS.ARROW_RIGHT.key ->editor.cursor.moveCursor(KEYS.ARROW_RIGHT.key)
            KEYS.PAGE_UP.key, KEYS.PAGE_DOWN.key ->{
                if(c == KEYS.PAGE_UP.key){
                    editor.cursor_y = editor.rowOffset
                }else if(c == KEYS.PAGE_DOWN.key){
                    editor.cursor_y =  editor.rowOffset + editor.rows - 1
                    if(editor.cursor_y > editor.num_rows)
                        editor.cursor_y = editor.num_rows
                }
                var max = editor.rows
                while(max-- > 0){
                    editor.cursor.moveCursor(if (c == KEYS.PAGE_UP.key) KEYS.ARROW_UP.key else KEYS.ARROW_DOWN.key)
                }
            }
            KEYS.HOME_KEY.key->editor.cursor_x = 0
            KEYS.END_KEY.key->{
                if(editor.cursor_y < editor.num_rows)
                    editor.cursor_x = editor.in_rows[editor.cursor_y].length
            }
            ctrl('s'.code)->editor.io.save()
            KEYS.DEL_KEY.key->{editor.writer.delChar()}
            ctrl('h'.code), KEYS.BACKSPACE.key -> {editor.writer.bckspcChar()}
            ctrl('l'.code)->{}
            ''.code->{}
            else -> editor.writer.insertChar(c.toChar())
        }
        return 0
    }

}