package AnEditor

import AnEditor.AnEditor.KEYS

class EditorKeyProcceser(val editor: AnEditor) {
    private fun ctrl(key: Int): Int{
        return key and 0x1f
    }
    fun readKey() : Int {
        val key = System.`in`.read()
        if (key == 27)
        {
            var seq  = ByteArray(3)
            System.`in`.read(seq)
            if(seq[0].toInt() !=0 && seq[1].toInt() != 1)
            {
                if(seq[0].toInt().toChar() =='['){
                    if (seq[1].toInt().toChar() >= '0' && seq[1].toInt().toChar() <= '9')
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
            ctrl(81)->return 1
            KEYS.ARROW_UP.key ->moveCoursor(KEYS.ARROW_UP.key)
            KEYS.ARROW_DOWN.key ->moveCoursor(KEYS.ARROW_DOWN.key)
            KEYS.ARROW_LEFT.key ->moveCoursor(KEYS.ARROW_LEFT.key)
            KEYS.ARROW_RIGHT.key ->moveCoursor(KEYS.ARROW_RIGHT.key)
            KEYS.PAGE_UP.key, KEYS.PAGE_DOWN.key ->{
                var max = editor.rows
                while(max-- != 0){
                    moveCoursor(if (c == KEYS.PAGE_UP.key) KEYS.ARROW_UP.key else KEYS.ARROW_DOWN.key)
                }
            }
            KEYS.HOME_KEY.key->editor.coursor_x = 1
            KEYS.END_KEY.key->editor.coursor_x = editor.cols-1
        }
        return 0
    }
    fun moveCoursor(key: Int){
        when (key){
            KEYS.ARROW_DOWN.key->{
                if(editor.coursor_y<editor.num_rows)
                    editor.coursor_y++
            }
            KEYS.ARROW_UP.key->{
                if(editor.coursor_y!=0)
                    editor.coursor_y--
            }
            KEYS.ARROW_LEFT.key->{
                if(editor.coursor_x!=0)
                    editor.coursor_x--
            }
            KEYS.ARROW_RIGHT.key->{
                if(editor.coursor_x!=editor.cols-1)
                    editor.coursor_x++
            }
        }
    }
}