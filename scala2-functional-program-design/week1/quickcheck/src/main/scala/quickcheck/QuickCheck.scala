package quickcheck

import org.scalacheck._
import Arbitrary._
import Gen._
import Prop.forAll

abstract class QuickCheckHeap extends Properties("Heap") with IntHeap {

  lazy val genHeap: Gen[H] = Arbitrary.arbitrary[List[Int]].sample.getOrElse(List()).foldLeft(empty)((h: H, num: Int) => insert(num, h))

  implicit lazy val arbHeap: Arbitrary[H] = Arbitrary(genHeap)

  property("gen1") = forAll { (h: H) =>
    val m = if (isEmpty(h)) 0 else findMin(h)
    findMin(insert(m, h)) == m
  }

  property("Testing minimal of two elements insertion to an empty list") = forAll { (el1: A, el2: A) =>
      List(el1, el2).min == findMin(insert(el1, insert(el2, empty)))
  }

  property("Testing minimum deletion of single element heap") = forAll { (el1: A) =>
    isEmpty(deleteMin(insert(el1, empty)))
  }

  property("Testing find minimum consistent") = forAll { (h: H) =>
    def helper(h: H, current_min: A): Boolean = {
      if (isEmpty(h))
        true
      else {
        val r = findMin(h)
        r >= current_min && helper(deleteMin(h), r)
      }
    }
    helper(h, Int.MinValue)
  }

  property("Testing minimum consistent when melding two heaps") = forAll { (h1: H, h2: H) =>
    val h3 = this.meld(h1, h2)
    if (isEmpty(h3))
      true
    else {
      val r = findMin(h3)
      (!isEmpty(h1) && r == findMin(h1)) || (!isEmpty(h2) && r == findMin(h2))
    }
  }

  property("Insertion order doesn't effect the minimum finding") = forAll {
    (h: H, el1: A, el2: A) =>
      findMin(deleteMin(insert(el2,insert(el1, h)))) == findMin(deleteMin(insert(el1,insert(el2, h))))
  }

  property("Verify deletion doesn't exclude items for any heap") = forAll {
    (i: Int, h: H) =>
      def getOrderHeap(h: H): Set[A] = {
        if (isEmpty(h))
          Set()
        else {
          val min = findMin(h)
          getOrderHeap(deleteMin(h)) + min
        }
      }
      getOrderHeap(insert(i, h)).contains(i)
  }

}
