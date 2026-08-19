package abdullah.bari.asif.ui.utils

actual object PlatformShare {
    actual fun shareText(text: String, title: String?) {
        try {
            val selection = java.awt.datatransfer.StringSelection(text)
            val clipboard = java.awt.Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(selection, selection)
            println("Copied to clipboard: $text")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
