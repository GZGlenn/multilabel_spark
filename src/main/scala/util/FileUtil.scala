package util

import java.io.{BufferedReader, IOException, InputStreamReader}
import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FSDataInputStream, FSDataOutputStream, FileSystem, Path}
import org.apache.hadoop.util.Progressable


/**
  * Created by glennbinhu on 2017/8/26.
  */
object FileUtil {

  val DEFAULTURI = "hdfs://tl-nn-tdw.tencent-distribute.com:54310"

  @throws[IOException]
  def createPath(conf: Configuration, file: String, uri: String = DEFAULTURI): Unit = {
    val fs = FileSystem.get(new URI(uri), conf)
    try {
      fs.mkdirs(new Path(file))
      MyLoggerUtil.info("finish create path:" + file)
    }
  }

  @throws[IOException]
  def createFile(conf: Configuration, file: String, content: String, uri: String = DEFAULTURI): Boolean = {
    var isSucc = false
    val fs = FileSystem.get(new URI(uri), conf)
    val buff = content.getBytes
    var os : FSDataOutputStream = null
    try {
      os = fs.create(new Path(file))
      os.write(buff, 0, buff.length)
      os.flush()
      MyLoggerUtil.info("finish create file:" + file)
      isSucc = true
      isSucc
    } finally {
      if (os != null) os.close()
      isSucc
    }
  }

  def isEmptyPath(conf: Configuration, pathStr : String, uri: String = DEFAULTURI): Boolean = {
    val fs = FileSystem.get(new URI(uri), conf)
    val path = new Path(pathStr)
    if (!fs.exists(path)) return true

    if (fs.isDirectory(path)) {
      val status = fs.listStatus(path)
      if (status.isEmpty) return true
      else if (status(0).getBlockSize == 0) return true
      else return false
    }
    else {
      val status = fs.getFileStatus(path)
      if (status.getBlockSize == 0) return true
      else return false
    }
  }

  @throws[IOException]
  def getFilesUnderFolder(conf: Configuration, folderPath: String, pattern: String,
                          uri: String = DEFAULTURI): Array[String] = {
    val fs = FileSystem.get(new URI(uri), conf)
    var paths = Array[String]()
    val rootFolder = new Path(folderPath)
    if (fs.exists(rootFolder)) {
      val fileStatus = fs.listStatus(rootFolder)
      var i = 0
      while (i < fileStatus.length) {
        val fileStatu = fileStatus(i)
        if (!fileStatu.isDir) {
          val oneFilePath = fileStatu.getPath
          if (pattern == null) paths = paths :+ oneFilePath.toString
          else if (oneFilePath.getName.contains(pattern)) paths = paths :+ oneFilePath.toString
          i += 1
        }
      }
    }
    paths
  }

  @throws[IOException]
  def wirteFile(conf: Configuration, path: String, content: String, uri: String = DEFAULTURI): Boolean = {
    var isSucc = false
    var os : FSDataOutputStream = null
    val fs = FileSystem.get(new URI(uri), conf)
    if (!fs.exists(new Path(path))) createFile(conf, path, content, uri)
    else {
      try {
        os = fs.append(new Path(path))
        val buff = content.getBytes
        os.write(buff, 0, buff.length)
        os.flush()
        val out = fs.create(new Path(path), new Progressable() {
          private[util] var fileCount = 0

          override

          def progress(): Unit = {
            fileCount += 1
            MyLoggerUtil.info("wirte data process:" + fileCount)
          }
        })
        isSucc = true
        isSucc
      } finally {
        if (os != null) os.close()
        isSucc
      }
    }
  }

  @throws[IOException]
  def readFile(conf: Configuration, path: String, uri: String = DEFAULTURI): Array[String] = {
    var lineList = Array[String]()
    var fsr : FSDataInputStream = null
    var bufferedReader : BufferedReader = null
    var lineTxt : String = ""
    try {
      val fs = FileSystem.get(new URI(uri), conf)
      fsr = fs.open(new Path(path))
      bufferedReader = new BufferedReader(new InputStreamReader(fsr))
      do {
        lineTxt = bufferedReader.readLine
        if (lineTxt != null && !lineTxt.equals("")) lineList = lineList :+ lineTxt
      } while (lineTxt != null)
    } catch {
      case e: Exception =>
        e.printStackTrace()
    } finally if (bufferedReader != null) try
      bufferedReader.close()
    catch {
      case e: IOException =>
        MyLoggerUtil.warn("read file Failed:" + e.getMessage)
    }
    lineList
  }

  @throws[IOException]
  def isExist(conf: Configuration, path: String, uri: String = DEFAULTURI): Boolean = {
    var fs : FileSystem = null
    var isExists : Boolean = false
    try {
      val p = new Path(path)
      fs = p.getFileSystem(conf)
      isExists = fs.exists(p)
    } catch {
      case e: Exception =>
        MyLoggerUtil.warn("file not exists: ", e.getMessage())
    }
    return isExists
  }

  @throws[IOException]
  def deleteFile(conf: Configuration, ioPath: String, uri: String = DEFAULTURI): Boolean = {
    if (!isExist(conf, ioPath, uri)) return true
    var isDelete = false
    var fs : FileSystem = null
    try {
      val fs = FileSystem.get(new URI(uri), conf)
      isDelete = fs.delete(new Path(ioPath), true)
    } catch {
      case e: Exception =>
        e.printStackTrace()
        isDelete = false
    }
    isDelete
  }


}
