package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.MaxAbsScalerModel
import ml.combust.mleap.core.types._
import ml.combust.mleap.runtime.{LeapFrame, LocalDataset, Row}
import ml.combust.mleap.tensor.Tensor
import org.apache.spark.ml.linalg.Vectors
import org.scalatest.FunSpec

/**
  * Created by mikhail on 9/25/16.
  */
class MaxAbsScalerSpec extends FunSpec{
  val schema = StructType(Seq(StructField("test_vec", TensorType(BasicType.Double)))).get
  val dataset = LocalDataset(Seq(Row(Tensor.denseVector(Array(0.0, 20.0, 20.0)))))
  val frame = LeapFrame(schema, dataset)

  val maxAbsScaler = MaxAbsScaler(
    shape = NodeShape.vector(3, 3, inputCol = "test_vec", outputCol = "test_normalized"),
    model = MaxAbsScalerModel(Vectors.dense(Array(10.0, 20.0, 40.0))))

  describe("#transform") {
    it("scales the input data by maximum value vector") {
      val frame2 = maxAbsScaler.transform(frame).get
      val data = frame2.dataset.toArray
      val norm = data(0).getTensor[Double](1)

      assert(norm(0) == 0.0)
      assert(norm(1) == 1.0)
      assert(norm(2) == 0.5)
    }

    describe("with invalid input column") {
      val maxAbsScaler2 = maxAbsScaler.copy(shape = NodeShape.vector(3, 3, inputCol = "bad_input"))

      it("returns a Failure") { assert(maxAbsScaler2.transform(frame).isFailure) }
    }
  }

  describe("input/output schema") {
    it("has the correct inputs and outputs") {
      assert(maxAbsScaler.schema.fields ==
        Seq(StructField("test_vec", TensorType.Double(3)),
          StructField("test_normalized", TensorType.Double(3))))
    }
  }
}
