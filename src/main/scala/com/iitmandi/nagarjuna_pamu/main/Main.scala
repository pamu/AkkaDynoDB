package com.iitmandi.nagarjuna_pamu.main

/**
 * Created by android on 29/1/15.
 */
object Main {

  def main(args: Array[String]): Unit = {
    println("Main Application")
    println("Factorial of 100 is " + fac(100))
  }

  def fac(n: BigInt): BigInt = {
    def facHelper(r: BigInt, c: Int): BigInt = {
      if(c > n) r
      else facHelper(r * c, c + 1)
    }
    facHelper(1, 1)
  }
}
