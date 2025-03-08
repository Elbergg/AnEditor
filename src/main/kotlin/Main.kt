import AnEditor.AnEditor

import kotlin.system.exitProcess

fun main() {
    val editor = AnEditor()
    editor.enableRaw()
    while(true){
        val key = System.`in`.read()
        if (key == 27){
            editor.disableRaw()
            exitProcess(0)
        }
        println(key)
        println(key.toChar())
    }
}


