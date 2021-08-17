package calculator

object Polynomial extends PolynomialInterface {
  def computeDelta(a: Signal[Double], b: Signal[Double],
      c: Signal[Double]): Signal[Double] = {
    Signal[Double](Math.pow(b.apply(),2) - 4 * a.apply() * c.apply())
  }

  def computeSolutions(a: Signal[Double], b: Signal[Double],
      c: Signal[Double], delta: Signal[Double]): Signal[Set[Double]] = {
    Signal({
      val delta = computeDelta(a, b, c)()
      Set((-b.apply() + Math.sqrt(delta)) / (2 * a.apply()), (b.apply() + Math.sqrt(delta)) / (2 * a.apply()))
    })
  }
}
