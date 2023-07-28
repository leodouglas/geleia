package com.wakaztahir.codeeditor.prettify.lang

import com.wakaztahir.codeeditor.prettify.parser.Prettify
import com.wakaztahir.codeeditor.prettify.parser.StylePattern
import com.wakaztahir.codeeditor.utils.new

/**
 * http://www.w3.org/TR/CSS21/grammar.html Section G2 defines the lexical
 * grammar.  This scheme does not recognize keywords containing escapes.
 *
 * @author leodouglas@gmail.com
 */
class LangJson : Lang() {
    companion object {
        val fileExtensions = listOf("json")
    }

    override fun getFileExtensions(): List<String> = fileExtensions

    override val fallthroughStylePatterns = ArrayList<StylePattern>()
    override val shortcutStylePatterns = ArrayList<StylePattern>()

    init {
        shortcutStylePatterns.new(
            Prettify.PR_PLAIN,
            Regex("^[\\t\\n\\r \\xA0]+"),
            null
        )
        shortcutStylePatterns.new(
            Prettify.PR_PUNCTUATION,
            Regex("^[.!%&()*+,\\-;<=>?\\[\\]^{|}:]+"),
            null
        )
        fallthroughStylePatterns.new(
            Prettify.PR_KEYWORD,
            Regex("^(?:true|false|null)\\b")
        )
        fallthroughStylePatterns.new(
            Prettify.PR_LITERAL,
            Regex("^(0[xX][0-9a-fA-F_]+L?|0[bB][0-1]+L?|[0-9_.]+([eE]-?[0-9]+)?[fFL]?)")
        )
        fallthroughStylePatterns.new(Prettify.PR_STRING, Regex("'.'"))
        fallthroughStylePatterns.new(
            Prettify.PR_TAG,
            Regex("\\{\\{.*}}")
        )
        fallthroughStylePatterns.new(
            Prettify.PR_TYPE,
            Regex("(^\".*\") *?:")
        )
        fallthroughStylePatterns.new(
            Prettify.PR_STRING,
            Regex("^\".*\"")
        )
    }

}