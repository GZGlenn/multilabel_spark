package test

import main.MultiBoost.BaseLearnerType
import test.TestModelUtil.generateTestData
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.{AdaBoostMHModelUtil, MultiLabeledPoint}
import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random

/**
  * Created by glenn on 2018/4/9.
  */
object TestLoadModel {

  def generateTestData(numClasses: Int, numFeatureDimensions: Int) : Array[MultiLabeledPoint] = {
    val numSample = 1000
    val data = new Array[MultiLabeledPoint](numSample)

    for (index <- 0 until numSample) {
      val labels = new Array[Double](numClasses)
      val features = new Array[Double](numFeatureDimensions)

      for (lIdx <- 0 until numClasses) {
        if ((new Random()).nextInt(10) >= 5) labels(lIdx) = 1.0
        else labels(lIdx) = -1.0
      }

      for (fIdx <- 0 until numFeatureDimensions) {
        features(fIdx) = new Random().nextGaussian()
      }

      data(index) = MultiLabeledPoint(Vectors.dense(labels), Vectors.dense(features))
    }
    data
  }

  def main(args: Array[String]): Unit = {
    val path = "./model/"

    val conf = new SparkConf().setAppName(this.getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val model = AdaBoostMHModelUtil.load(path, BaseLearnerType.DecisionStump, 3, 1000)
    val testData = sc.parallelize(generateTestData(3, 1000), 100)
    val testResult = AdaBoostMHModelUtil.test(model, testData).collect()

    for (index <- 0 until testResult.length) {
      println("---------------------")
      for (sIdx <- 0 until testResult(index)._1.length) {
        println(testResult(index)._1(sIdx) + "  :::  " + testResult(index)._2(sIdx))
      }
    }

    sc.stop()
  }

}
