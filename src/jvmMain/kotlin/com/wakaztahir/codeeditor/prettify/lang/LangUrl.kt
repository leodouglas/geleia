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
class LangUrl : Lang() {
    companion object {
        val fileExtensions = listOf("url")
    }

    override fun getFileExtensions(): List<String> = fileExtensions

    override val fallthroughStylePatterns = ArrayList<StylePattern>()
    override val shortcutStylePatterns = ArrayList<StylePattern>()

    init {
        fallthroughStylePatterns.new(
            Prettify.PR_TAG,
            Regex("(\\{\\{.+}})")
        )
        fallthroughStylePatterns.new(
            Prettify.PR_COMMENT,
            Regex("(//.*?/)")
        )
        fallthroughStylePatterns.new(
            Prettify.PR_COMMENT,
            Regex("^(.+:)")
        )
    }

}