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
    fun save(): Int{
        if(editor.fileName == "")
            return -1
        val buf = editor.rowmng.rowsToString()
        val file = File(editor.fileName)
        try{file.writeText(buf)}catch(e:IOException){
            editor.gui.setStatusMessage(arrayOf("Error saving file"))
            return -1
        }
        editor.gui.setStatusMessage(arrayOf("File saved successfully"))
        editor.notSaved = false
        return 0
    }

}