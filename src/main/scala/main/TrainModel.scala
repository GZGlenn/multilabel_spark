package main

import main.MultiBoost.BaseLearnerType
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.{AdaBoostMHModelUtil, ModelParameter, MultiLabeledPoint}
import org.apache.spark.{SparkConf, SparkContext}
import util.{FileUtil, GeneralArgParser}

/**
  * Created by glenn on 2018/4/9.
  */
object TrainModel {

  var libsvm_path = ""
  var feature_size : Int = 0
  var class_num : Int = 0
  var model_path = ""

  var model_type = ""
  var model_num : Int = 10

  var sample_ratio : Double = 1.0

  def initParam(args: Array[String]) = {
    val parser = new GeneralArgParser(args)
    libsvm_path = parser.getStringValue("libsvm_path")
    model_path = parser.getStringValue("model_path")
    model_type = parser.getStringValue("model_type")

    feature_size = parser.getIntValue("feature_size", 0)
    class_num = parser.getIntValue("class_num", 0)
    model_num = parser.getIntValue("model_num", 0)
    sample_ratio = parser.getDoubleValue("sample_ratio", 1.0)
  }

  def checkInput(): Boolean = {
    if (feature_size <= 0 || class_num <= 0 || model_num <= 0) false
    else true
  }

  def main(args: Array[String]): Unit = {
    initParam(args)
    if(!checkInput()) throw new IllegalArgumentException("param error")

    val conf = new SparkConf().setAppName(this.getClass.getSimpleName)
    val sc = new SparkContext(conf)

    val bcFeatSize = sc.broadcast(feature_size)

    val data = sc.textFile(libsvm_path)
      .sample(false, sample_ratio)
      .map(info => {
      val spInfo = info.split(" ")
      val labelsStr = spInfo(0).split(",")

      val labels = new Array[Double](labelsStr.length)
      for (index <- 0 until labelsStr.length) {
        labels(index) = labelsStr(index).toDouble
        if (labels(index) == 0) labels(index) = -1
      }

      val features = new Array[Double](bcFeatSize.value)
      for(index <- 1 until spInfo.length) {
        val sp = spInfo(index).split(":")
        val pos = sp(0).toInt-1
        val value = sp(1).toDouble
        features(pos) = value
      }

      MultiLabeledPoint(Vectors.dense(labels), Vectors.dense(features))
    })


    val modelParam = new ModelParameter(class_num, feature_size)
    modelParam.setNumBaseLearner(model_num)
    model_type.toLowerCase match {
      case "cut" => modelParam.setBaseLearner(BaseLearnerType.DecisionStump)
      case "lr" => modelParam.setBaseLearner(BaseLearnerType.LRBase)
      case "svm" => modelParam.setBaseLearner(BaseLearnerType.SVMBase)
    }

    val algorithm = AdaBoostMHModelUtil.creatModel(modelParam)
    val model = AdaBoostMHModelUtil.train(algorithm, data)

    FileUtil.deleteFile(sc.hadoopConfiguration, model_path)
    AdaBoostMHModelUtil.save(model, model_path)

    sc.stop()
  }

}
