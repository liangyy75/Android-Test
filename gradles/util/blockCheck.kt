import java.io.File

fun File?.allFiles(): MutableList<File> {
    if (this != null && this.exists()) {
        when {
            this.isDirectory -> {
                val files = this.listFiles()
                val result = mutableListOf<File>()
                files.forEach { file ->
                    if (file.isDirectory) {
                        result.addAll(file.allFiles())
                    } else {
                        result.add(file)
                    }
                }
                return result
            }
            else -> return mutableListOf(this)
        }
    }
    return mutableListOf<File>()
}

open class MutablePair<T1, T2>(open var first: T1, open var second: T2)

fun handle(file: File, map: MutableMap<String, MutablePair<MutableMap<String, Int>, MutableMap<String, Int>>>) {
    file.forEachLine { line ->
        // TODO:
    }
}

fun main(args: Array<String>?) {
    if (args.isNullOrEmpty()) {
        System.err.println("\u001b[31mwrong args, need dir path or file path")
        return
    }
    if (args.size > 1) {
        System.err.println("\u001b[31mwrong args, dont't need too match arg")
        return
    }
    val file = File(args[0])
    if (!file.exists()) {
        System.err.println("\u001b[31mpath not exists")
        return
    }

    val map: MutableMap<String, MutablePair<MutableMap<String, Int>, MutableMap<String, Int>>> = mutableMapOf()
    if (file.isDirectory) {
        file.allFiles().forEach { handle(file, map) }
    } else {
        handle(file, map)
    }
    println(map.size)
}
// kotlinc blockCheck.kt -include-runtime -d blockCheck.jar && kotlin blockCheck.jar ~/Desktop/hotsoon-lites/hotsoon-lite2/components/comment/comment/src/main/java/com/ss/android/ugc/live
