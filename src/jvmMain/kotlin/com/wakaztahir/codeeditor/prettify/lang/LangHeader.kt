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
class LangHeader : Lang() {
    companion object {
        val fileExtensions = listOf("header")
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
            Regex("([^ ]+):.*")
        )
    }

}