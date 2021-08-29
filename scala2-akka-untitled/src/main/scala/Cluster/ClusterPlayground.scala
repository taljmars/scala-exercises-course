package com.talma
package Cluster




object ClusterPlayground extends App {

  println("Welcome to Cluster Playground")

  TransformationFrontend.main(Seq("2551").toArray)
  TransformationBackend.main(Seq("2552").toArray)
//  TransformationBackend.main(Array.empty)
//  TransformationBackend.main(Array.empty)
//  TransformationFrontend.main(Array.empty)

  println("Exit")

}
