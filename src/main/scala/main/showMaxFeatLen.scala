package main

import org.apache.spark.{SparkConf, SparkContext}
import util.GeneralArgParser

/**
  * Created by glenn on 2018/4/9.
  */
object showMaxFeatLen {

  var feat_path = ""

  def main(args: Array[String]): Unit = {
    val parser = new GeneralArgParser(args)
    feat_path = parser.getStringValue("feat_path")

    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(conf)

    val tmp = sc.textFile(feat_path)
      .map(info => {
        val spInfo = info.split("\t")
        spInfo
      })

    val collection = tmp.takeSample(false, 100)

    for (coll <- collection) println(coll(0) + "|" + coll(1) + "|" + coll(2))

    val result = tmp
      .map(info => {
        info.last.trim.toInt
      })

    println("max Feat len:" + result.max())
    println("min feat Len:" + result.min())

    sc.stop()
  }

}
