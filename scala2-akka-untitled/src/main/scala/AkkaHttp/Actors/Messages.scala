package com.talma
package AkkaHttp.Actors

case class PARSE(url: String)
case class PARSE_COMPLETED(imagesUrlList: List[String])
case class DOWNLOAD(url: String)
case class DOWNLOAD_COMPLETED(filename: String, content: String)
case class WRITING_COMPLETED(filename: String)