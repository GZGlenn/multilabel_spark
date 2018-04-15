package test

import java.net.URI

import main.MultiBoost.BaseLearnerType
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.mllib.classification.multilabel.baselearners._
import org.apache.spark.mllib.classification.multilabel.stronglearners.{AdaBoostMHAlgorithm, AdaBoostMHModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.MultiLabeledPointParser.parseAnyToVector
import org.apache.spark.mllib.util.{AdaBoostMHModelUtil, ModelParameter, MultiLabeledPoint, MultiLabeledPointParser}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}
import util.GeneralArgParser

import scala.collection.mutable
import scala.util.Random

/**
  * Created by glenn on 2018/4/3.
  */
object TestMultiBoost {

  var input_path = ""
  var ouptut_path = ""

  def initParam(args: Array[String]) = {
    val parser = new GeneralArgParser(args)
    input_path = parser.getStringValue("input_path")
    ouptut_path = parser.getStringValue("ouptut_path")
  }

  def generateData(numClasses: Int, numFeatureDimensions: Int) : Array[MultiLabeledPoint] = {
    val numSample = 1000
    val data = new Array[MultiLabeledPoint](numSample)

    for (index <- 0 until numSample) {
      val labels = new Array[Double](numClasses)
      val features = new Array[Double](numFeatureDimensions)

      for (lIdx <- 0 until numClasses) {
        if ((new Random()).nextInt(10) >= 5) labels(lIdx) = 1.0
        else labels(lIdx) = 0.0
      }

      for (fIdx <- 0 until numFeatureDimensions) {
        features(fIdx) = new Random().nextGaussian()
      }

      data(index) = MultiLabeledPoint(Vectors.dense(labels), Vectors.dense(features))
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


    val numClasses = 4
    val numFeatureDimensions = 100

    val data = sc.parallelize(generateData(numClasses, numFeatureDimensions), 100)

    val modelParam = new ModelParameter(numClasses, numFeatureDimensions)
    modelParam.setNumBaseLearner(10)

    val model = modelParam.baseLearner match {
      case BaseLearnerType.DecisionStump =>
        val baseLearnerAlgo = new DecisionStumpAlgorithm(numClasses, numFeatureDimensions,
          modelParam.sampleRate, modelParam.featureRate)
        val strongLearnerAlgo = new AdaBoostMHAlgorithm[DecisionStumpModel, DecisionStumpAlgorithm](
          baseLearnerAlgo, numClasses, numFeatureDimensions, modelParam.getNumBaseLearner)
        strongLearnerAlgo.run(data)

      case BaseLearnerType.LRBase =>
        val binaryAlgo = new LRClassificationAlgorithm
        val baseLearnerAlgo = new GeneralizedBinaryBaseLearnerAlgorithm[LRClassificationModel, LRClassificationAlgorithm](numClasses, numFeatureDimensions, binaryAlgo)
        val strongLearnerAlgo = new AdaBoostMHAlgorithm[GeneralizedBinaryBaseLearnerModel[LRClassificationModel], GeneralizedBinaryBaseLearnerAlgorithm[LRClassificationModel, LRClassificationAlgorithm]](baseLearnerAlgo, numClasses, numFeatureDimensions, modelParam.getNumBaseLearner)
        strongLearnerAlgo.run(data)

      case BaseLearnerType.SVMBase =>
        val binaryAlgo = new SVMClassificationAlgorithm
        val baseLearnerAlgo = new GeneralizedBinaryBaseLearnerAlgorithm[SVMClassificationModel, SVMClassificationAlgorithm](numClasses, numFeatureDimensions, binaryAlgo)
        val strongLearnerAlgo = new AdaBoostMHAlgorithm[GeneralizedBinaryBaseLearnerModel[SVMClassificationModel], GeneralizedBinaryBaseLearnerAlgorithm[SVMClassificationModel, SVMClassificationAlgorithm]](baseLearnerAlgo, numClasses, numFeatureDimensions, modelParam.getNumBaseLearner)
        strongLearnerAlgo.run(data)
    }

    val predictsAndLabels = data.map { mlp =>
      ((model predict mlp.features).toArray, mlp.labels.toArray)
    }.cache()


    val preAndLabelColl = predictsAndLabels.collect()
    for (info <- preAndLabelColl) {
      for (index <- 0 until info._1.length)
        println(info._1(index) + " :: " + info._2(index))

      println("-----------")
    }

//
    val hammingLoss = computeHammingLoss(predictsAndLabels)
    val accuracy = computeAccuracy(predictsAndLabels)
    val strictAccuracy = computeStrictAccuracy(predictsAndLabels)
    val precision = computePrecision(predictsAndLabels)
    val recall = computeRecall(predictsAndLabels)
    val f1Score = computeF1Score(precision, recall)
//

    var resultStr = s"$model\n\n"
    resultStr += s"${model.debugString}\n\n"
    resultStr += s"Num of training samples: ${data.count()}\n"
    resultStr += s"Num of testing samples: ${data.count()}\n"
    resultStr += s"Testing hamming loss is: $hammingLoss\n"
    resultStr += s"Testing accuracy is: $accuracy\n"
    resultStr += s"Testing strict accuracy is: $strictAccuracy\n"
    resultStr += s"Testing precision is: $precision\n"
    resultStr += s"Testing recall is: $recall\n"
    resultStr += s"F1 score is: $f1Score\n"

    println(resultStr)

    sc.parallelize(resultStr).saveAsTextFile("./model/")

//
//    var resultStr = model.toString
//    println(resultStr.getBytes("UTF-8"))


//    val hadoopConf = new Configuration()
//    val fs = FileSystem.get(URI.create("./model/"), hadoopConf)
//    var dst = new Path("./model/model.txt")
//    val out = fs.create(dst)
//    out.write(resultStr.getBytes("UTF-8"))

    sc.stop()

  }


}
