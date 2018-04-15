package test

import test.TestMultiBoost._
import _root_.main.MultiBoost.BaseLearnerType
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.{AdaBoostMHModelUtil, ModelParameter, MultiLabeledPoint}
import org.apache.spark.rdd.RDD

import scala.util.Random

/**
  * Created by glenn on 2018/4/4.
  */
object TestModelUtil {

  def generateTestData(numClasses: Int, numFeatureDimensions: Int) : Array[MultiLabeledPoint] = {
    val numSample = 100
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

  def generatePredictData(numClasses: Int, numFeatureDimensions: Int) : Array[Array[Double]] = {
    val numSample = 100
    val data = new Array[Array[Double]](numSample)

    for (index <- 0 until numSample) {
      val features = new Array[Double](numFeatureDimensions)

      for (fIdx <- 0 until numFeatureDimensions) {
        features(fIdx) = new Random().nextGaussian()
      }

      data(index) = features
    }
    data
  }

  def computeHammingLoss(predictsAndLabels: RDD[(Array[Double], Array[Double])]): Double = {
    predictsAndLabels.flatMap {
      case (ps, ls) =>
        (ps zip ls) filter {
          case (p, l) =>
            p * l < 0.0
        }
    }.count().toDouble /
      (predictsAndLabels.count() * predictsAndLabels.take(1)(0)._2.size)
  }

  def computePrecision(predictsAndLabels: RDD[(Array[Double], Array[Double])]): Double = {
    val positiveSet = predictsAndLabels.flatMap {
      case (predicts, labels) =>
        (predicts zip labels) filter (_._1 > 0.0)
    }

    positiveSet.filter {
      case (predict, label) =>
        predict * label > 0.0
    }.count().toDouble / positiveSet.count()
  }

  def computeRecall(predictsAndLabels: RDD[(Array[Double], Array[Double])]): Double = {
    val trueSet = predictsAndLabels.flatMap {
      case (predicts, labels) =>
        (predicts zip labels) filter (_._2 > 0.0)
    }

    trueSet.filter {
      case (predict, label) =>
        predict * label > 0.0
    }.count().toDouble / trueSet.count()
  }

  def computeAccuracy(predictsAndLabels: RDD[(Array[Double], Array[Double])]): Double = {
    val total = predictsAndLabels.flatMap {
      case (predicts, labels) =>
        predicts zip labels
    }

    total.filter { case (p, l) => p * l > 0.0 }.count().toDouble / total.count()
  }

  def computeStrictAccuracy(predictsAndLabels: RDD[(Array[Double], Array[Double])]): Double = {
    predictsAndLabels.filter {
      case (predicts, labels) =>
        (predicts zip labels).count {
          case (p, l) =>
            p * l > 0.0
        } == predicts.size
    }.count().toDouble / predictsAndLabels.count().toDouble
  }

  def computeF1Score(precision: Double, recall: Double): Double = {
    if (precision + recall > 0.0) 2.0 * precision * recall / (precision + recall) else 0.0
  }

  def main(args: Array[String]): Unit = {
    //    initParam(args)

    val conf = new SparkConf().setAppName(this.getClass.getSimpleName).setMaster("local")
    val sc = new SparkContext(conf)

    val numClasses = 3
    val numFeatureDimensions = 1000

    val trainData = sc.parallelize(generateTestData(numClasses, numFeatureDimensions), 100)

    val trainCollect = trainData.takeSample(false, 100)
    for (coll <- trainCollect) {
      println(coll.labels.toString)
    }

    val modelParam = new ModelParameter(numClasses, numFeatureDimensions)
    modelParam.setNumBaseLearner(30)
    modelParam.setBaseLearner(BaseLearnerType.DecisionStump)

    val algorithm = AdaBoostMHModelUtil.creatModel(modelParam)
    val model = AdaBoostMHModelUtil.train(algorithm, trainData)
    val testResult = AdaBoostMHModelUtil.test(model, trainData)

    val testColl = testResult.takeSample(false, 100)
    println("testData:")
    for (idx <- 0 until testColl.length) {
      for (sIdx <- 0 until testColl(idx)._1.length) {
        println(testColl(idx)._1(sIdx) + " :: " + testColl(idx)._2(sIdx))
      }
      println("-------------------")
    }

    val predictData = sc.parallelize(generatePredictData(numClasses, numFeatureDimensions))
    val predictResult = AdaBoostMHModelUtil.predict(model, predictData)

    val preColl = predictResult.takeSample(false, 100)
    println("preData:")
    for (idx <- 0 until preColl.length) {
      for (sIdx <- 0 until preColl(idx)._2.length) {
        println(preColl(idx)._2(sIdx))
      }
      println("-------------------")
    }

    val hammingLoss = computeHammingLoss(testResult)
    val accuracy = computeAccuracy(testResult)
    val strictAccuracy = computeStrictAccuracy(testResult)
    val precision = computePrecision(testResult)
    val recall = computeRecall(testResult)
    val f1Score = computeF1Score(precision, recall)

    var resultStr = s"$model\n\n"
    resultStr += s"${model.debugString}\n\n"
    resultStr += s"Num of training samples: ${testResult.count()}\n"
    resultStr += s"Num of testing samples: ${testResult.count()}\n"
    resultStr += s"Testing hamming loss is: $hammingLoss\n"
    resultStr += s"Testing accuracy is: $accuracy\n"
    resultStr += s"Testing strict accuracy is: $strictAccuracy\n"
    resultStr += s"Testing precision is: $precision\n"
    resultStr += s"Testing recall is: $recall\n"
    resultStr += s"F1 score is: $f1Score\n"

    println(resultStr)

    AdaBoostMHModelUtil.save(model, "./model/")

    sc.stop()

  }


}
