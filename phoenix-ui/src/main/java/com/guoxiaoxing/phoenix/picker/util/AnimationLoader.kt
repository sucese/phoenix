package com.guoxiaoxing.phoenix.picker.util

import android.content.Context
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.util.AttributeSet
import android.util.Xml
import android.view.animation.*
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

object AnimationLoader {

    @Throws(Resources.NotFoundException::class)
    fun loadAnimation(context: Context, id: Int): Animation {

        var parser: XmlResourceParser? = null

        try {
            parser = context.resources.getAnimation(id)
            return createAnimationFromXml(context, parser)
        } catch (ex: XmlPullParserException) {
            val rnf = Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id))
            rnf.initCause(ex)
            throw rnf
        } catch (ex: IOException) {
            val rnf = Resources.NotFoundException("Can't load animation resource ID #0x" + Integer.toHexString(id))
            rnf.initCause(ex)
            throw rnf
        } finally {
            if (parser != null) parser.close()
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun createAnimationFromXml(context: Context, parser: XmlPullParser, parent: AnimationSet? = null, attrs: AttributeSet = Xml.asAttributeSet(parser)): Animation {

        // Make sure we are on a start tag.
        val type: Int = parser.next()
        val depth = parser.depth

        var anim: Animation = AlphaAnimation(context, attrs)

        while ((type != XmlPullParser.END_TAG || parser.depth > depth)
                && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue
            }

            val name = parser.name
            if (name == "set") {
                anim = AnimationSet(context, attrs)
                createAnimationFromXml(context, parser, anim as AnimationSet?, attrs)
            } else if (name == "alpha") {
                anim = AlphaAnimation(context, attrs)
            } else if (name == "scale") {
                anim = ScaleAnimation(context, attrs)
            } else if (name == "rotate") {
                anim = RotateAnimation(context, attrs)
            } else if (name == "translate") {
                anim = TranslateAnimation(context, attrs)
            } else {
                try {
                    anim = Class.forName(name).getConstructor(Context::class.java, AttributeSet::class.java).newInstance(context, attrs) as Animation
                } catch (te: Exception) {
                    throw RuntimeException("Unknown animation name: " + parser.name + " error:" + te.message)
                }
            }
            parent?.addAnimation(anim)
        }
        return anim
    }
}
