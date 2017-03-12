package moviestore.db.triplestore.rdfparsers

import org.apache.jena.graph.Node
import org.apache.jena.riot.Lang
import org.apache.jena.riot.LangBuilder
import org.apache.jena.riot.RDFLanguages
import org.apache.jena.riot.lang.LangNTuple
import org.apache.jena.riot.system.ParserProfile
import org.apache.jena.riot.system.StreamRDF
import org.apache.jena.riot.tokens.Token
import org.apache.jena.riot.tokens.TokenType
import org.apache.jena.riot.tokens.Tokenizer
import org.apache.jena.sparql.core.Quad
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
  * Since the data we typically get from the internet is faulty, we need
  * a more lenient parser, that would handle malformed quads more gracefully,
  * without bailing out
  * <p>
  *
  * @see: LangNQuads.java. The original class is not designed to be extendable.
  */
object LenientNquadParser {
  val MALFORMED_NQUAD_LANG: String = "malformed_nquad"
  val LOGGER: Logger = LoggerFactory.getLogger(classOf[LenientNquadParser])
  val LANG: Lang = createLang

  private def createLang: Lang = {
    val originalNquadLang: Lang = RDFLanguages.NQUADS
    return LangBuilder.create(MALFORMED_NQUAD_LANG, Lang.NQUADS.toString).contentType(originalNquadLang.getContentType.getContentType).build
  }
}

class LenientNquadParser(tokens: Tokenizer,profile: ParserProfile,dest: StreamRDF) extends LangNTuple[Quad](tokens, profile, dest) {
  private var currentGraph: Node = null

  def getLang: Lang = {
    if (!RDFLanguages.isRegistered(LenientNquadParser.LANG)) {
      RDFLanguages.register(LenientNquadParser.LANG)
    }
    return RDFLanguages.nameToLang(LenientNquadParser.MALFORMED_NQUAD_LANG)
  }

  /**
    * Method to parse the whole stream of triples, sending each to the sink
    */
  final protected def runParser() {
    while (hasNext) {
      {
        var x: Quad = null
        try {
          x = parseOne
        }
        catch {
          case e: Exception => {
            LenientNquadParser.LOGGER.warn("NQuad parsing failed {}", e.getMessage)
          }
        }
        if (x != null) dest.quad(x)
      }
    }
  }

  final protected def parseOne: Quad = {
    val sToken: Token = nextToken
    if (sToken.getType eq TokenType.EOF) exception(sToken, "Premature end of file: %s", sToken)
    val pToken: Token = nextToken
    if (pToken.getType eq TokenType.EOF) exception(pToken, "Premature end of file: %s", pToken)
    val oToken: Token = nextToken
    if (oToken.getType eq TokenType.EOF) exception(oToken, "Premature end of file: %s", oToken)
    var xToken: Token = nextToken // Maybe DOT
    if (xToken.getType eq TokenType.EOF) exception(xToken, "Premature end of file: Quad not terminated by DOT: %s", xToken)
    // Process graph node first, before S,P,O
    // to set bnode label scope (if not global)
    var c: Node = null
    if (xToken.getType ne TokenType.DOT) {
      // Allow bNodes for graph names.
      checkIRIOrBNode(xToken)
      // Allow only IRIs
      //checkIRI(xToken) ;
      c = tokenAsNode(xToken)
      xToken = nextToken
      currentGraph = c
    }
    else {
      c = Quad.defaultGraphNodeGenerated
      currentGraph = null
    }
    // createQuad may also check but these checks are cheap and do form syntax errors.
    checkIRIOrBNode(sToken)
    checkIRI(pToken)
    checkRDFTerm(oToken)
    // xToken already checked.
    val s: Node = tokenAsNode(sToken)
    val p: Node = tokenAsNode(pToken)
    val o: Node = tokenAsNode(oToken)
    // Check end of tuple.
    if (xToken.getType ne TokenType.DOT) exception(xToken, "Quad not terminated by DOT: %s", xToken)
    return profile.createQuad(c, s, p, o, sToken.getLine, sToken.getColumn)
  }

  final protected def tokenAsNode(token: Token): Node = {
    return profile.create(currentGraph, token)
  }
}