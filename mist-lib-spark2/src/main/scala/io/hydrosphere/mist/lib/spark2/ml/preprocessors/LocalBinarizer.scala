package io.hydrosphere.mist.lib.spark2.ml.preprocessors

import io.hydrosphere.mist.lib.spark2.ml._
import org.apache.spark.ml.feature.Binarizer

class LocalBinarizer(override val sparkTransformer: Binarizer) extends LocalTransformer[Binarizer] {
  override def transform(localData: LocalData): LocalData = {
    localData.column(sparkTransformer.getInputCol) match {
      case Some(column) =>
        val trashhold: Double = sparkTransformer.getThreshold
        val newData = column.data.map(r => {
          if (r.asInstanceOf[Double] > trashhold) 1.0 else 0.0
        })
        localData.withColumn(LocalDataColumn(sparkTransformer.getOutputCol, newData))
      case None => localData
    }
  }
}

object LocalBinarizer extends LocalModel[Binarizer] {
  override def load(metadata: Metadata, data: Map[String, Any]): Binarizer = {
    new Binarizer(metadata.uid)
      .setInputCol(metadata.paramMap("inputCol").asInstanceOf[String])
      .setOutputCol(metadata.paramMap("outputCol").asInstanceOf[String])
      .setThreshold(metadata.paramMap("threshold").toString.toDouble)
  }

  override implicit def getTransformer(transformer: Binarizer): LocalTransformer[Binarizer] = new LocalBinarizer(transformer)
}
