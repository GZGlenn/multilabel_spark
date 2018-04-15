package util

import java.io.{InputStream, OutputStream}
import java.net.URI

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}

/**
  * Created by groverli on 2016/6/15.
  */
object HdfsUtil {

  /**
    * Remove hdfs file, if path is a Directory, remove it and all files in it.
    *
    * @param path hdfs path to be removed
    */
  def rmHdfsFile(path: String) = {
    val cluster_name = getHdfsClusterName(path)
    if (cluster_name == null) {
      throw new IllegalArgumentException("hdfs cluster name parse error")
    }
    val hdfs = FileSystem.get(new URI(cluster_name), new Configuration())
    val term = new Path(path)
    if (hdfs.exists(term))
      hdfs.delete(term, true)
  }

  /**
    * Return boolean:
    * true - hdfs path is exist.
    * false - hdfs path is not exist.
    *
    * @param path hdfs path
    */
  def isExistHdfsFile(path: String): Boolean = {
    val cluster_name = getHdfsClusterName(path)
    if (cluster_name == null) {
      throw new IllegalArgumentException("hdfs cluster name parse error")
    }
    val hdfs = FileSystem.get(new URI(cluster_name), new Configuration())
    val term = new Path(path)
    hdfs.exists(term)
  }

  private def getHdfsClusterName(path: String): String = {
    if (path.startsWith("hdfs://")) {
      val pos = path.indexOf("/", 7)
      if (pos > 0) {
        return path.substring(0, pos)
      }
    }
    null
  }

  /**
    * Return a String array
    * list all files in hdfs path
    *
    * @param path hdfs path
    */
  def listHdfsFile(path: String): Array[String] = {
    val cluster_name = getHdfsClusterName(path)
    if (cluster_name == null) {
      throw new IllegalArgumentException("hdfs cluster name parse error")
    }
    val hdfs = FileSystem.get(new URI(cluster_name), new Configuration())
    hdfs.listStatus(new Path(path)).filter(status => {
      status.isFile
    }).map(status => {
      status.getPath.getName
    })
  }


  /**
    * Return a String array
    * list all directories in hdfs path
    *
    * @param path hdfs path
    */
  def listHdfsDir(path: String): Array[String] = {
    val cluster_name = getHdfsClusterName(path)
    println("cluster: " + cluster_name)
    if (cluster_name == null) {
      throw new IllegalArgumentException("hdfs cluster name parse error")
    }
    val hdfs = FileSystem.get(new URI(cluster_name), new Configuration())
    hdfs.listStatus(new Path(path)).filter(status => {
      status.isDirectory
    }).map(status => {
      status.getPath.getName
    })
  }

  def getInputStream(path: String): InputStream = {
    val cluster_name = getHdfsClusterName(path)
    if (cluster_name == null) {
      throw new IllegalArgumentException("hdfs cluster name parse error")
    }
    val hdfs = FileSystem.get(new URI(cluster_name), new Configuration())
    val h_path = new Path(path)
    if (!hdfs.exists(h_path)) {
      throw new IllegalArgumentException("hdfs file not exist")
    }
    hdfs.open(h_path)
  }

  def getOutputStream(path: String): OutputStream = {
    val cluster_name = getHdfsClusterName(path)
    if (cluster_name == null) {
      throw new IllegalArgumentException("hdfs cluster name parse error")
    }
    val hdfs = FileSystem.get(new URI(cluster_name), new Configuration())
    val h_path = new Path(path)
    hdfs.create(h_path, true)
  }
}
