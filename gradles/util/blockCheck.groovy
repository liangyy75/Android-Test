import groovy.io.FileType

String[] args = this.args
if (args == null || args.length == 0) {
    System.err.println("\u001b[31mwrong args, need dir path or file path")
    return
}
if (args.length > 1) {
    System.err.println("\u001b[31mwrong args, dont't need too match arg")
    return
}

File file = new File(args[0])
if (!file.exists()) {
    System.err.println("\u001b[31mpath not exists")
    return
}

HashMap<String, List<Tuple2<String, Integer>>> map = new HashMap<>()

if (file.isDirectory()) {
    file.eachFileRecurse(FileType.FILES) { subFile ->
        subFile.eachLine { line ->
            if (line.indexOf("putData")) {
            } else if (line.indexOf("getObservable")) {
            }
        }
    }
} else {
    println("isFile")
}
