package main

import main.MultiBoost.BaseLearnerType
import org.apache.spark.mllib.classification.multilabel.stronglearners.AdaBoostMHModel
import org.apache.spark.mllib.util.AdaBoostMHModelUtil
import org.apache.spark.{SparkConf, SparkContext}
import util.{FileUtil, GeneralArgParser}

/**
  * Created by glenn on 2018/4/9.
  */
object TestModel {

  var model_path = ""
  var data_path = ""
  var predict_path = ""
  var feature_size : Int = 0
  var class_num : Int = 0

  var model_type = ""

  var sample_ratio : Double = 1.0


  def initParam(args: Array[String]) = {
    val parser = new GeneralArgParser(args)
    model_path = parser.getStringValue("model_path")
    data_path = parser.getStringValue("data_path")
    predict_path = parser.getStringValue("predict_path")
    model_type = parser.getStringValue("model_type")

    feature_size = parser.getIntValue("feature_size", 0)
    class_num = parser.getIntValue("class_num", 0)
    sample_ratio = parser.getDoubleValue("sample_ratio", 1.0)
  }

  def checkInput(): Boolean = {
    if (feature_size <= 0 || class_num <= 0) false
    else if (model_path.isEmpty || data_path.isEmpty || predict_path.isEmpty) false
    else true
  }


  def main(args: Array[String]): Unit = {
    initParam(args)
    if(!checkInput()) throw new IllegalArgumentException("param error")

    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(conf)

    val bcFeatSize = sc.broadcast(feature_size)

    val data = sc.textFile(data_path)
      .sample(false, sample_ratio)
      .map(info => {
        val spInfo = info.split(" ")
        val key = spInfo(0)


        val features = new Array[Double](bcFeatSize.value)
        for(index <- 1 until spInfo.length) {
          val sp = spInfo(index).split(":")
          val pos = sp(0).toInt-1
          val value = sp(1).toDouble
          features(pos) = value
        }

        (key, features)
      })

    var model : AdaBoostMHModel[_] = null

    model_type.toLowerCase() match {
      case "cut" => model = AdaBoostMHModelUtil.load(model_path, BaseLearnerType.DecisionStump, class_num, feature_size)
      case "lr" => model = AdaBoostMHModelUtil.load(model_path, BaseLearnerType.DecisionStump, class_num, feature_size)
      case "svm" => model = AdaBoostMHModelUtil.load(model_path, BaseLearnerType.DecisionStump, class_num, feature_size)
    }

    val predict = AdaBoostMHModelUtil.predict2(model, data)

    FileUtil.deleteFile(sc.hadoopConfiguration, predict_path)

    predict.map(info => {
      var buffer = new StringBuffer()
      buffer.append(info._1)
      for (l <- info._2) {
        buffer.append(" ").append(l)
      }
      buffer.toString
    })
      .saveAsTextFile(predict_path)

    sc.stop()
  }

}
