package AnEditor
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class EditorIO (private val editor: AnEditor) {
    fun open(fileName: String){
        try {
            val lines = File(fileName).readLines()
            editor.num_rows = lines.size
            editor.in_rows = lines.toMutableList()
            editor.renders = lines.toMutableList()
            editor.rowmng.updateRows()
            editor.lineNumOffset = lines.size.toString().length
        }catch(e: FileNotFoundException){
            editor.die("File not found")
        }catch(e: IOException){
            editor.die("IOError")
        }
    }
    fun initEmpty(){
        editor.num_rows = 1
        editor.in_rows = arrayOf("").toMutableList()
        editor.renders = arrayOf("").toMutableList()
        editor.lineNumOffset = editor.rows.toString().length

    }
    fun save(): Int{
        if(editor.fileName == "") {
            editor.fileName = editor.gui.prompt("Save as: ").trim()
            if(editor.fileName == "") {
                editor.gui.setStatusMessage(arrayOf("File not saved"))
                return 0
            }
        }
        val buf = editor.rowmng.rowsToString()
        val file = File(editor.fileName)
        try{file.writeText(buf)}catch(e:IOException){
            editor.gui.setStatusMessage(arrayOf("Error saving file"))
            return -1
        }
        editor.gui.setStatusMessage(arrayOf("File saved successfully"))
        editor.notSaved = false
        editor.gui.refreshScreen()
        return 0
    }

}