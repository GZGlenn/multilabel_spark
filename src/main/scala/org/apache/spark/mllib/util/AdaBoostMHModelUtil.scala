package org.apache.spark.mllib.util


import main.MultiBoost.BaseLearnerType
import main.MultiBoost.BaseLearnerType.BaseLearnerType
import org.apache.spark.SparkContext
import org.apache.spark.mllib.classification.multilabel.baselearners._
import org.apache.spark.mllib.classification.multilabel.stronglearners.{AdaBoostMHAlgorithm, AdaBoostMHModel}
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD

/**
  * Created by glenn on 2018/4/4.
  */
object AdaBoostMHModelUtil {

  def creatModel(modelParam: ModelParameter): AdaBoostMHAlgorithm[_,_] = {

    val model = modelParam.baseLearner match {
      case BaseLearnerType.DecisionStump =>
        val baseLearnerAlgo = new DecisionStumpAlgorithm(modelParam.numClass, modelParam.featureSize,
          modelParam.sampleRate, modelParam.featureRate)
        val strongLearnerAlgo = new AdaBoostMHAlgorithm[DecisionStumpModel, DecisionStumpAlgorithm](
          baseLearnerAlgo, modelParam.numClass, modelParam.featureSize, modelParam.getNumBaseLearner)
        strongLearnerAlgo

      case BaseLearnerType.LRBase =>
        val binaryAlgo = new LRClassificationAlgorithm
        val baseLearnerAlgo = new GeneralizedBinaryBaseLearnerAlgorithm[LRClassificationModel, LRClassificationAlgorithm](modelParam.numClass, modelParam.featureSize, binaryAlgo)
        val strongLearnerAlgo = new AdaBoostMHAlgorithm[GeneralizedBinaryBaseLearnerModel[LRClassificationModel], GeneralizedBinaryBaseLearnerAlgorithm[LRClassificationModel, LRClassificationAlgorithm]](baseLearnerAlgo, modelParam.numClass, modelParam.featureSize, modelParam.getNumBaseLearner)
        strongLearnerAlgo

      case BaseLearnerType.SVMBase =>
        val binaryAlgo = new SVMClassificationAlgorithm
        val baseLearnerAlgo = new GeneralizedBinaryBaseLearnerAlgorithm[SVMClassificationModel, SVMClassificationAlgorithm](modelParam.numClass, modelParam.featureSize, binaryAlgo)
        val strongLearnerAlgo = new AdaBoostMHAlgorithm[GeneralizedBinaryBaseLearnerModel[SVMClassificationModel], GeneralizedBinaryBaseLearnerAlgorithm[SVMClassificationModel, SVMClassificationAlgorithm]](baseLearnerAlgo, modelParam.numClass, modelParam.featureSize, modelParam.getNumBaseLearner)
        strongLearnerAlgo
    }

    return model
  }

  def train(model : AdaBoostMHAlgorithm[_,_], data: RDD[MultiLabeledPoint]) : AdaBoostMHModel[_] = {
    val result = model.run(data)
    result.asInstanceOf[AdaBoostMHModel[_]]
  }

  def test(model : AdaBoostMHModel[_], data: RDD[MultiLabeledPoint]): RDD[(Array[Double], Array[Double])] = {
    data.map(info => (model.predict(info.features).toArray, info.labels.toArray))
  }

  def predict(model : AdaBoostMHModel[_], data: RDD[Array[Double]]): RDD[(Array[Double], Array[Double])] = {
    data.map(info => (info, model predict Vectors.dense(info) toArray))
  }

  def predict2(model : AdaBoostMHModel[_], data: RDD[(String, Array[Double])]): RDD[(String, Array[Double])] = {
    data.map(info => (info._1, model predict Vectors.dense(info._2) toArray))
  }

  def save(model : AdaBoostMHModel[_], path : String): Unit = {
    SparkContext.getOrCreate().parallelize(model.toString.split(";\n")).saveAsTextFile(path)
  }

  def load(path : String, name : BaseLearnerType = BaseLearnerType.DecisionStump,
           numClass : Int, numFeature : Int):AdaBoostMHModel[_] = {
    val modelRDD = SparkContext.getOrCreate().textFile(path)

    name match {
      case BaseLearnerType.DecisionStump =>
        val modelList = modelRDD.map(info => parseFeatureCutLine(info)).collect()
        val model = AdaBoostMHModel.apply(numClass, numFeature, modelList.toList)
        model

      case _ => throw new IllegalArgumentException("not support load this type model : " + name)
    }
  }

  def parseFeatureCutLine(line : String): DecisionStumpModel = {
    val subLine = line.substring(1, line.length - 1)
    val firstIndex = subLine.indexOf(",[")
    val secondIndex = subLine.indexOf("],")
    val alpha = subLine.substring(0, firstIndex).toDouble
    val voteStr = subLine.substring(firstIndex+1, secondIndex+1)
    val vote = Vectors.parse(voteStr)
    val featureCutIdx = subLine.indexOf("FeatureCut(")
    val featureCutArr = subLine.substring(featureCutIdx+11, subLine.length-2).split(",")
    val cut = new FeatureCut(featureCutArr(0).toInt, featureCutArr(1).toDouble)
    val model = new DecisionStumpModel(alpha, vote, Option(cut))
    return model
  }

}
