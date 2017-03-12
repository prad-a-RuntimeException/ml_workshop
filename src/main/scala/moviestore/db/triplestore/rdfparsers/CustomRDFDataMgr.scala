/**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * <p>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *//**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  * <p>
  * http://www.apache.org/licenses/LICENSE-2.0
  * <p>
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package moviestore.db.triplestore.rdfparsers

import java.io.{InputStream, Reader}
import java.util.Objects

import org.apache.commons.io.input.ReaderInputStream
import org.apache.jena.atlas.web.{ContentType, TypedInputStream}
import org.apache.jena.riot._
import org.apache.jena.riot.lang.LangRIOT
import org.apache.jena.riot.system._
import org.apache.jena.riot.tokens.Tokenizer
import org.apache.jena.sparql.util.Context
import org.apache.jena.system.JenaSystem
import org.slf4j.{Logger, LoggerFactory}

/**
  * <p>General purpose reader framework for RDF (triples and quads) syntaxes.</p>
  * <ul>
  * <li>HTTP Content negotiation</li>
  * <li>File type hint by the extension</li>
  * <li>Application language hint</li>
  * </ul>
  * <p>
  * It also provides a way to lookup names in different
  * locations and to remap URIs to other URIs.
  * </p>
  * <p>
  * Extensible - a new syntax can be added to the framework.
  * </p>
  * <p>Operations fall into the following categories:</p>
  * <ul>
  * <li>{@code read}    -- Read data from a location into a Model/Dataset etc</li>
  * <li>{@code loadXXX} -- Read data and return an in-memory object holding the data.</li>
  * <li>{@code parse}   -- Read data and send to an {@link StreamRDF}</li>
  * <li>{@code open}    -- Open a typed input stream to the location, using any alternative locations</li>
  * <li>{@code write}   -- Write Model/Dataset etc</li>
  * <li>{@code create}  -- Create a reader or writer explicitly</li>
  * </ul>
  */
object CustomRDFDataMgr {
  private[rdfparsers] val log: Logger = LoggerFactory.getLogger(CustomRDFDataMgr.getClass)
  private val riotBase: String = "http://jena.apache.org/riot/"

  private class MalformedNquadReader private[rdfparsers]() extends ReaderRIOT {
    this.lang = LenientNquadParser.LANG
    errorHandler = ErrorHandlerFactory.getDefaultErrorHandler
    final private var lang: Lang = null
    private var errorHandler: ErrorHandler = null
    private var parserProfile: ParserProfile = null

    def read(in: InputStream, baseURI: String, ct: ContentType, output: StreamRDF, context: Context) {
      @SuppressWarnings(Array("deprecation")) val parser: LangRIOT = createParser(in, output)
      parser.parse()
    }

    def read(in: Reader, baseURI: String, ct: ContentType, output: StreamRDF, context: Context) {
      @SuppressWarnings(Array("deprecation")) val parser: LangRIOT = createParser(new ReaderInputStream(in), output)
      parser.getProfile.setHandler(errorHandler)
      parser.parse()
    }

    def getErrorHandler: ErrorHandler = {
      return errorHandler
    }

    def setErrorHandler(errorHandler: ErrorHandler) {
      this.errorHandler = errorHandler
    }

    def getParserProfile: ParserProfile = {
      return parserProfile
    }

    def setParserProfile(parserProfile: ParserProfile) {
      this.parserProfile = parserProfile
      this.errorHandler = parserProfile.getHandler
    }
  }

  /**
    * Read RDF data.
    *
    * @param sink Destination for the RDF read.
    * @param in   Bytes to read.
    * @param lang Syntax for the stream.
    */
  def parse(sink: StreamRDF, in: InputStream, lang: Lang) {
    process(sink, new TypedInputStream(in), null, lang, null)
  }

  private def getReader(ct: ContentType): ReaderRIOT = {
    val lang: Lang = RDFLanguages.contentTypeToLang(ct)
    if (lang == null) return null
    val r: ReaderRIOTFactory = RDFParserRegistry.getFactory(lang)
    if (r == null) return null
    return r.create(lang)
  }

  def createParser(input: InputStream, dest: StreamRDF): LangRIOT = {
    val tokenizer: Tokenizer = LenientTokenizer.create(input)
    val profile: ParserProfile = RiotLib.profile(RDFLanguages.NQUADS, null)
    return new LenientNquadParser(tokenizer, profile, dest)
  }

  private def process(destination: StreamRDF, in: TypedInputStream, baseUri: String, lang: Lang, context: Context) {
    Objects.requireNonNull(in, "TypedInputStream is null")
    val reader: ReaderRIOT = new CustomRDFDataMgr.MalformedNquadReader
    reader.read(in, baseUri, null, destination, context)
  }

  JenaSystem.init()
}