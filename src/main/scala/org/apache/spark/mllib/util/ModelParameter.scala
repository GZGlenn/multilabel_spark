package org.apache.spark.mllib.util

import main.MultiBoost.BaseLearnerType
import main.MultiBoost.BaseLearnerType.BaseLearnerType

/**
  * Created by glenn on 2018/4/3.
  */
class ModelParameter(numClasses: Int, numFeatureDimensions: Int) {

  var featureSize = numFeatureDimensions
  var numClass = numClasses
  var sampleRate = 0.3
  var featureRate = 1.0
  var baseLearner : BaseLearnerType = BaseLearnerType.DecisionStump
  var NumBaseLearner = 10

  def setSampleRate(rate : Double) = sampleRate = rate
  def setFeatureRate(rate : Double) = featureRate = rate
  def setBaseLearner(learner : BaseLearnerType) = baseLearner = learner
  def setNumBaseLearner(num : Int) = NumBaseLearner = num
  def setFeatureSize(size : Int) = featureSize = size
  def setNumClass(num : Int) = numClass = num

  def getSampleRate : Double = sampleRate
  def getFeatureRate : Double = featureRate
  def getBaseLearner : BaseLearnerType = baseLearner
  def getNumBaseLearner : Int = NumBaseLearner
  def getNumClasses : Int = numClass
  def getfeatureSize : Int = featureSize


}
