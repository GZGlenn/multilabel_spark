package test

import org.apache.spark.mllib.classification.SVMModel
import org.apache.spark.mllib.classification.multilabel.baselearners._
import org.apache.spark.mllib.classification.multilabel.stronglearners.AdaBoostMHModel
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.mllib.util.{MultiLabeledPoint, MultiLabeledPointParser, WeightedMultiLabeledPoint}
import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by glenn on 2018/4/2.
  */
object Test {

  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName(this.getClass.getSimpleName).setMaster("local")
    val sc = SparkContext.getOrCreate(conf)

    val data = sc.parallelize(Array(
      "([1.0,0.0],[0.7,0.3,0.9])",
    "([1.0,0.0],[0.4,0.6,1.3])",
    "([0.0,0.0],[1.3,0.6,0.4])",
    "([1.0,0.0],[0.3,0.6,0.5])",
    "([1.0,0.0],[0.9,0.6,1.3])",
    "([0.0,1.0],[1.4,0.8,1.3])",
    "([0.0,1.0],[1.2,0.8,2.2])",
    "([1.0,1.0],[1.3,1.2,0.5])"
    ))
    .map(MultiLabeledPointParser.parse(_))

    val collection = data.collect()

    for (coll <- collection) {
      println("feature:")
      for (feat <- coll.features.toArray) {
        println(feat)
      }

      println("label:")
      for (label <- coll.labels.toArray) {
        println(label)
      }
    }
//
//    val dataSet = {
//      sc.parallelize(Array(
//        "([0.0625,0.0625],([1.0,-1.0],[0.7,0.3,0.9]))",
//        "([0.0625,0.0625],([1.0,-1.0],[0.4,0.6,1.3]))",
//        "([0.0625,0.0625],([-1.0,-1.0],[1.3,0.6,0.4]))",
//        "([0.0625,0.0625],([1.0,-1.0],[0.3,0.6,0.5]))",
//        "([0.0625,0.0625],([1.0,-1.0],[0.9,0.6,1.3]))",
//        "([0.0625,0.0625],([-1.0,1.0],[1.4,0.8,1.3]))",
//        "([0.0625,0.0625],([-1.0,1.0],[1.2,0.8,2.2]))",
//        "([0.0625,0.0625],([-1.0,1.0],[1.3,1.2,0.5]))"
//      )).map(MultiLabeledPointParser.parseWeighted).cache()
//    }
//
//    test1()
//
//    test2(dataSet)
//
//    test3()
//
//    test4()

    println(Vectors.parse("[-1.0,-1.0,-1.0]").getClass)

  }

  def test1(): Unit = {
    val featureCut = new FeatureCut(1, 0.5)
    val multiLabeledPoint = MultiLabeledPoint(
      Vectors.dense(1.0, -1.0),
      Vectors.dense(0.5, 1.0, 0.8))
    assert(featureCut.cut(multiLabeledPoint) == 1.0)

    val weightedMultiLabelPoint = WeightedMultiLabeledPoint(
      Vectors.dense(0.3, 0.7),
      MultiLabeledPoint(
        Vectors.dense(1.0, -1.0),
        Vectors.dense(4.1, 0.2, 2.0)))
    assert(featureCut.cut(weightedMultiLabelPoint) == -1.0)
  }

  def test2(dataSet: RDD[WeightedMultiLabeledPoint]): Unit = {

    // To get split metrics on features 0, 1 and 2.
    val getSplitFunc = DecisionStumpAlgorithm.getLocalSplitMetrics(Iterator(0, 1, 2)) _

    val allSplitMetrics = dataSet.map((0, _))
      .groupByKey()
      .map(_._2.toArray)
      .flatMap(getSplitFunc)

    assert(allSplitMetrics.count() == 16 + 3)

    val aggregatedSplitMetrics = DecisionStumpAlgorithm aggregateSplitMetrics allSplitMetrics

    assert(aggregatedSplitMetrics.count == 16 + 1)

    val model: DecisionStumpModel = DecisionStumpAlgorithm findBestSplitMetrics aggregatedSplitMetrics

    // From the dataset, we see that the best DecisionStumpModel should be FeatureCut(1,0.6) with
    // votes [-1, 1], which has only one classification error on the dataset.
    assert(model.votes == Vectors.dense(-1.0, 1.0))
    assert(model.cut == Some(FeatureCut(1, 0.6)))

  }

  def test3(): Unit ={
    val svmModel1 = new SVMClassificationModel(new SVMModel(Vectors.dense(1.0, -1.0, 1.0), 0.0))
    val svmModel2 = new SVMClassificationModel(new SVMModel(Vectors.dense(-1.0, -1.0, 0.0), 0.0))
    val featureVec1 = Vectors.dense(2.0, 3.0, 4.0)
    val featureVec2 = Vectors.dense(-5.0, 4.0, 3.0)

    assert(svmModel1.predictPoint(featureVec1) == 1.0)
    assert(svmModel1.predictPoint(featureVec2) == 0.0)
    assert(svmModel2.predictPoint(featureVec1) == 0.0)
    assert(svmModel2.predictPoint(featureVec2) == 1.0)

    val gbm1 = new GeneralizedBinaryBaseLearnerModel[SVMClassificationModel](
      1.0, Vectors.dense(1.0, -1.0), svmModel1)
    val gbm2 = new GeneralizedBinaryBaseLearnerModel[SVMClassificationModel](
      0.5, Vectors.dense(1.0, 1.0), svmModel2)

    assert(gbm1.predict(featureVec1) == Vectors.dense(1.0, -1.0))
    assert(gbm1.predict(featureVec2) == Vectors.dense(-1.0, 1.0))
    assert(gbm2.predict(featureVec1) == Vectors.dense(-0.5, -0.5))
    assert(gbm2.predict(featureVec2) == Vectors.dense(0.5, 0.5))

    val adaboostMHModel = new AdaBoostMHModel[GeneralizedBinaryBaseLearnerModel[SVMClassificationModel]](
      2, 3, List(gbm1, gbm2))

    assert(adaboostMHModel.predict(featureVec1) == Vectors.dense(1.0, -1.0))
    assert(adaboostMHModel.predict(featureVec2) == Vectors.dense(-1.0, 1.0))
  }

  def test4(): Unit = {
    Seq(
      MultiLabeledPoint(
        Vectors.dense(1.0, 0.0, 1.0),
        Vectors.dense(0.5, 1.3, 2.2)),
      MultiLabeledPoint(
        Vectors.sparse(2, Array(1), Array(-1.0)),
        Vectors.sparse(2, Array(1), Array(1.0))),
      MultiLabeledPoint(
        Vectors.dense(1.0, 0.0, 1.0),
        Vectors.sparse(2, Array(1), Array(1.0))),
      MultiLabeledPoint(
        Vectors.sparse(2, Array(1), Array(-1.0)),
        Vectors.dense(1.0, 0.0, 1.0))
    ).foreach(p => assert(p == MultiLabeledPointParser.parse(p.toString)))

  }

}
