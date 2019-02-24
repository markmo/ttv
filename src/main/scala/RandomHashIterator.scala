import scala.util.Random

class RandomHashIterator(n: Int) extends Iterator[Int => Int] {

  private val primes: Stream[Int] = 2 #::
    Stream.from(3).filter(n => !primes.takeWhile(_ <= math.sqrt(n)).exists(n % _ == 0))

  private def firstPrimeGreaterThan(n: Int): Int = primes.filter(_ > n).take(1).head

  private val random = new Random()

  private val p = firstPrimeGreaterThan(n)

  private def randomHash(n: Int): Int => Int = {
    val a = random.nextInt(n - 2) + 1
    val b = random.nextInt(n - 1)
    val c = p // avoid closure
    x => ((a.toLong * x + b) % c).toInt
  }

  def hasNext = true

  def next: Int => Int = randomHash(n)

}
