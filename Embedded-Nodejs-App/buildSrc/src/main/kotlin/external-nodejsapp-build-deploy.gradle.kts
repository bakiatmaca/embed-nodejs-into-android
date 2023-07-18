import org.gradle.internal.os.OperatingSystem;
import java.security.MessageDigest

val EXTERNAL_PROJECT_PATH = "../../Nodejs-App/"
val EXTERNAL_APP_BIN_FILE = "index.js"

val ASSET_PATH = "src/main/assets/"
val APP_PACKAGE_FILE = "deploy.zip"

tasks.register("ext-build-deploy") {
    dependsOn("ext-package")

    doLast{
        if (isPackageNewest()) {

            //deploy
            sync {
                from(EXTERNAL_PROJECT_PATH + "dist/" + APP_PACKAGE_FILE)
                into(ASSET_PATH)
            }

            //newest version copy to cache
            sync {
                from(EXTERNAL_PROJECT_PATH + "build/" + EXTERNAL_APP_BIN_FILE)
                into(EXTERNAL_PROJECT_PATH + "dist/cache/")
            }

            println("A New Node.js app package $APP_PACKAGE_FILE deploy")
        } else {
            println("Node.js app package is the newest. No changes")
        }
    }
}

tasks.register<Zip>("ext-package") {
    dependsOn("ext-nodejs-build")

    into("app") {
        from(EXTERNAL_PROJECT_PATH + "build/" + EXTERNAL_APP_BIN_FILE)
        into("meta") {
            from(EXTERNAL_PROJECT_PATH + "build/meta/")
        }
        /*
        into("node_modules") {
            from(EXTERNAL_PROJECT_PATH + "node_modules/")
            exclude(".bin")
        }
        */
    }

    destinationDirectory.set(File((EXTERNAL_PROJECT_PATH + "dist/")))
    archiveFileName.set(APP_PACKAGE_FILE)
}

tasks.register("ext-nodejs-build") {
    dependsOn("ext-delete-old-files")

    doLast{
        if (OperatingSystem.current().isLinux()) {
            exec {
                workingDir(EXTERNAL_PROJECT_PATH)
                commandLine("bash", "-c", "npx webpack")
                setIgnoreExitValue(false)
            }
        } else {
            exec {
                workingDir(EXTERNAL_PROJECT_PATH)
                commandLine("cmd", "/c", "npx webpack")
                setIgnoreExitValue(false)
            }
        }
    }
}

tasks.register("ext-delete-old-files") {
    doFirst{
        delete(EXTERNAL_PROJECT_PATH + "build/index.js")
        delete(EXTERNAL_PROJECT_PATH + "build/index.js.map")
    }
}

tasks.register("ext-delete-package-file") {
    doFirst{
        delete(ASSET_PATH + APP_PACKAGE_FILE)
    }
}

fun isPackageNewest(): Boolean {
    val hashnew = md5Hash( projectDir.absolutePath + "/" + EXTERNAL_PROJECT_PATH + "build/" + EXTERNAL_APP_BIN_FILE)
    val hashcache = md5Hash( projectDir.absolutePath + "/" + EXTERNAL_PROJECT_PATH + "dist/cache/" + EXTERNAL_APP_BIN_FILE)
    //println("hash new build file:\t\t" + hashnew)
    //println("hash current build file:\t" + hashcache)

    return ((!File(projectDir.absolutePath + "/" + ASSET_PATH + APP_PACKAGE_FILE).exists())
            || hashcache.equals("#") || (!hashnew.equals(hashcache)))
}

fun md5Hash(fileName: String): String {
    val md = MessageDigest.getInstance("MD5")

    val data = readFile(fileName)
    if (data == null)
        return "#"

    val bigInt = BigInteger(1, md.digest(data))
    return String.format("%02x", bigInt)
}

fun readFile(fileName: String): ByteArray? {
    val f = File(fileName)

    if (!f.exists())
        return null

    return f.inputStream().readBytes();
}

