package ml.combust.mleap.runtime.transformer.feature

import ml.combust.mleap.core.feature.BucketizerModel
import ml.combust.mleap.core.types._
import ml.combust.mleap.runtime.{LeapFrame, LocalDataset, Row}
import org.scalatest.FunSpec

/**
  * Created by hollinwilkins on 9/28/16.
  */
class BucketizerSpec extends FunSpec {
  val schema = StructType(Seq(StructField("test_double", ScalarType.Double))).get
  val dataset = LocalDataset(Seq(Row(11.0), Row(0.0), Row(55.0)))
  val frame = LeapFrame(schema, dataset)

  val bucketizer = Bucketizer(
    shape = NodeShape.scalar(inputCol = "test_double", outputCol = "test_bucket"),
    model = BucketizerModel(Array(0.0, 10.0, 20.0, 100.0)))

  describe("#transform") {
    it("places the input double into the appropriate bucket") {
      val frame2 = bucketizer.transform(frame).get
      val data = frame2.dataset.toArray

      assert(data(0).getDouble(1) == 1.0)
      assert(data(1).getDouble(1) == 0.0)
      assert(data(2).getDouble(1) == 2.0)
    }

    describe("with input feature out of range") {
      val dataset = LocalDataset(Array(Row(11.0), Row(0.0), Row(-23.0)))
      val frame = LeapFrame(schema, dataset)

      it("returns a Failure") { assert(bucketizer.transform(frame).isFailure) }
    }

    describe("with invalid input column") {
      val bucketizer2 = bucketizer.copy(shape = NodeShape.scalar(inputCol = "bad_double", outputCol = "test_bucket"))

      it("returns a Failure") { assert(bucketizer2.transform(frame).isFailure) }
    }
  }

  describe("input/output schema") {
    it("has the correct inputs and outputs") {
      assert(bucketizer.schema.fields ==
        Seq(StructField("test_double", ScalarType.Double.nonNullable),
          StructField("test_bucket", ScalarType.Double.nonNullable)))
    }
  }
}
